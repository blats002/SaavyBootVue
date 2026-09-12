<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericCrud from '@core/components/GenericCrud.vue';
import ProgressSpinner from 'primevue/progressspinner';

const vendorRuleService = createJpaService('vendor-rules');
const meta = ref({});
const isLoaded = ref(false);

onMounted(async () => {
    try {
        meta.value = await vendorRuleService.getMasterMeta();
        isLoaded.value = true;
    } catch (e) {
        console.error('Failed to load vendor rules meta:', e);
        isLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isLoaded">
        <GenericCrud
            title="Vendor Smart Routing Rules"
            subtitle="Configure pattern matching rules to automatically classify vendor transactions into tax buckets"
            :meta="meta"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>
