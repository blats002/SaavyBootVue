<script setup>
import { QrcodeStream } from 'vue-qrcode-reader';

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
  <div class="qr-reader-page">
    <QrcodeStream @detect="onDetect" @error="onError" />
  </div>
</template>

<style scoped lang="scss">
.qr-reader-page {
  width: 320px;
  max-width: 100%;
}
</style>