<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericCrud from '@core/components/GenericCrud.vue';
import ProgressSpinner from 'primevue/progressspinner';

const attendanceService = createJpaService('attendance-logs');

const meta = ref(null);
const fields = ref([]);
const isDataLoaded = ref(false);

onMounted(async () => {
  try {
    meta.value = await attendanceService.getMasterMeta();
    fields.value = meta.value?.fields || [];
    isDataLoaded.value = true;
  } catch (e) {
    console.error('Failed to load attendance logs metadata:', e);
    isDataLoaded.value = true;
  }
});
</script>

<template>
  <div v-if="isDataLoaded">
    <GenericCrud
      :title="meta?.title || 'Attendance Records & Timesheets'"
      :dialogHeader="meta?.dialogHeader || 'Attendance Details'"
      :fields="fields"
      :service="attendanceService"
      :messages="meta?.messages"
    />
  </div>
  <div v-else class="card flex justify-content-center p-5">
    <ProgressSpinner />
  </div>
</template>

<style scoped></style>
