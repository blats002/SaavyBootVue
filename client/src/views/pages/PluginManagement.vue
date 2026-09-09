<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '../../service/JPAService';
import GenericCrud from '../../components/GenericCrud.vue';
import ProgressSpinner from 'primevue/progressspinner';
import { pluginState } from '../../plugins/pluginState';

const pluginService = createJpaService('plugins');
const pluginMeta = ref({});
const isDataLoaded = ref(false);

const handleRecordSaved = async () => {
    // Refresh reactive active plugin states across the application
    await pluginState.fetchActivePlugins();
};

onMounted(async () => {
    try {
        pluginMeta.value = await pluginService.getMasterMeta();
        isDataLoaded.value = true;
    } catch (e) {
        console.error('Failed to load plugin metadata:', e);
        isDataLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericCrud
            :title="pluginMeta.title || 'Plugin Management'"
            :dialogHeader="pluginMeta.dialogHeader || 'Plugin Details'"
            :fields="pluginMeta.fields || []"
            :service="pluginService"
            :messages="pluginMeta.messages || {}"
            :showDeleteButton="pluginMeta.deletable !== false"
            :deletableField="pluginMeta.deletableField || ''"
            role="ROLE_ADMIN"
            @record-saved="handleRecordSaved"
            @record-deleted="handleRecordSaved"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>

<style scoped></style>
