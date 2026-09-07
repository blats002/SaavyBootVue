<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericCrud from '@core/components/GenericCrud.vue';
import ProgressSpinner from 'primevue/progressspinner';

const sampleItemService = createJpaService('sample-items');

const sampleItemMeta = ref({});
const isDataLoaded = ref(false);

onMounted(async () => {
    try {
        sampleItemMeta.value = await sampleItemService.getMasterMeta();
        isDataLoaded.value = true;
    } catch (e) {
        console.error('Failed to load sample item metadata:', e);
        isDataLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericCrud
            :title="sampleItemMeta.title || 'Sample Items'"
            :dialogHeader="sampleItemMeta.dialogHeader || 'Sample Item Details'"
            :fields="sampleItemMeta.fields || []"
            :service="sampleItemService"
            :messages="sampleItemMeta.messages || {}"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>

<style scoped></style>
