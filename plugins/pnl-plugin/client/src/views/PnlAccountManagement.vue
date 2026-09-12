<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericCrud from '@core/components/GenericCrud.vue';
import GenericFileUploadDialog from '@core/components/GenericFileUploadDialog.vue';
import ProgressSpinner from 'primevue/progressspinner';
import PnlReportService from '../service/PnlReportService';
import { useToast } from 'primevue/usetoast';

const toast = useToast();
const accountService = createJpaService('pnl-accounts');

const meta = ref(null);
const fields = ref([]);
const isDataLoaded = ref(false);
const refreshCounter = ref(0);
const showImportDialog = ref(false);

const downloadTemplate = async () => {
    try {
        await PnlReportService.downloadAccountTemplate();
        toast.add({
            severity: 'info',
            summary: 'Template Downloaded',
            detail: 'Chart of Accounts CSV template ready for editing',
            life: 3000
        });
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: 'Download Error',
            detail: 'Failed to download account template',
            life: 3000
        });
    }
};

const handleAccountCsvUpload = (file) => {
    return PnlReportService.uploadAccountCsv(file);
};

const panelCustomButtons = [
    {
        key: 'template',
        label: 'Get CSV Template',
        icon: 'pi pi-download',
        class: 'p-button-outlined p-button-info mr-2',
        onClick: () => downloadTemplate()
    },
    {
        key: 'import',
        label: 'Import CSV',
        icon: 'pi pi-upload',
        class: 'p-button-success mr-2',
        onClick: () => {
            showImportDialog.value = true;
        }
    }
];

const onImportSuccess = () => {
    refreshCounter.value++;
};

onMounted(async () => {
    try {
        meta.value = await accountService.getMasterMeta();
        fields.value = meta.value?.fields || [];
        isDataLoaded.value = true;
    } catch (e) {
        console.error('Failed to load P&L Account metadata:', e);
        isDataLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericCrud
            :title="meta?.title || 'Chart of Accounts'"
            :dialogHeader="meta?.dialogHeader || 'Account Details'"
            :fields="fields"
            :service="accountService"
            :messages="meta?.messages"
            :role="['ROLE_ADMIN', 'ROLE_MANAGER']"
            :refreshKey="refreshCounter"
            :panelCustomButtons="panelCustomButtons"
        />

        <!-- CSV Account Import Dialog using Core GenericFileUploadDialog -->
        <GenericFileUploadDialog
            v-model:visible="showImportDialog"
            title="Import Chart of Accounts (CSV)"
            description="Bulk upload or update Chart of Accounts. Existing account codes will be updated and new codes will be added."
            :showTemplate="true"
            templateTitle="Chart of Accounts Template (CSV)"
            fileHint="Columns: code, name, category, subcategory, sort_order, description"
            uploadButtonLabel="Import Accounts"
            :onDownloadTemplate="downloadTemplate"
            :onUpload="handleAccountCsvUpload"
            :role="['ROLE_ADMIN', 'ROLE_MANAGER']"
            @success="onImportSuccess"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>

<style scoped></style>
