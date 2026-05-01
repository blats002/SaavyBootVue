<script setup>
import GenericForm from './GenericForm.vue';
const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    },
    type: {
        type: String,
        default: 'message',
        validator: (value) => ['form', 'confirm', 'message'].includes(value)
    },
    header: {
        type: String,
        default: ''
    },
    message: {
        type: String,
        default: ''
    },
    icon: {
        type: String,
        default: 'pi pi-info-circle'
    },
    width: {
        type: String,
        default: '450px'
    },
    fields: {
        type: Array,
        default: () => []
    },
    modelValue: {
        type: Object,
        default: () => ({})
    },
    submitted: {
        type: Boolean,
        default: false
    },
    buttons: {
        type: Array,
        default: () => []
    },
    confirmLabel: {
        type: String,
        default: 'Yes'
    },
    cancelLabel: {
        type: String,
        default: 'No'
    },
    saveLabel: {
        type: String,
        default: 'Save'
    },
    closeLabel: {
        type: String,
        default: 'Close'
    }
});

const emit = defineEmits([
    'update:visible',
    'update:modelValue',
    'button-click',
    'confirm',
    'cancel',
    'save',
    'close'
]);

// const updateField = (fieldName, value) => {
//     emit('update:modelValue', {
//         ...props.modelValue,
//         [fieldName]: value
//     });
// };

// const getFieldOptions = (field) => {
//     return field.options || [];
// };

// const getOptionLabel = (field) => {
//     return field.optionLabel || 'label';
// };

// const getOptionValue = (field) => {
//     return field.optionValue || 'value';
// };

const handleButtonClick = (button) => {
    if (button.closesDialog) {
        emit('update:visible', false);
    }

    emit('button-click', button);
};

const getDefaultButtons = () => {
    if (props.type === 'form') {
        return [
            {
                key: 'cancel',
                label: props.cancelLabel,
                icon: 'pi pi-times',
                class: 'p-button-text',
                closesDialog: true
            },
            {
                key: 'save',
                label: props.saveLabel,
                icon: 'pi pi-check',
                class: 'p-button-text'
            }
        ];
    }

    if (props.type === 'confirm') {
        return [
            {
                key: 'cancel',
                label: props.cancelLabel,
                icon: 'pi pi-times',
                class: 'p-button-text',
                closesDialog: true
            },
            {
                key: 'confirm',
                label: props.confirmLabel,
                icon: 'pi pi-check',
                class: 'p-button-text'
            }
        ];
    }

    return [
        {
            key: 'close',
            label: props.closeLabel,
            icon: 'pi pi-check',
            class: 'p-button-text',
            closesDialog: true
        }
    ];
};

const getDialogButtons = () => {
    return props.buttons.length ? props.buttons : getDefaultButtons();
};

const closeDialog = () => {
    emit('update:visible', false);
    emit('close');
};

const cancelDialog = () => {
    emit('update:visible', false);
    emit('cancel');
};

const confirmDialog = () => {
    emit('confirm');
};

const saveDialog = () => {
    emit('save');
};

// const isEmptyValue = (value) => {
//     if (typeof value === 'string') {
//         return value.trim() === '';
//     }
//
//     return value === null || value === undefined;
// };

// const hasFieldError = (field) => {
//     return props.submitted && field.required && isEmptyValue(props.modelValue[field.name]);
// };

</script>

<template>
    <Dialog
        :visible="visible"
        :style="{ width }"
        :header="header"
        :modal="true"
        :class="{ 'p-fluid': type === 'form' }"
        @update:visible="emit('update:visible', $event)"
    >
      <GenericForm
          v-if="type === 'form'"
          :fields="fields"
          :modelValue="modelValue"
          :submitted="submitted"
          @update:modelValue="emit('update:modelValue', $event)"
      />

        <template v-else>
            <div class="flex align-items-center justify-content-center">
                <i :class="icon" class="mr-3 generic-dialog-icon" />
                <span>{{ message }}</span>
            </div>
        </template>

        <template #footer>
            <Button
                v-for="button in getDialogButtons()"
                :key="button.key"
                :label="button.label"
                :icon="button.icon"
                :class="button.class"
                :severity="button.severity"
                :disabled="button.disabled"
                @click="handleButtonClick(button)"
            />
        </template>
    </Dialog>
</template>

<style scoped lang="scss">
.generic-dialog-icon {
    font-size: 2rem;
}
</style>