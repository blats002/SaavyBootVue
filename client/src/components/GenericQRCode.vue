<script setup>
import VueQrcode from 'vue-qrcode';

const props = defineProps({
  value: {
    type: String,
    required: true,
    default: ''
  },
  size: {
    type: Number,
    default: 180
  },
  margin: {
    type: Number,
    default: 2
  },
  color: {
    type: Object,
    default: () => ({
      dark: '#000000',
      light: '#ffffff'
    })
  },
  errorCorrectionLevel: {
    type: String,
    default: 'M'
  },
  type: {
    type: String,
    default: 'image/png'
  }
});
</script>

<template>
  <div class="generic-qrcode-wrapper flex justify-content-center align-items-center">
    <VueQrcode
      v-if="value"
      :value="value"
      :options="{
        width: size,
        margin: margin,
        color: color,
        errorCorrectionLevel: errorCorrectionLevel
      }"
      :type="type"
      class="border-round shadow-1"
    />
    <div v-else class="empty-qr p-3 text-color-secondary border-dashed border-1 surface-border border-round text-center">
      <i class="pi pi-qrcode text-3xl mb-2"></i>
      <div>No QR Value</div>
    </div>
  </div>
</template>

<style scoped>
.generic-qrcode-wrapper {
  display: inline-flex;
}
.empty-qr {
  width: 160px;
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>
