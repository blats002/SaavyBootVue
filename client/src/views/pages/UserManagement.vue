<script setup>
import GenericMasterDetail from '../../components/GenericMasterDetail.vue';
import userService, { createEmptyUser } from '../../service/JPAUserService';
// import roleService, { createEmptyRole } from '../../service/JPARoleService';
import { onMounted, ref } from 'vue';

const isDataLoaded = ref(false);

const masterUser = ref({});
const detailsUser = ref([]);

// const masterUser = ref({
//     title: 'Users',
//     dialogHeader: 'User Details',
//     dataKey: 'id',
//     fields: [],
//     service: userService,
//     createEmptyRecord: createEmptyUser,
//     messages: {
//         created: 'User successfully created',
//         updated: 'User successfully updated',
//         deleted: 'User successfully deleted',
//         deletedMany: 'Selected users deleted'
//     }
// });
//
// const detailsUser = ref([
//     {
//         key: 'roles',
//         title: 'Assigned Roles',
//         dialogHeader: 'Role',
//         dataKey: 'id',
//         parentField: 'user',
//         fields: [],
//         service: roleService,
//         createEmptyRecord: () => createEmptyRole(),
//         messages: {
//             created: 'Role assigned to user',
//             updated: 'Role updated',
//             deleted: 'Role removed from user',
//             deletedMany: 'Roles removed from user'
//         }
//     }
// ]);

onMounted(async () => {
  try {
    masterUser.value = await userService.getMasterMeta();
    detailsUser.value = await userService.getDetailMeta();
    isDataLoaded.value = true;
  } catch (e) {
    console.error('Failed to load user metadata:', e);
    isDataLoaded.value = true;
  }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <GenericMasterDetail
            title="User Management"
            subtitle=""
            layout="modal"
            :master="masterUser"
            :details="detailsUser"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>
