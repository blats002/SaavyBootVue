<script setup>
import { onMounted, ref, onBeforeMount, watch } from 'vue';
import productService from '../../service/JPAProductService';
import { createEmptyProduct } from '../../service/JPAProductService';

const master = ref({});
const details = ref([]);

onMounted(async () => {
    master.value = await productService.getMasterMeta();
    details.value = await productService.getDetailMeta();
    console.log(master.value);
    console.log(details.value);
    console.log(master.value.title);
});

// Watch for changes in master and details
watch([master, details], ([newMaster, newDetails]) => {
    if (newMaster != undefined && newDetails != undefined) {
        isDataLoaded.value = true;
    } else {
        isDataLoaded.value = false;
    }
});

const isDataLoaded = ref(false);
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericMasterDetail title="Stocks" subtitle="" :master="master" :details="details" />
    </div>
    <div v-else>Loading...</div>
</template>
