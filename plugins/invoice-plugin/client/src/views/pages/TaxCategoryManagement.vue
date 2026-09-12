<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericCrud from '@core/components/GenericCrud.vue';
import ProgressSpinner from 'primevue/progressspinner';

const taxCategoryService = createJpaService('tax-categories');
const meta = ref({});
const isLoaded = ref(false);

onMounted(async () => {
    try {
        meta.value = await taxCategoryService.getMasterMeta();
        isLoaded.value = true;
    } catch (e) {
        console.error('Failed to load tax categories meta:', e);
        isLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isLoaded">
        <GenericCrud
            title="Tax Categories & Buckets"
            subtitle="Manage BIR Schedule line item buckets and their direct mappings to P&L accounts"
            :meta="meta"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>
