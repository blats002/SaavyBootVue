<script setup>
import { computed, ref } from 'vue';
import { QrcodeStream } from 'vue-qrcode-reader';
import AuthService from '../service/AuthService';

const props = defineProps({
  role: {
    type: [String, Array],
    default: null
  },
  paused: {
    type: Boolean,
    default: false
  },
  torch: {
    type: Boolean,
    default: false
  },
  showScannerFrame: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['scan', 'detect', 'error', 'camera-on', 'camera-off']);

const isRoleAuthorized = computed(() => {
  if (!props.role) return true;
  return AuthService.hasRole(props.role);
});

const isCameraActive = ref(false);
const errorMessage = ref('');

const onDetect = (detectedCodes) => {
  if (props.paused) return;
  const firstCode = detectedCodes[0];
  if (firstCode && firstCode.rawValue) {
    emit('scan', firstCode.rawValue);
    emit('detect', detectedCodes);
  }
};

const onCameraOn = (capabilities) => {
  isCameraActive.value = true;
  errorMessage.value = '';
  emit('camera-on', capabilities);
};

const onCameraOff = () => {
  isCameraActive.value = false;
  emit('camera-off');
};

const onError = (error) => {
  console.error('QR scanner error:', error);
  if (error.name === 'NotAllowedError') {
    errorMessage.value = 'Camera access permission denied.';
  } else if (error.name === 'NotFoundError') {
    errorMessage.value = 'No camera found on this device.';
  } else if (error.name === 'NotSupportedError') {
    errorMessage.value = 'Secure context required (HTTPS or localhost).';
  } else if (error.name === 'NotReadableError') {
    errorMessage.value = 'Camera is already in use by another application.';
  } else {
    errorMessage.value = error.message || 'Camera initialization error.';
  }
  emit('error', error);
};

function paintBoundingBox(detectedCodes, ctx) {
  for (const detectedCode of detectedCodes) {
    const {
      boundingBox: { x, y, width, height }
    } = detectedCode;

    ctx.lineWidth = 3;
    ctx.strokeStyle = '#22c55e';
    ctx.strokeRect(x, y, width, height);
  }
}
</script>

<template>
  <div v-if="isRoleAuthorized" class="generic-qr-reader-container relative">
    <div v-if="errorMessage" class="p-3 border-round bg-red-50 text-red-600 border-1 border-red-200 text-center mb-2">
      <i class="pi pi-exclamation-triangle text-xl mb-1 block"></i>
      <span>{{ errorMessage }}</span>
    </div>

    <div class="camera-viewport relative overflow-hidden border-round shadow-2 surface-card">
      <QrcodeStream
        :paused="paused"
        :torch="torch"
        :track="paintBoundingBox"
        @detect="onDetect"
        @camera-on="onCameraOn"
        @camera-off="onCameraOff"
        @error="onError"
      >
        <!-- Overlay scanning target frame -->
        <div v-if="showScannerFrame && !paused" class="scanner-overlay flex justify-content-center align-items-center">
          <div class="scanner-frame">
            <div class="laser-line"></div>
          </div>
        </div>
      </QrcodeStream>
    </div>
  </div>
</template>

<style scoped lang="scss">
.generic-qr-reader-container {
  width: 100%;
  max-width: 450px;
  margin: 0 auto;
}

.camera-viewport {
  width: 100%;
  aspect-ratio: 1 / 1;
  background-color: #0f172a;
}

.scanner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.scanner-frame {
  width: 70%;
  height: 70%;
  border: 2px dashed rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  position: relative;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.35);

  &::before, &::after {
    content: '';
    position: absolute;
    width: 24px;
    height: 24px;
    border-color: #3b82f6;
    border-style: solid;
  }
  &::before {
    top: -2px;
    left: -2px;
    border-width: 4px 0 0 4px;
    border-top-left-radius: 14px;
  }
  &::after {
    bottom: -2px;
    right: -2px;
    border-width: 0 4px 4px 0;
    border-bottom-right-radius: 14px;
  }
}

.laser-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #22c55e, #3b82f6, transparent);
  box-shadow: 0 0 8px #22c55e;
  animation: scanLaser 2s infinite ease-in-out;
}

@keyframes scanLaser {
  0% {
    top: 0%;
  }
  50% {
    top: 98%;
  }
  100% {
    top: 0%;
  }
}
</style>