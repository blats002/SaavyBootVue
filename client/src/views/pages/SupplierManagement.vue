<script setup>
import GenericCrud from '../../components/GenericCrud.vue';
import supplierService, { createEmptySupplier } from '../../service/JPASupplierService';
import { onMounted, ref } from 'vue';

const fields = ref([]);

onMounted(async () => {
    fields.value = await supplierService.getFields();
});
</script>

<template>
    <GenericCrud
        dialogHeader="Test Details"
        dataKey="id"
        :fields="fields"
        :service="supplierService"
        :createEmptyRecord="createEmptySupplier"
        :messages="{
            created: 'Test Created',
            updated: 'Test Updated',
            deleted: 'Test Deleted',
            deletedMany: 'Tests Deleted'
        }"
    />
</template>
