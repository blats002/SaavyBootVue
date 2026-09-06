<script setup>
import GenericCrud from '../../components/GenericCrud.vue';
import roleService, { createEmptyRole } from '../../service/JPARoleService';
import { onMounted, ref } from 'vue';

const fields = ref([]);
const isDataLoaded = ref(false);

onMounted(async () => {
    try {
        fields.value = await roleService.getFields();
        isDataLoaded.value = true;
    } catch (error) {
        console.error('Failed to load role metadata fields:', error);
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericCrud
            title="Role Management"
            dialogHeader="Role Details"
            dataKey="id"
            :fields="fields"
            :service="roleService"
            :createEmptyRecord="createEmptyRole"
            :messages="{
                created: 'Role successfully created',
                updated: 'Role successfully updated',
                deleted: 'Role successfully deleted',
                deletedMany: 'Selected roles deleted'
            }"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>
