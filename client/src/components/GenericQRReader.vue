<script setup>
import { computed } from 'vue';
import { QrcodeStream } from 'vue-qrcode-reader';
import AuthService from '@/service/AuthService';

const props = defineProps({
  role: {
    type: [String, Array],
    default: null
  }
});

const isRoleAuthorized = computed(() => {
  if (!props.role) return true;
  return AuthService.hasRole(props.role);
});

const onDetect = (detectedCodes) => {
  const firstCode = detectedCodes[0];

  if (firstCode) {
    console.log(firstCode.rawValue);
  }
};

const onError = (error) => {
  console.error('QR scanner error:', error);
};
</script>

<template>
  <div v-if="isRoleAuthorized" class="qr-reader-page">
    <QrcodeStream @detect="onDetect" @error="onError" />
  </div>
</template>

<style scoped lang="scss">
.qr-reader-page {
  width: 320px;
  max-width: 100%;
}
</style>