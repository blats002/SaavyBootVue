<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericCrud from '@core/components/GenericCrud.vue';
import ProgressSpinner from 'primevue/progressspinner';

const ledgerService = createJpaService('pnl-ledger-entries');

const meta = ref(null);
const fields = ref([]);
const isDataLoaded = ref(false);

onMounted(async () => {
    try {
        meta.value = await ledgerService.getMasterMeta();
        fields.value = meta.value?.fields || [];
        isDataLoaded.value = true;
    } catch (e) {
        console.error('Failed to load P&L Ledger Entry metadata:', e);
        isDataLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericCrud
            :title="meta?.title || 'P&L Ledger Entries'"
            :dialogHeader="meta?.dialogHeader || 'Ledger Entry Details'"
            :fields="fields"
            :service="ledgerService"
            :messages="meta?.messages"
            :role="['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER']"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>

<style scoped></style>
