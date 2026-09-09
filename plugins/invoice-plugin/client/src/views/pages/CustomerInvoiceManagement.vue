<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericMasterDetail from '@core/components/GenericMasterDetail.vue';
import ProgressSpinner from 'primevue/progressspinner';

const customerInvoiceService = createJpaService('customer-invoices');

const masterCustomerInvoice = ref({});
const detailsCustomerInvoice = ref([]);
const isDataLoaded = ref(false);

onMounted(async () => {
    try {
        masterCustomerInvoice.value = await customerInvoiceService.getMasterMeta();
        detailsCustomerInvoice.value = await customerInvoiceService.getDetailMeta();
        isDataLoaded.value = true;
    } catch (e) {
        console.error('Failed to load customer invoice metadata:', e);
        isDataLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericMasterDetail
            title="Customer Invoice Management"
            subtitle=""
            layout="modal"
            :master="masterCustomerInvoice"
            :details="detailsCustomerInvoice"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>

<style scoped></style>
