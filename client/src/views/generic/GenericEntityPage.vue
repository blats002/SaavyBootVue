<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '../../service/JPAService';
import ProgressSpinner from 'primevue/progressspinner';
import GenericCrud from '../../components/GenericCrud.vue';

const props = defineProps({
    endpoint: {
        type: String,
        required: true
    },
    dialogHeader: {
        type: String,
        default: 'Details'
    },
    dataKey: {
        type: String,
        default: 'id'
    }
});

const isDataLoaded = ref(false);
const fields = ref([]);
const service = ref(null);

onMounted(async () => {
    try {
        service.value = createJpaService(props.endpoint);
        fields.value = await service.value.getFields();
        isDataLoaded.value = true;
    } catch (e) {
        console.error('Failed loading entity fields:', e);
        isDataLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericCrud
            :dialogHeader="dialogHeader"
            :dataKey="dataKey"
            :fields="fields"
            :service="service"
            :createEmptyRecord="() => ({})"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>

<style scoped></style>
