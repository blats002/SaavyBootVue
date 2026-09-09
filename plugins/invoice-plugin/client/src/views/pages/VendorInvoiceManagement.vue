<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericMasterDetail from '@core/components/GenericMasterDetail.vue';
import ProgressSpinner from 'primevue/progressspinner';

const vendorInvoiceService = createJpaService('vendor-invoices');

const masterVendorInvoice = ref({});
const detailsVendorInvoice = ref([]);
const isDataLoaded = ref(false);

onMounted(async () => {
    try {
        masterVendorInvoice.value = await vendorInvoiceService.getMasterMeta();
        detailsVendorInvoice.value = await vendorInvoiceService.getDetailMeta();
        isDataLoaded.value = true;
    } catch (e) {
        console.error('Failed to load vendor invoice metadata:', e);
        isDataLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericMasterDetail
            title="Vendor Invoice Management"
            subtitle=""
            layout="modal"
            :master="masterVendorInvoice"
            :details="detailsVendorInvoice"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>

<style scoped></style>
