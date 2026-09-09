<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericCrud from '@core/components/GenericCrud.vue';
import ProgressSpinner from 'primevue/progressspinner';

const partyService = createJpaService('parties');

const meta = ref(null);
const fields = ref([]);
const isDataLoaded = ref(false);

onMounted(async () => {
    try {
        meta.value = await partyService.getMasterMeta();
        fields.value = meta.value?.fields || [];
        isDataLoaded.value = true;
    } catch (e) {
        console.error('Failed to load party metadata:', e);
        isDataLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericCrud
            :title="meta?.title || 'Party Management'"
            :dialogHeader="meta?.dialogHeader || 'Party Details'"
            :fields="fields"
            :service="partyService"
            :messages="meta?.messages"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>

<style scoped></style>
