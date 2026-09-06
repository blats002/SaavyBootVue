<script setup>
import { useLayout } from '../../../layout/composables/layout';
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import AuthService from '../../../service/AuthService';
import AppConfig from '../../../layout/AppConfig.vue';

const router = useRouter();
const route = useRoute();
const { layoutConfig } = useLayout();

const username = ref('');
const password = ref('');
const checked = ref(false);
const loading = ref(false);
const errorMessage = ref('');

const logoUrl = computed(() => {
    return `layout/images/${layoutConfig.darkTheme.value ? 'logo-white' : 'logo-dark'}.svg`;
});

const handleLogin = async () => {
    errorMessage.value = '';
    if (!username.value || !password.value) {
        errorMessage.value = 'Please enter both username and password.';
        return;
    }

    loading.value = true;
    try {
        await AuthService.login(username.value, password.value);
        const redirect = route.query.redirect || '/';
        router.push(redirect);
    } catch (error) {
        if (error.response && error.response.status === 401) {
            errorMessage.value = 'Invalid username or password.';
        } else {
            errorMessage.value = 'Login failed. Please check server connection and try again.';
        }
    } finally {
        loading.value = false;
    }
};
</script>

<template>
    <div class="surface-ground flex align-items-center justify-content-center min-h-screen min-w-screen overflow-hidden">
        <div class="flex flex-column align-items-center justify-content-center">
            <img :src="logoUrl" alt="Sakai logo" class="mb-5 w-6rem flex-shrink-0" />
            <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                <div class="w-full surface-card py-8 px-5 sm:px-8" style="border-radius: 53px">
                    <div class="text-center mb-5">
                        <img src="/demo/images/login/avatar.png" alt="Image" height="50" class="mb-3" />
                        <div class="text-900 text-3xl font-medium mb-3">Welcome Back!</div>
                        <span class="text-600 font-medium">Sign in to Invoice Management System</span>
                    </div>

                    <div v-if="errorMessage" class="p-message p-message-error mb-4">
                        <div class="p-message-wrapper p-3 text-red-600 font-medium">
                            <i class="pi pi-exclamation-circle mr-2"></i>
                            {{ errorMessage }}
                        </div>
                    </div>

                    <form @submit.prevent="handleLogin">
                        <label for="username" class="block text-900 text-xl font-medium mb-2">Username</label>
                        <InputText id="username" type="text" placeholder="Username" class="w-full md:w-30rem mb-5" style="padding: 1rem" v-model="username" />

                        <label for="password1" class="block text-900 font-medium text-xl mb-2">Password</label>
                        <Password id="password1" v-model="password" placeholder="Password" :toggleMask="true" :feedback="false" class="w-full mb-3" inputClass="w-full" :inputStyle="{ padding: '1rem' }"></Password>

                        <div class="flex align-items-center justify-content-between mb-5 gap-5">
                            <div class="flex align-items-center">
                                <Checkbox v-model="checked" id="rememberme1" binary class="mr-2"></Checkbox>
                                <label for="rememberme1">Remember me</label>
                            </div>
                        </div>
                        <Button type="submit" label="Sign In" :loading="loading" class="w-full p-3 text-xl"></Button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <AppConfig simple />
</template>

<style scoped>
.pi-eye {
    transform: scale(1.6);
    margin-right: 1rem;
}

.pi-eye-slash {
    transform: scale(1.6);
    margin-right: 1rem;
}
</style>
