<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import createJpaService from '@core/service/JPAService';
import GenericQRCode from '@/components/GenericQRCode.vue';
import GenericCrud from '@/components/GenericCrud.vue';

const badges = ref([]);
const loading = ref(false);
const selectedBadge = ref(null);
const showPrintPreview = ref(false);

const fetchBadges = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/employee-badges');
    badges.value = res.data?.content || res.data || [];
  } catch (e) {
    console.error('Failed to load badges:', e);
  } finally {
    loading.value = false;
  }
};

const openPrintBadge = (badge) => {
  selectedBadge.value = badge;
  showPrintPreview.value = true;
};

const printCurrentBadge = () => {
  window.print();
};

onMounted(() => {
  fetchBadges();
});

const badgeService = createJpaService('employee-badges');

const meta = ref(null);
const fields = ref([]);
const isDataLoaded = ref(false);

const getAvatarSrc = (badge) => {
  if (!badge) return null;
  if (badge.avatarFile?.content) return badge.avatarFile.content;
  if (typeof badge.avatarFile === 'string' && badge.avatarFile) return badge.avatarFile;
  if (badge.content) return badge.content;
  return null;
};

const downloadBadge = async () => {
  if (!selectedBadge.value) return;

  const badge = selectedBadge.value;
  const canvas = document.createElement('canvas');
  const scale = 2; // High-resolution scale for sharp print/screen quality
  const width = 360;
  const height = 480;
  canvas.width = width * scale;
  canvas.height = height * scale;
  const ctx = canvas.getContext('2d');
  ctx.scale(scale, scale);

  // 1. Badge Background
  const radius = 16;
  ctx.fillStyle = '#ffffff';
  ctx.strokeStyle = '#e2e8f0';
  ctx.lineWidth = 1.5;

  ctx.beginPath();
  ctx.moveTo(radius, 0);
  ctx.lineTo(width - radius, 0);
  ctx.quadraticCurveTo(width, 0, width, radius);
  ctx.lineTo(width, height - radius);
  ctx.quadraticCurveTo(width, height, width - radius, height);
  ctx.lineTo(radius, height);
  ctx.quadraticCurveTo(0, height, 0, height - radius);
  ctx.lineTo(0, radius);
  ctx.quadraticCurveTo(0, 0, radius, 0);
  ctx.closePath();
  ctx.fill();
  ctx.stroke();

  // Top Accent Bar
  ctx.save();
  ctx.beginPath();
  ctx.moveTo(radius, 0);
  ctx.lineTo(width - radius, 0);
  ctx.quadraticCurveTo(width, 0, width, radius);
  ctx.lineTo(width, 8);
  ctx.lineTo(0, 8);
  ctx.lineTo(0, radius);
  ctx.quadraticCurveTo(0, 0, radius, 0);
  ctx.closePath();
  ctx.fillStyle = '#6366f1';
  ctx.fill();
  ctx.restore();

  // 2. Header Text
  ctx.textAlign = 'center';
  ctx.fillStyle = '#6366f1';
  ctx.font = 'bold 11px Inter, sans-serif';
  ctx.fillText('SAAVY ENTERPRISE', width / 2, 28);

  ctx.fillStyle = '#64748b';
  ctx.font = '9px Inter, sans-serif';
  ctx.fillText('OFFICIAL ACCESS IDENTIFICATION', width / 2, 42);

  // 3. Avatar Photo / Fallback Initial
  const avatarSize = 72;
  const avatarX = (width - avatarSize) / 2;
  const avatarY = 54;
  const avatarSrc = getAvatarSrc(badge);

  const loadImg = (src) => new Promise((resolve) => {
    const img = new window.Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => resolve(img);
    img.onerror = () => resolve(null);
    img.src = src;
  });

  const drawFallbackAvatar = () => {
    ctx.beginPath();
    ctx.arc(avatarX + avatarSize / 2, avatarY + avatarSize / 2, avatarSize / 2, 0, Math.PI * 2);
    ctx.fillStyle = '#e0e7ff';
    ctx.fill();
    ctx.strokeStyle = '#c7d2fe';
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.fillStyle = '#4338ca';
    ctx.font = 'bold 28px Inter, sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    const initial = badge.employeeName ? badge.employeeName.charAt(0).toUpperCase() : 'E';
    ctx.fillText(initial, avatarX + avatarSize / 2, avatarY + avatarSize / 2);
    ctx.textBaseline = 'alphabetic';
  };

  if (avatarSrc) {
    try {
      const img = await loadImg(avatarSrc);
      if (img) {
        ctx.save();
        ctx.beginPath();
        ctx.arc(avatarX + avatarSize / 2, avatarY + avatarSize / 2, avatarSize / 2, 0, Math.PI * 2);
        ctx.closePath();
        ctx.clip();
        ctx.drawImage(img, avatarX, avatarY, avatarSize, avatarSize);
        ctx.restore();

        // Border around avatar
        ctx.beginPath();
        ctx.arc(avatarX + avatarSize / 2, avatarY + avatarSize / 2, avatarSize / 2, 0, Math.PI * 2);
        ctx.strokeStyle = '#cbd5e1';
        ctx.lineWidth = 2;
        ctx.stroke();
      } else {
        drawFallbackAvatar();
      }
    } catch (e) {
      drawFallbackAvatar();
    }
  } else {
    drawFallbackAvatar();
  }

  // 4. Employee Information
  ctx.fillStyle = '#0f172a';
  ctx.font = 'bold 16px Inter, sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(badge.employeeName || 'Employee', width / 2, 148);

  ctx.fillStyle = '#475569';
  ctx.font = '600 12px Inter, sans-serif';
  ctx.fillText(badge.jobTitle || 'Staff', width / 2, 166);

  ctx.fillStyle = '#64748b';
  ctx.font = '10px Inter, sans-serif';
  const deptText = `${badge.department || 'General'}  •  ID: ${badge.employeeId || '-'}`;
  ctx.fillText(deptText, width / 2, 182);

  // 5. QR Code
  const qrCanvas = document.querySelector('.printable-badge canvas');
  const qrImgEl = document.querySelector('.printable-badge img:not([alt="Avatar"])');
  const qrElement = qrCanvas || qrImgEl;

  if (qrElement) {
    const qrSize = 130;
    const qrX = (width - qrSize) / 2;
    const qrY = 196;

    // QR white card container
    ctx.fillStyle = '#ffffff';
    ctx.strokeStyle = '#e2e8f0';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.roundRect ? ctx.roundRect(qrX - 8, qrY - 8, qrSize + 16, qrSize + 16, 8) : ctx.rect(qrX - 8, qrY - 8, qrSize + 16, qrSize + 16);
    ctx.fill();
    ctx.stroke();

    if (qrElement.tagName.toLowerCase() === 'canvas') {
      ctx.drawImage(qrElement, qrX, qrY, qrSize, qrSize);
    } else {
      const qrImg = await loadImg(qrElement.src);
      if (qrImg) {
        ctx.drawImage(qrImg, qrX, qrY, qrSize, qrSize);
      }
    }
  }

  // 6. Footer Note
  ctx.fillStyle = '#94a3b8';
  ctx.font = '9px Inter, sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('Scan at any authorized Bundy Clock Kiosk terminal.', width / 2, 458);

  // 7. Trigger Download
  const safeName = (badge.employeeName || 'badge').toLowerCase().replace(/[^a-z0-9]/g, '-');
  const fileName = `badge-${badge.employeeId || 'emp'}-${safeName}.png`;
  const link = document.createElement('a');
  link.download = fileName;
  link.href = canvas.toDataURL('image/png');
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

onMounted(async () => {
  try {
    meta.value = await badgeService.getMasterMeta();
    fields.value = meta.value?.fields || [];
    isDataLoaded.value = true;
  } catch (e) {
    console.error('Failed to load party metadata:', e);
    isDataLoaded.value = true;
  }
});
</script>

<template>
  <div class="grid">
    <!-- Top Bar: Badge CRUD and Action Header -->
    <div class="col-12">
      <div class="card p-4">
        <!-- Admin Table to Add / Edit Badges -->
        <div class="mt-5 border-top-1 surface-border pt-4">
          <div v-if="isDataLoaded">
            <GenericCrud
                :title="meta?.title || 'Manage Badge Records'"
                :dialogHeader="meta?.dialogHeader || 'Party Details'"
                :fields="fields"
                :service="badgeService"
                :messages="meta?.messages"
                :customButtons="[
                    {
                        label: 'Download Badge',
                        icon: 'pi pi-download',
                        severity: 'secondary',
                        onClick: (rowData) => openPrintBadge(rowData)
                    }
                ]"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Badge Preview & Download Modal Dialog -->
    <Dialog
      v-model:visible="showPrintPreview"
      modal
      header="Employee Badge Preview"
      :style="{ width: '420px' }"
    >
      <div v-if="selectedBadge" class="printable-badge surface-card p-4 border-round-xl shadow-3 text-center border-1 surface-border">
        <div class="text-xs font-bold text-primary uppercase tracking-widest mb-1">SAAVY ENTERPRISE</div>
        <div class="text-xs text-500 mb-3">OFFICIAL ACCESS IDENTIFICATION</div>

        <!-- Avatar Image / Fallback -->
        <div class="flex justify-content-center mb-3">
          <div
            v-if="getAvatarSrc(selectedBadge)"
            class="border-circle overflow-hidden shadow-2 surface-border border-2 bg-white flex justify-content-center align-items-center"
            style="width: 5.5rem; height: 5.5rem;"
          >
            <img
              :src="getAvatarSrc(selectedBadge)"
              alt="Avatar"
              style="width: 100%; height: 100%; object-fit: cover; display: block;"
            />
          </div>
          <Avatar
            v-else
            :label="selectedBadge.employeeName ? selectedBadge.employeeName.charAt(0).toUpperCase() : 'E'"
            size="xlarge"
            shape="circle"
            class="bg-primary-100 text-primary-700 font-bold shadow-2"
            style="width: 5.5rem; height: 5.5rem; font-size: 2.25rem;"
          />
        </div>

        <div class="text-xl font-bold text-900">{{ selectedBadge.employeeName }}</div>
        <div class="text-sm font-semibold text-600 mb-1">{{ selectedBadge.jobTitle }}</div>
        <div class="text-xs text-500 mb-3">{{ selectedBadge.department }} &bull; ID: {{ selectedBadge.employeeId }}</div>

        <div class="flex justify-content-center mb-3">
          <div class="bg-white p-2 border-round shadow-1">
            <GenericQRCode :value="selectedBadge.qrToken" :size="150" />
          </div>
        </div>

        <div class="text-xs text-400">Scan at any authorized Bundy Clock Kiosk terminal.</div>
      </div>

      <template #footer>
        <div class="flex justify-content-end gap-2">
          <Button label="Close" class="p-button-text" @click="showPrintPreview = false" />
          <Button label="Print" icon="pi pi-print" class="p-button-outlined p-button-secondary" @click="printCurrentBadge" />
          <Button label="Download PNG" icon="pi pi-download" class="p-button-primary" @click="downloadBadge" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped lang="scss">
.badge-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  }
}

@media print {
  body * {
    visibility: hidden;
  }
  .printable-badge, .printable-badge * {
    visibility: visible;
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
  }
  .printable-badge {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 320px;
    box-shadow: none !important;
    border: 1px solid #ccc !important;
  }
}
</style>
