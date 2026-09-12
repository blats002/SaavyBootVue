<script setup>
import { ref, computed } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import Message from 'primevue/message';
import ProgressBar from 'primevue/progressbar';
import AuthService from '../service/AuthService';

const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    },
    title: {
        type: String,
        default: 'Import File'
    },
    description: {
        type: String,
        default: ''
    },
    accept: {
        type: String,
        default: '.csv'
    },
    fileHint: {
        type: String,
        default: 'Supports UTF-8 CSV files'
    },
    showTemplate: {
        type: Boolean,
        default: false
    },
    templateTitle: {
        type: String,
        default: 'Sample Template (CSV)'
    },
    templateButtonLabel: {
        type: String,
        default: 'Download Template'
    },
    templateButtonIcon: {
        type: String,
        default: 'pi pi-download'
    },
    uploadButtonLabel: {
        type: String,
        default: 'Process & Upload'
    },
    uploadButtonIcon: {
        type: String,
        default: 'pi pi-check'
    },
    onDownloadTemplate: {
        type: Function,
        default: null
    },
    onUpload: {
        type: Function,
        required: true
    },
    width: {
        type: String,
        default: '580px'
    },
    role: {
        type: [String, Array],
        default: null
    }
});

const emit = defineEmits(['update:visible', 'success', 'error', 'close']);

const isRoleAuthorized = computed(() => {
    if (!props.role) return true;
    return AuthService.hasRole(props.role);
});

const selectedFile = ref(null);
const fileInput = ref(null);
const isUploading = ref(false);
const resultMessage = ref(null);
const errorList = ref([]);
const warningList = ref([]);

const triggerFileSelect = () => {
    if (fileInput.value) {
        fileInput.value.click();
    }
};

const handleFileChange = (e) => {
    const files = e.target.files;
    if (files && files.length > 0) {
        selectedFile.value = files[0];
        resultMessage.value = null;
        errorList.value = [];
        warningList.value = [];
    }
};

const handleDownloadTemplate = async () => {
    if (typeof props.onDownloadTemplate === 'function') {
        try {
            await props.onDownloadTemplate();
        } catch (e) {
            console.error('Failed to download template:', e);
        }
    }
};

const handleUpload = async () => {
    if (!selectedFile.value || typeof props.onUpload !== 'function') return;
    isUploading.value = true;
    resultMessage.value = null;
    errorList.value = [];
    warningList.value = [];

    try {
        const res = await props.onUpload(selectedFile.value);
        if (res && res.success !== false) {
            resultMessage.value = {
                severity: 'success',
                summary: 'Completed',
                detail: res.message || res.detail || `Successfully processed ${res.totalProcessed ?? ''} record(s).`
            };
            if (res.warnings && Array.isArray(res.warnings)) {
                warningList.value = res.warnings;
            }
            emit('success', res);
        } else {
            resultMessage.value = {
                severity: 'error',
                summary: 'Upload Failed',
                detail: res?.message || 'Some errors occurred during processing.'
            };
            errorList.value = res?.errors || [];
            emit('error', res);
        }
    } catch (e) {
        const msg = e.response?.data?.errors?.join(', ') || e.response?.data?.message || e.message;
        resultMessage.value = {
            severity: 'error',
            summary: 'Upload Error',
            detail: msg || 'An unexpected error occurred.'
        };
        emit('error', e);
    } finally {
        isUploading.value = false;
    }
};

const closeDialog = () => {
    emit('update:visible', false);
    emit('close');
    selectedFile.value = null;
    resultMessage.value = null;
    errorList.value = [];
    warningList.value = [];
};
</script>

<template>
    <Dialog
        v-if="isRoleAuthorized"
        :visible="visible"
        :header="title"
        :modal="true"
        :style="{ width }"
        :closable="!isUploading"
        @update:visible="emit('update:visible', $event)"
    >
        <div class="flex flex-column gap-3">
            <p v-if="description" class="text-secondary text-sm m-0">
                {{ description }}
            </p>

            <!-- Template Download Banner -->
            <div
                v-if="showTemplate || onDownloadTemplate"
                class="flex justify-content-between align-items-center p-3 bg-blue-50 border-round border-1 border-blue-200"
            >
                <div class="flex align-items-center gap-2">
                    <i class="pi pi-file-excel text-blue-600 text-xl" />
                    <span class="text-sm font-medium text-blue-900">{{ templateTitle }}</span>
                </div>
                <Button
                    :label="templateButtonLabel"
                    :icon="templateButtonIcon"
                    size="small"
                    outlined
                    severity="info"
                    @click="handleDownloadTemplate"
                />
            </div>

            <!-- Drag & Drop Zone -->
            <div
                class="border-2 border-dashed border-300 border-round p-5 text-center cursor-pointer hover:border-primary transition-colors transition-duration-150"
                @click="triggerFileSelect"
            >
                <input ref="fileInput" type="file" :accept="accept" class="hidden" @change="handleFileChange" />
                <i class="pi pi-cloud-upload text-4xl text-500 mb-2" />
                <div v-if="!selectedFile">
                    <div class="font-medium text-900">Click to browse or drop file here</div>
                    <small class="text-500">{{ fileHint }}</small>
                </div>
                <div v-else class="text-primary font-bold">
                    <i class="pi pi-file mr-1" />
                    {{ selectedFile.name }} ({{ (selectedFile.size / 1024).toFixed(1) }} KB)
                </div>
            </div>

            <ProgressBar v-if="isUploading" mode="indeterminate" style="height: 6px" />

            <Message v-if="resultMessage" :severity="resultMessage.severity" :closable="false">
                <span class="font-bold">{{ resultMessage.summary }}:</span> {{ resultMessage.detail }}
            </Message>

            <div v-if="warningList.length > 0" class="p-2 bg-yellow-50 border-round border-1 border-yellow-200 text-xs text-yellow-900 max-h-8rem overflow-y-auto">
                <div class="font-bold mb-1">Notice:</div>
                <ul class="m-0 pl-3">
                    <li v-for="(warn, idx) in warningList" :key="idx">{{ warn }}</li>
                </ul>
            </div>

            <div v-if="errorList.length > 0" class="p-2 bg-red-50 border-round border-1 border-red-200 text-xs text-red-900 max-h-8rem overflow-y-auto">
                <div class="font-bold mb-1">Errors:</div>
                <ul class="m-0 pl-3">
                    <li v-for="(err, idx) in errorList" :key="idx">{{ err }}</li>
                </ul>
            </div>
        </div>

        <template #footer>
            <Button label="Cancel" icon="pi pi-times" text :disabled="isUploading" @click="closeDialog" />
            <Button
                :label="uploadButtonLabel"
                :icon="uploadButtonIcon"
                :loading="isUploading"
                :disabled="!selectedFile || isUploading"
                @click="handleUpload"
            />
        </template>
    </Dialog>
</template>

<style scoped></style>
