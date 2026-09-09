<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useLayout } from './composables/layout';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';

import AuthService from '../service/AuthService';

const { layoutConfig, onMenuToggle, logoUrl } = useLayout();
const toast = useToast();
const router = useRouter();

const outsideClickListener = ref(null);
const topbarMenuActive = ref(false);

const profileMenu = ref(null);
const profileMenuItems = ref([
    {
        label: 'Change Password',
        icon: 'pi pi-key',
        command: () => {
            openChangePasswordDialog();
        }
    },
    {
        separator: true
    },
    {
        label: 'Sign Out',
        icon: 'pi pi-sign-out',
        command: () => {
            onLogout();
        }
    }
]);

const changePasswordDialog = ref(false);
const isSubmitting = ref(false);
const passwordForm = ref({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
});
const passwordErrors = ref({});

const toggleProfileMenu = (event) => {
    profileMenu.value.toggle(event);
};

const openChangePasswordDialog = () => {
    passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    };
    passwordErrors.value = {};
    changePasswordDialog.value = true;
    topbarMenuActive.value = false;
};

const closeChangePasswordDialog = () => {
    changePasswordDialog.value = false;
    passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    };
    passwordErrors.value = {};
};

const validatePasswordForm = () => {
    const errors = {};
    if (!passwordForm.value.currentPassword) {
        errors.currentPassword = 'Current password is required';
    }
    if (!passwordForm.value.newPassword) {
        errors.newPassword = 'New password is required';
    } else if (passwordForm.value.newPassword.length < 6) {
        errors.newPassword = 'New password must be at least 6 characters';
    }
    if (!passwordForm.value.confirmPassword) {
        errors.confirmPassword = 'Confirm password is required';
    } else if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        errors.confirmPassword = 'Passwords do not match';
    }

    passwordErrors.value = errors;
    return Object.keys(errors).length === 0;
};

const handleChangePassword = async () => {
    if (!validatePasswordForm()) {
        return;
    }

    isSubmitting.value = true;
    try {
        const response = await AuthService.changePassword(passwordForm.value);
        toast.add({
            severity: 'success',
            summary: 'Success',
            detail: response?.message || 'Password changed successfully',
            life: 4000
        });
        closeChangePasswordDialog();
    } catch (error) {
        const errorMsg = error.response?.data?.message || error.message || 'Failed to change password';
        toast.add({
            severity: 'error',
            summary: 'Error',
            detail: errorMsg,
            life: 5000
        });
    } finally {
        isSubmitting.value = false;
    }
};

const onLogout = () => {
    AuthService.logout();
    topbarMenuActive.value = false;
    router.push('/auth/login');
};

onMounted(() => {
    bindOutsideClickListener();
});

onBeforeUnmount(() => {
    unbindOutsideClickListener();
});

const onTopBarMenuButton = () => {
    topbarMenuActive.value = !topbarMenuActive.value;
};

const topbarMenuClasses = computed(() => {
    return {
        'layout-topbar-menu-mobile-active': topbarMenuActive.value
    };
});

const bindOutsideClickListener = () => {
    if (!outsideClickListener.value) {
        outsideClickListener.value = (event) => {
            if (isOutsideClicked(event)) {
                topbarMenuActive.value = false;
            }
        };
        document.addEventListener('click', outsideClickListener.value);
    }
};

const unbindOutsideClickListener = () => {
    if (outsideClickListener.value) {
        document.removeEventListener('click', outsideClickListener);
        outsideClickListener.value = null;
    }
};

const isOutsideClicked = (event) => {
    if (!topbarMenuActive.value) return;

    const sidebarEl = document.querySelector('.layout-topbar-menu');
    const topbarEl = document.querySelector('.layout-topbar-menu-button');

    return !(sidebarEl?.isSameNode(event.target) || sidebarEl?.contains(event.target) || topbarEl?.isSameNode(event.target) || topbarEl?.contains(event.target));
};
</script>

<template>
    <div class="layout-topbar">
        <router-link to="/" class="layout-topbar-logo">
            <img :src="logoUrl" alt="logo" />
            <span>Saavy</span>
        </router-link>

        <button class="p-link layout-menu-button layout-topbar-button" @click="onMenuToggle()">
            <i class="pi pi-bars"></i>
        </button>

        <button class="p-link layout-topbar-menu-button layout-topbar-button" @click="onTopBarMenuButton()">
            <i class="pi pi-ellipsis-v"></i>
        </button>

        <div class="layout-topbar-menu" :class="topbarMenuClasses">
            <button class="p-link layout-topbar-button" @click="toggleProfileMenu($event)" title="Profile">
                <i class="pi pi-user"></i>
                <span>Profile</span>
            </button>
            <Menu ref="profileMenu" :model="profileMenuItems" :popup="true" />

<!--            <button @click="onLogout()" class="p-link layout-topbar-button" title="Sign Out">-->
<!--                <i class="pi pi-sign-out"></i>-->
<!--                <span>Sign Out</span>-->
<!--            </button>-->
        </div>

        <!-- Change Password Dialog -->
        <Dialog
            v-model:visible="changePasswordDialog"
            header="Change Password"
            :modal="true"
            :style="{ width: '420px' }"
            class="p-fluid"
        >
            <div class="flex flex-column gap-3 pt-2">
                <div class="field mb-0">
                    <label for="currentPassword" class="font-medium text-900 block mb-2">Current Password</label>
                    <Password
                        id="currentPassword"
                        v-model="passwordForm.currentPassword"
                        :toggleMask="true"
                        :feedback="false"
                        placeholder="Enter current password"
                        class="w-full"
                        inputClass="w-full"
                        :class="{ 'p-invalid': passwordErrors.currentPassword }"
                    />
                    <small v-if="passwordErrors.currentPassword" class="p-error block mt-1">
                        {{ passwordErrors.currentPassword }}
                    </small>
                </div>

                <div class="field mb-0">
                    <label for="newPassword" class="font-medium text-900 block mb-2">New Password</label>
                    <Password
                        id="newPassword"
                        v-model="passwordForm.newPassword"
                        :toggleMask="true"
                        placeholder="Enter new password"
                        class="w-full"
                        inputClass="w-full"
                        :class="{ 'p-invalid': passwordErrors.newPassword }"
                    />
                    <small v-if="passwordErrors.newPassword" class="p-error block mt-1">
                        {{ passwordErrors.newPassword }}
                    </small>
                </div>

                <div class="field mb-0">
                    <label for="confirmPassword" class="font-medium text-900 block mb-2">Confirm Password</label>
                    <Password
                        id="confirmPassword"
                        v-model="passwordForm.confirmPassword"
                        :toggleMask="true"
                        :feedback="false"
                        placeholder="Confirm new password"
                        class="w-full"
                        inputClass="w-full"
                        :class="{ 'p-invalid': passwordErrors.confirmPassword }"
                    />
                    <small v-if="passwordErrors.confirmPassword" class="p-error block mt-1">
                        {{ passwordErrors.confirmPassword }}
                    </small>
                </div>
            </div>

            <template #footer>
                <Button label="Cancel" icon="pi pi-times" class="p-button-text" @click="closeChangePasswordDialog" />
                <Button
                    label="Change Password"
                    icon="pi pi-check"
                    class="p-button-primary"
                    :loading="isSubmitting"
                    @click="handleChangePassword"
                />
            </template>
        </Dialog>
    </div>
</template>

<style lang="scss" scoped></style>

