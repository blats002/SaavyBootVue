<script setup>
import { ref, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import Calendar from 'primevue/calendar';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import AuthService from '../service/AuthService';

import FileUpload from 'primevue/fileupload';
import Image from 'primevue/image';

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { FilterMatchMode } from 'primevue/api';
import GenericCrud from './GenericCrud.vue';

const props = defineProps({
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
    role: {
        type: [String, Array],
        default: null
    }
});

const isRoleAuthorized = computed(() => {
    if (!props.role) return true;
    return AuthService.hasRole(props.role);
});

const emit = defineEmits(['update:modelValue']);

const relationshipDialogVisible = ref(false);
const relationshipDialogField = ref(null);
const relationshipDialogOptions = ref([]);
const relationshipDialogSelection = ref(null);
const relationshipDialogLoading = ref(false);
const relationshipDialogFilters = ref({
    global: {
        value: null,
        matchMode: FilterMatchMode.CONTAINS
    }
});

const updateField = (fieldName, value) => {
    emit('update:modelValue', {
        ...props.modelValue,
        [fieldName]: value
    });
};

const getFieldOptions = (field) => {
    return field.options || [];
};

const getOptionLabel = (field) => {
    return field.optionLabel || 'label';
};

const getOptionValue = (field) => {
    return field.optionValue || 'value';
};

const isManyToOneRelationship = (field) => {
    return field.type === 'manyToOne';
};

const isOneToManyRelationship = (field) => {
    return field.type === 'oneToMany';
};

const isSelectField = (field) => {
    return field.type === 'enum' || field.type === 'dropdown';
};

const getRelationshipOptionValue = (field) => {
    return field.optionValue || 'id';
};

const getRelationshipOptionLabel = (field) => {
    return field.optionLabel || 'name';
};

const getRelationshipDisplayValue = (field) => {
    const value = props.modelValue[field.name];

    if (value === '' || value === null || value === undefined) {
        return '';
    }

    if (typeof field.displayTemplate === 'function') {
        return field.displayTemplate(value, props.modelValue, field);
    }

    if (value.displayValue !== null && value.displayValue !== undefined && value.displayValue !== '') {
        return value.displayValue;
    }

    const optionLabel = getRelationshipOptionLabel(field);

    if (typeof value === 'object') {
        return value[optionLabel] || value.name || value.id || '';
    }

    return value;
};

const getRelationshipColumns = (field) => {
    if (field.selectionColumns?.length) {
        return field.selectionColumns;
    }

    return [
        {
            field: getRelationshipOptionValue(field),
            header: 'ID'
        },
        {
            field: getRelationshipOptionLabel(field),
            header: field.label
        }
    ];
};

const toast = useToast();

const getErrorMessage = (error, defaultMsg = 'An unexpected error occurred') => {
    if (!error) return defaultMsg;
    if (typeof error === 'string') return error;
    if (error.response?.data) {
        const data = error.response.data;
        if (typeof data === 'string') return data;
        if (data.message) return data.message;
        if (data.error) return data.error;
        if (Array.isArray(data.errors) && data.errors.length > 0) {
            return data.errors.map(e => e.defaultMessage || e.message || JSON.stringify(e)).join(', ');
        }
    }
    return error.message || defaultMsg;
};

const openRelationshipDialog = async (field) => {
    relationshipDialogField.value = field;
    relationshipDialogVisible.value = true;
    relationshipDialogSelection.value = null;
    relationshipDialogLoading.value = true;

    try {
        if (field.loadOptions) {
            relationshipDialogOptions.value = await field.loadOptions({
                record: props.modelValue,
                field
            });
        } else if (field.service?.findAll) {
            relationshipDialogOptions.value = await field.service.findAll();
        } else {
            relationshipDialogOptions.value = field.options || [];
        }
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: 'Error Loading Options',
            detail: getErrorMessage(error, 'Failed to load options from server'),
            life: 5000
        });
    } finally {
        relationshipDialogLoading.value = false;
    }
};

const rowSelectRelationshipRecord = (field) => {
    relationshipDialogSelection.value = field;
};

const selectRelationshipRecord = () => {
    const field = relationshipDialogField.value;

    if (!field || !relationshipDialogSelection.value) {
        return;
    }

    updateField(field.name, relationshipDialogSelection.value);

    relationshipDialogVisible.value = false;
    relationshipDialogSelection.value = null;
};

const clearRelationshipRecord = (field) => {
    updateField(field.name, null);
};

const isBlankValue = (value) => {
    return value === null || value === undefined || value === '';
};

const isFieldHidden = (field) => {
    if (!isBlankValue(field.hidden)) {
        return field.hidden;
    }

    return field.editable === false;
};

const isEmptyValue = (value) => {
    if (typeof value === 'string') {
        return value.trim() === '';
    }

    return value === null || value === undefined;
};

const getFieldValue = (field) => {
    return props.modelValue[field.name];
};

const normalizeFieldValue = (field, value) => {
    if (value === null || value === undefined || value === '') {
        return value;
    }

    const optionValue = getOptionValue(field);

    if (optionValue && typeof value === 'object') {
        return value[optionValue];
    }

    return value;
};

const hasFieldError = (field) => {
    return props.submitted && field.required && isEmptyValue(props.modelValue[field.name]);
};


const onImageUpload = (event, fieldName) => {
  const file = event.files?.[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = () => {
    updateField(fieldName, reader.result);
  };
  reader.readAsDataURL(file);
};

const onFileSelect = (event, fieldName, fileNameField, contentTypeField) => {
  // Access the newly added file
  const file = event.files[0];
  console.log("Selected file name:", file.name);

  const reader = new FileReader();

  reader.onload = () => {
    emit('update:modelValue', {
      ...props.modelValue,
      [fieldName]: reader.result,
      [fileNameField]: file.name,
      [contentTypeField]: file.type
    });
  };

  reader.readAsDataURL(file);
};

</script>

<template>
    <template v-if="isRoleAuthorized">
        <template v-for="field in fields" :key="field.name" >
        <input v-if="isFieldHidden(field)" :id="field.name" :value="modelValue[field.name]" type="hidden" @input="updateField(field.name, $event.target.value)" />

        <div v-else-if="!field.editable" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <InputText :id="field.name" :modelValue="modelValue[field.name]" disabled />
        </div>

        <div v-else-if="field.type === 'text'" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <InputText :id="field.name" :modelValue="modelValue[field.name]" :class="{ 'p-invalid': hasFieldError(field) }" @update:modelValue="updateField(field.name, $event)" />
            <small v-if="hasFieldError(field)" class="p-invalid"> {{ field.label }} is required. </small>
        </div>

        <div v-else-if="field.type === 'number'" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <InputNumber :id="field.name" :modelValue="modelValue[field.name]" integeronly :class="{ 'p-invalid': hasFieldError(field) }" @update:modelValue="updateField(field.name, $event)" />
            <small v-if="hasFieldError(field)" class="p-invalid"> {{ field.label }} is required. </small>
        </div>

        <div v-else-if="field.type === 'currency'" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <InputNumber mode="currency" :id="field.name" :modelValue="modelValue[field.name]" integeronly :class="{ 'p-invalid': hasFieldError(field) }" @update:modelValue="updateField(field.name, $event)" />
            <small v-if="hasFieldError(field)" class="p-invalid"> {{ field.label }} is required. </small>
        </div>

        <div v-else-if="field.type === 'enum'" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <Dropdown
                :id="field.name"
                :modelValue="modelValue[field.name]"
                :options="getFieldOptions(field)"
                :optionLabel="getOptionLabel(field)"
                :optionValue="getOptionValue(field)"
                :placeholder="field.placeholder || `Select ${field.label}`"
                :showClear="true"
                :class="{ 'p-invalid': hasFieldError(field) }"
                @update:modelValue="updateField(field.name, normalizeFieldValue(field, $event))"
            />
            <small v-if="hasFieldError(field)" class="p-invalid"> {{ field.label }} is required. </small>
        </div>

        <div v-else-if="isSelectField(field)" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <Dropdown
                :id="field.name"
                :modelValue="getFieldValue(field)"
                :options="getFieldOptions(field)"
                :optionLabel="getOptionLabel(field)"
                :optionValue="getOptionValue(field)"
                :placeholder="field.placeholder || `Select ${field.label}`"
                :showClear="true"
                :class="{ 'p-invalid': hasFieldError(field) }"
                @update:modelValue="updateField(field.name, normalizeFieldValue(field, $event))"
            />
            <small v-if="hasFieldError(field)" class="p-invalid"> {{ field.label }} is required. </small>
        </div>

        <div v-else-if="isManyToOneRelationship(field)" class="field">
            <label :for="field.name">{{ field.label }}</label>

            <div class="p-inputgroup">
                <InputText :id="field.name" :modelValue="getRelationshipDisplayValue(field)" :placeholder="field.placeholder || `Select ${field.label}`" disabled :class="{ 'p-invalid': hasFieldError(field) }" />
                <Button icon="pi pi-search" type="button" @click="openRelationshipDialog(field)" />
                <Button icon="pi pi-times" type="button" class="p-button-secondary" @click="clearRelationshipRecord(field)" />
            </div>

            <small v-if="hasFieldError(field)" class="p-invalid"> {{ field.label }} is required. </small>
        </div>

        <div v-else-if="field.type === 'textarea'" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <Textarea :id="field.name" :modelValue="modelValue[field.name]" rows="3" cols="20" :class="{ 'p-invalid': hasFieldError(field) }" @update:modelValue="updateField(field.name, $event)" />
            <small v-if="hasFieldError(field)" class="p-invalid"> {{ field.label }} is required. </small>
        </div>

        <div v-else-if="field.type === 'date'" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <Calendar :id="field.name" :modelValue="modelValue[field.name]" :class="{ 'p-invalid': hasFieldError(field) }" @update:modelValue="updateField(field.name, $event)" />
            <small v-if="hasFieldError(field)" class="p-invalid"> {{ field.label }} is required. </small>
        </div>

        <div v-else-if="field.type === 'datetime'" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <Calendar :id="field.name" :modelValue="modelValue[field.name]" :showTime="true" :class="{ 'p-invalid': hasFieldError(field) }" @update:modelValue="updateField(field.name, $event)" />
            <small v-if="hasFieldError(field)" class="p-invalid"> {{ field.label }} is required. </small>
        </div>

      <div v-else-if="field.type === 'image' || field.type === 'file'" class="field">
        <label :for="field.name">{{ field.label }}</label>

        <!-- Image preview -->
        <FileUpload
            :src="modelValue[field.name]"
            :multiple="false"
            :fileLimit="1"
            preview
            customUpload
            chooseLabel="Upload File"
            @select="(e) => onFileSelect(e, field.name, field.fileNameField, field.contentTypeField)"
        />

<!--        <Image-->
<!--            v-if="field.type === 'image' && modelValue[field.name]"-->
<!--            :src="modelValue[field.name]"-->
<!--            preview-->
<!--            imageClass="uploaded-image"-->
<!--            width="250"-->
<!--        />-->

<!--        <Button-->
<!--            v-if="field.type === 'file'"-->
<!--            icon="pi pi-image"-->
<!--            label="Download"-->
<!--            link-->
<!--            @click="downloadFile(-->
<!--                                  slotProps.data.content,-->
<!--                                  slotProps.data.fileName-->
<!--                              )"-->
<!--        />-->

        <small v-if="hasFieldError(field)" class="p-invalid">
          {{ field.label }} is required.
        </small>
      </div>
    </template>
    <Dialog v-model:visible="relationshipDialogVisible" :header="relationshipDialogField ? `Select ${relationshipDialogField.label}` : 'Select Record'" :modal="true" class="p-fluid" :style="{ width: '700px' }">
        <GenericCrud :showToolbar="true" title="" dialogHeader="" :fields="relationshipDialogField?.optionsFields" :service="relationshipDialogField?.optionsService" @record-selected="rowSelectRelationshipRecord" />
        <!--    <DataTable-->
        <!--        v-model:selection="relationshipDialogSelection"-->
        <!--        v-model:filters="relationshipDialogFilters"-->
        <!--        :value="relationshipDialogOptions"-->
        <!--        selectionMode="single"-->
        <!--        dataKey="id"-->
        <!--        :paginator="true"-->
        <!--        :rows="10"-->
        <!--        :loading="relationshipDialogLoading"-->
        <!--        responsiveLayout="scroll"-->
        <!--        filterDisplay="row"-->
        <!--        :globalFilterFields="getRelationshipColumns(relationshipDialogField || {}).map(col => col.field)"-->
        <!--        @row-dblclick="selectRelationshipRecord"-->
        <!--    >-->
        <!--      <template #header>-->
        <!--        <div class="flex justify-content-end">-->
        <!--          <span class="p-input-icon-left">-->
        <!--            <i class="pi pi-search"/>-->
        <!--            <InputText-->
        <!--                v-model="relationshipDialogFilters['global'].value"-->
        <!--                placeholder="Search..."-->
        <!--            />-->
        <!--          </span>-->
        <!--        </div>-->
        <!--      </template>-->

        <!--      <Column selectionMode="single" headerStyle="width: 3rem"/>-->

        <!--      <Column-->
        <!--          v-for="column in getRelationshipColumns(relationshipDialogField || {})"-->
        <!--          :key="column.field"-->
        <!--          :field="column.field"-->
        <!--          :header="column.header"-->
        <!--          :sortable="column.sortable !== false"-->
        <!--      />-->
        <!--    </DataTable>-->

        <template #footer>
            <Button label="Cancel" icon="pi pi-times" type="button" class="p-button-text" @click="relationshipDialogVisible = false" />
            <Button label="Select" icon="pi pi-check" type="button" :disabled="!relationshipDialogSelection" @click="selectRelationshipRecord" />
        </template>
    </Dialog>
    </template>
</template>

<style>

.uploaded-image {
  max-width: 200px;
  border-radius: 6px;
}

</style>