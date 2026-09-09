<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import axios from 'axios';
import GenericQRReader from '@/components/GenericQRReader.vue';
import { useToast } from 'primevue/usetoast';

const toast = useToast();

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

// Live Clock State
const currentTime = ref('');
const currentDate = ref('');
let clockTimer = null;

const updateClock = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  currentDate.value = now.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
};

// Scanner & Kiosk State
const isScannerActive = ref(true);
const isProcessing = ref(false);
const lastScanResult = ref(null);
const selectedAction = ref(null); // null = Auto Detect, or 'CLOCK_IN', 'CLOCK_OUT', 'BREAK_OUT', 'BREAK_IN'
const showPinDialog = ref(false);
const pinCode = ref('');
const lastScannedToken = ref('');
let debounceTimer = null;

// Today's summary stats
const summaryStats = ref({
  presentToday: 0,
  lateToday: 0,
  totalActiveEmployees: 0
});

// Sound Generator using Web Audio API (No external sound assets needed)
const playChime = (isSuccess = true) => {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);

    if (isSuccess) {
      // Pleasant high double beep
      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
      osc.frequency.setValueAtTime(880, ctx.currentTime + 0.1); // A5
      gain.gain.setValueAtTime(0.3, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.35);
      osc.start();
      osc.stop(ctx.currentTime + 0.35);
    } else {
      // Error buzz
      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(200, ctx.currentTime);
      gain.gain.setValueAtTime(0.4, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.4);
      osc.start();
      osc.stop(ctx.currentTime + 0.4);
    }
  } catch (e) {
    console.warn('Audio playback not permitted yet:', e);
  }
};

const fetchTodaySummary = async () => {
  try {
    const res = await axios.get(`${SERVER_URL}/api/attendance/today-summary`);
    if (res.data) {
      summaryStats.value = res.data;
    }
  } catch (e) {
    console.error('Failed to load summary stats:', e);
  }
};

// Process Scan Endpoint
const submitScan = async (token, pin = null) => {
  if (isProcessing.value) return;

  // Anti-passback debounce (prevent double scan within 5 seconds of exact same token)
  if (token && token === lastScannedToken.value) {
    return;
  }

  isProcessing.value = true;
  lastScannedToken.value = token || '';

  try {
    const res = await axios.post(`${SERVER_URL}/api/attendance/scan-qr`, {
      qrToken: token,
      pinCode: pin,
      kioskDeviceId: 'Web-Kiosk-Terminal-01',
      preferredAction: selectedAction.value
    });

    const data = res.data;
    if (data.success) {
      playChime(true);
      lastScanResult.value = {
        ...data,
        receivedAt: new Date()
      };
      toast.add({
        severity: data.status === 'LATE' ? 'warn' : 'success',
        summary: data.employeeName,
        detail: data.message,
        life: 5000
      });
      fetchTodaySummary();
    } else {
      playChime(false);
      toast.add({
        severity: 'error',
        summary: 'Punch Failed',
        detail: data.message || 'Unrecognized badge or PIN.',
        life: 4000
      });
    }
  } catch (err) {
    playChime(false);
    toast.add({
      severity: 'error',
      summary: 'Connection Error',
      detail: err.response?.data?.message || err.message,
      life: 4000
    });
  } finally {
    isProcessing.value = false;
    showPinDialog.value = false;
    pinCode.value = '';

    // Clear debounce after 6 seconds
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      lastScannedToken.value = '';
    }, 6000);
  }
};

const onQrScanned = (token) => {
  if (token) {
    submitScan(token);
  }
};

const submitManualPin = () => {
  if (pinCode.value && pinCode.value.trim().length > 0) {
    submitScan(null, pinCode.value.trim());
  }
};

onMounted(() => {
  updateClock();
  clockTimer = setInterval(updateClock, 1000);
  fetchTodaySummary();
});

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer);
  if (debounceTimer) clearTimeout(debounceTimer);
});
</script>

<template>
  <div class="bundy-kiosk-page surface-ground h-screen w-screen flex flex-column justify-content-between p-3 md:p-4 lg:p-5 overflow-hidden select-none">
    <Toast />

    <!-- Top Navigation & Brand Header -->
    <header class="kiosk-top-bar w-full max-w-7xl mx-auto flex justify-content-between align-items-center flex-shrink-0 px-2 py-1">
      <div class="flex align-items-center gap-2">
        <div class="kiosk-logo-badge bg-primary text-white border-round-lg flex align-items-center justify-content-center p-2 shadow-2">
          <i class="pi pi-qrcode text-xl sm:text-2xl font-bold"></i>
        </div>
        <div>
          <span class="font-bold text-base sm:text-xl text-900 tracking-wide block line-height-1">SAAVY KIOSK</span>
          <span class="text-xs text-500 font-medium">Smart Attendance Station</span>
        </div>
      </div>

      <div class="flex align-items-center gap-2">
        <router-link to="/">
          <Button icon="pi pi-home" label="Portal" class="p-button-outlined p-button-secondary p-button-sm text-xs py-2 px-3 border-round-lg shadow-1" />
        </router-link>
      </div>
    </header>

    <!-- Live Digital Clock Display -->
    <section class="kiosk-header text-center flex-shrink-0 my-2">
      <div class="text-500 font-semibold text-xs sm:text-sm md:text-base uppercase tracking-widest mb-1">{{ currentDate }}</div>
      <div class="live-clock text-5xl sm:text-6xl md:text-7xl font-extrabold text-primary line-height-1">{{ currentTime }}</div>
      <div class="text-xs sm:text-sm text-400 mt-1">Authorized Punch Terminal &bull; Auto-detecting shift actions</div>
    </section>

    <!-- Main Responsive Kiosk Grid -->
    <main class="kiosk-main-grid grid w-full max-w-7xl mx-auto align-items-stretch justify-content-center flex-1 gap-3 md:gap-4 my-2 px-2 overflow-hidden">
      <!-- Left Column: Camera Scanner & Mode Controls -->
      <div class="col-12 md:col flex flex-column justify-content-center h-full max-w-xl">
        <div class="kiosk-card card p-3 sm:p-4 surface-card shadow-3 border-round-2xl flex flex-column justify-content-between h-full border-1 surface-border">
          <!-- Card Header -->
          <div class="flex justify-content-between align-items-center mb-2 flex-shrink-0">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-camera text-primary text-lg sm:text-xl"></i>
              <span class="font-bold text-base sm:text-lg text-900">QR Badge Scanner</span>
            </div>
            <Button
              icon="pi pi-key"
              label="Enter PIN"
              class="p-button-outlined p-button-primary p-button-sm py-1 px-3 text-xs border-round-lg"
              @click="showPinDialog = true"
            />
          </div>

          <!-- Responsive Camera Viewport -->
          <div class="scanner-box relative border-round-xl overflow-hidden shadow-inner surface-900 flex-1 my-2">
            <GenericQRReader
              :paused="isProcessing"
              @scan="onQrScanned"
            />
            
            <!-- Scanning Laser Animation Overlay -->
            <div class="scanner-laser"></div>

            <div v-if="isProcessing" class="processing-overlay flex flex-column align-items-center justify-content-center">
              <ProgressSpinner style="width: 48px; height: 48px" strokeWidth="4" />
              <div class="text-white font-bold text-sm sm:text-base mt-3">Processing Punch...</div>
            </div>
          </div>

          <!-- Mode Override Controls (Touch-Friendly) -->
          <div class="punch-mode-selector flex-shrink-0 pt-2 border-top-1 surface-border">
            <div class="text-xs text-500 font-semibold uppercase tracking-wider mb-2">Punch Mode:</div>
            <div class="grid grid-nogutter gap-2">
              <Button
                label="Auto Toggle"
                icon="pi pi-refresh"
                :class="selectedAction === null ? 'p-button-primary shadow-2 font-bold' : 'p-button-outlined p-button-secondary'"
                class="col p-button-sm text-xs sm:text-sm py-2 border-round-lg"
                @click="selectedAction = null"
              />
              <Button
                label="Clock In"
                icon="pi pi-sign-in"
                :class="selectedAction === 'CLOCK_IN' ? 'p-button-success shadow-2 font-bold' : 'p-button-outlined p-button-secondary'"
                class="col p-button-sm text-xs sm:text-sm py-2 border-round-lg"
                @click="selectedAction = 'CLOCK_IN'"
              />
              <Button
                label="Clock Out"
                icon="pi pi-sign-out"
                :class="selectedAction === 'CLOCK_OUT' ? 'p-button-danger shadow-2 font-bold' : 'p-button-outlined p-button-secondary'"
                class="col p-button-sm text-xs sm:text-sm py-2 border-round-lg"
                @click="selectedAction = 'CLOCK_OUT'"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Instant Feedback & Today's Attendance Summary -->
      <div class="col-12 md:col flex flex-column justify-content-between h-full gap-3 max-w-xl">
        <!-- Live Result Banner or Waiting Animation Card -->
        <div 
          v-if="lastScanResult" 
          class="kiosk-card card p-3 sm:p-4 surface-card shadow-3 border-round-2xl flex-1 flex flex-column justify-content-between border-left-4 animate-fadein"
          :class="lastScanResult.status === 'LATE' ? 'border-orange-500' : 'border-green-500'"
        >
          <div class="flex align-items-center justify-content-between mb-3 flex-shrink-0">
            <div class="flex align-items-center gap-3">
              <Avatar
                v-if="lastScanResult.avatar"
                :image="lastScanResult.avatar"
                size="xlarge"
                shape="circle"
                class="shadow-2 border-2 border-white"
                style="width: 4rem; height: 4rem;"
              />
              <Avatar
                v-else
                icon="pi pi-user"
                size="xlarge"
                shape="circle"
                :class="lastScanResult.status === 'LATE' ? 'bg-orange-100 text-orange-700' : 'bg-green-100 text-green-700'"
                style="width: 4rem; height: 4rem;"
              />
              <div>
                <div class="text-xl sm:text-2xl font-bold text-900 line-height-1 mb-1">{{ lastScanResult.employeeName }}</div>
                <div class="text-xs sm:text-sm text-500 font-medium">{{ lastScanResult.department || 'General' }} &bull; ID: {{ lastScanResult.employeeId }}</div>
              </div>
            </div>
            <Tag
              :value="lastScanResult.logType?.replace('_', ' ')"
              :severity="lastScanResult.logType === 'CLOCK_IN' ? 'success' : 'danger'"
              class="text-xs sm:text-sm font-bold px-3 py-2 border-round-lg"
            />
          </div>

          <!-- Result Punch Details -->
          <div class="surface-50 p-3 border-round-xl text-center my-auto flex-1 flex flex-column justify-content-center">
            <div class="text-sm sm:text-base font-semibold text-800 mb-2">{{ lastScanResult.message }}</div>
            <div class="flex justify-content-center gap-2">
              <Tag
                v-if="lastScanResult.status"
                :value="lastScanResult.status === 'ON_TIME' ? 'ON TIME' : lastScanResult.status"
                :severity="lastScanResult.status === 'ON_TIME' ? 'success' : lastScanResult.status === 'LATE' ? 'warning' : 'info'"
                class="text-xs px-2 py-1 font-bold"
              />
              <Tag
                :value="lastScanResult.verificationMethod || 'QR CODE'"
                severity="secondary"
                class="text-xs px-2 py-1"
              />
            </div>
          </div>
          
          <div class="text-center text-xs text-400 mt-2 flex-shrink-0">
            Recorded at: {{ new Date(lastScanResult.timestamp || lastScanResult.receivedAt).toLocaleTimeString() }}
          </div>
        </div>

        <!-- Standby Ready State Card -->
        <div v-else class="kiosk-card card p-4 surface-card shadow-2 border-round-2xl text-center flex-1 flex flex-column justify-content-center align-items-center border-1 surface-border">
          <div class="scan-pulse-container mb-3">
            <i class="pi pi-id-card text-5xl sm:text-6xl text-primary"></i>
          </div>
          <div class="text-xl sm:text-2xl font-bold text-900 mb-2">Ready to Scan</div>
          <div class="text-xs sm:text-sm text-500 max-w-sm">Present your QR badge in front of the camera or click "Enter PIN" to record your attendance.</div>
        </div>

        <!-- Today's Attendance KPI Summary -->
        <div class="kiosk-card card p-3 sm:p-4 surface-card shadow-3 border-round-2xl flex-shrink-0 border-1 surface-border">
          <div class="flex justify-content-between align-items-center mb-2">
            <span class="text-xs sm:text-sm font-bold text-600 uppercase tracking-wider">Today's Quick Summary</span>
            <span class="text-xs text-400 font-medium">Live Feed</span>
          </div>
          <div class="grid text-center grid-nogutter align-items-center">
            <div class="col-4">
              <div class="text-2xl sm:text-3xl font-extrabold text-green-600 line-height-1">{{ summaryStats.presentToday }}</div>
              <div class="text-xs text-500 font-medium mt-1">Checked In</div>
            </div>
            <div class="col-4 border-left-1 border-right-1 surface-border">
              <div class="text-2xl sm:text-3xl font-extrabold text-orange-500 line-height-1">{{ summaryStats.lateToday }}</div>
              <div class="text-xs text-500 font-medium mt-1">Late</div>
            </div>
            <div class="col-4">
              <div class="text-2xl sm:text-3xl font-extrabold text-primary line-height-1">{{ summaryStats.totalActiveEmployees }}</div>
              <div class="text-xs text-500 font-medium mt-1">Total Staff</div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Kiosk Footer Info Bar -->
    <footer class="text-center text-xs text-400 py-1 flex-shrink-0">
      Saavy Enterprise Attendance Kiosk &bull; Terminal ID: Web-Kiosk-Terminal-01
    </footer>

    <!-- Manual PIN Entry Dialog -->
    <Dialog v-model:visible="showPinDialog" modal header="Enter Employee PIN" :style="{ width: '360px' }" class="border-round-xl">
      <div class="p-fluid pt-2">
        <div class="field mb-4">
          <label for="pinCodeInput" class="font-semibold text-sm text-700">4-Digit Security PIN</label>
          <Password
            id="pinCodeInput"
            v-model="pinCode"
            :feedback="false"
            toggleMask
            placeholder="Enter PIN"
            class="w-full text-center text-3xl font-bold"
            @keyup.enter="submitManualPin"
          />
        </div>
        <Button label="Confirm Punch" icon="pi pi-check" class="p-button-primary py-3 font-bold border-round-lg" @click="submitManualPin" :disabled="!pinCode" />
      </div>
    </Dialog>
  </div>
</template>

<style scoped lang="scss">
.bundy-kiosk-page {
  height: 100vh;
  max-height: 100vh;
  overflow: hidden;
  background: radial-gradient(circle at 50% 0%, var(--surface-card) 0%, var(--surface-ground) 100%);
}

.live-clock {
  font-family: 'Inter', monospace, sans-serif;
  letter-spacing: -1.5px;
}

.scanner-box {
  width: 100%;
  min-height: 180px;
  max-height: 38vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #0f172a;

  :deep(.generic-qr-reader-container) {
    width: 100%;
    height: 100%;
    max-height: 38vh;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  :deep(.camera-viewport) {
    width: 100%;
    height: 100%;
    max-height: 38vh;
    aspect-ratio: auto;
    object-fit: cover;
  }
}

.processing-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.88);
  backdrop-filter: blur(4px);
  z-index: 10;
}

.scan-pulse-container {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(99, 102, 241, 0.1);
  animation: pulseShadow 2.5s infinite ease-in-out;
}

@keyframes pulseShadow {
  0% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.4);
  }
  70% {
    box-shadow: 0 0 0 20px rgba(99, 102, 241, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0);
  }
}

@keyframes scanLaser {
  0% {
    top: 0%;
  }
  50% {
    top: 96%;
  }
  100% {
    top: 0%;
  }
}
</style>
