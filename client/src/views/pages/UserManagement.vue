<script setup>
import GenericMasterDetail from '@/components/GenericMasterDetail.vue';
import userService, { createEmptyUser } from '@/service/JPAUserService';
import roleService, { createEmptyRole } from '@/service/JPARoleService';
import { onMounted, ref } from 'vue';

const isDataLoaded = ref(false);

const masterUser = ref({
    title: 'Users',
    dialogHeader: 'User Details',
    dataKey: 'id',
    fields: [],
    service: userService,
    createEmptyRecord: createEmptyUser,
    messages: {
        created: 'User successfully created',
        updated: 'User successfully updated',
        deleted: 'User successfully deleted',
        deletedMany: 'Selected users deleted'
    }
});

const detailsUser = ref([
    {
        key: 'roles',
        title: 'Assigned Roles',
        dialogHeader: 'Role',
        dataKey: 'id',
        parentField: 'user',
        fields: [],
        service: {
            findByParent: (parentField, user) => roleService.findByParent('user', user),
            findByParentWithPage: (parentField, user, payload) => roleService.findByParentWithPage('user', user, payload),
            createOrUpdate: async (role, user) => {
                if (role.id) {
                    await userService.assignRole(user.id, role.id);
                    return role;
                } else {
                    const savedRole = await roleService.createOrUpdate(role);
                    await userService.assignRole(user.id, savedRole.id);
                    return savedRole;
                }
            },
            delete: async (roleId, user) => {
                await userService.removeRole(user.id, roleId);
                return { id: roleId };
            }
        },
        createEmptyRecord: () => createEmptyRole(),
        messages: {
            created: 'Role assigned to user',
            updated: 'Role updated',
            deleted: 'Role removed from user',
            deletedMany: 'Roles removed from user'
        }
    }
]);

onMounted(async () => {
    try {
        const [uFields, rFields] = await Promise.all([
            userService.getFields(),
            roleService.getFields()
        ]);
        masterUser.value.fields = uFields;
        detailsUser.value[0].fields = rFields;
        isDataLoaded.value = true;
    } catch (error) {
        console.error('Failed to load metadata fields:', error);
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
