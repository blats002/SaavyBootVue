<script setup>
import { FilterMatchMode } from 'primevue/api';
import { ref, onBeforeMount, onMounted, watch } from 'vue';
import { useToast } from 'primevue/usetoast';
import GenericDialog from './GenericDialog.vue';
import GenericPanel from './GenericPanel.vue';

const props = defineProps({
  title: {
    type: String,
    default: 'Manage Records'
  },
  dialogHeader: {
    type: String,
    default: 'Record Details'
  },
  dataKey: {
    type: String,
    default: 'id'
  },
  selectionMode: {
    type: String,
    default: 'multiple'
  },
  refreshKey: {
    type: [String, Number],
    default: 0
  },
  fields: {
    type: Array,
    required: true
  },
  service: {
    type: Object,
    required: true
  },
  showToolbar: {
    type: Boolean,
    default: true
  },
  createEmptyRecord: {
    type: Function,
    default: () => ({})
  },
  messages: {
    type: Object,
    default: () => ({
      created: 'Record Created',
      updated: 'Record Updated',
      deleted: 'Record Deleted',
      deletedMany: 'Records Deleted'
    })
  }
});

const emit = defineEmits(['record-selected', 'records-loaded', 'record-saved', 'record-deleted']);

const toast = useToast();

const records = ref([]);
const record = ref({});
const selectedRecords = ref(null);

const getSelectionColumnMode = () => {
    return props.selectionMode === 'single' ? 'single' : 'multiple';
};

const filters = ref({});
const submitted = ref(false);

const recordDialog = ref(false);
const deleteRecordDialog = ref(false);
const deleteRecordsDialog = ref(false);

const leftToolBarButtons = [
    {
        key: 'new',
        label: 'New',
        icon: 'pi pi-plus',
        class: 'p-button-success mr-2'
    },
    {
        key: 'delete-selected',
        label: 'Delete',
        icon: 'pi pi-trash',
        class: 'p-button-danger'
    }
];

const formDialogButtons = [
    {
        key: 'cancel',
        label: 'Cancel',
        icon: 'pi pi-times',
        class: 'p-button-text',
        closesDialog: true
    },
    {
        key: 'save',
        label: 'Save',
        icon: 'pi pi-check',
        class: 'p-button-text'
    }
];

const confirmDialogButtons = [
    {
        key: 'cancel',
        label: 'No',
        icon: 'pi pi-times',
        class: 'p-button-text',
        closesDialog: true
    },
    {
        key: 'confirm',
        label: 'Yes',
        icon: 'pi pi-check',
        class: 'p-button-text'
    }
];

onBeforeMount(() => {
    initFilters();
});

onMounted(() => {
    loadRecords();
});

watch(
    () => props.refreshKey,
    () => {
        loadRecords();
    }
);

const loadRecords = async () => {
    records.value = await props.service.findAll();
    emit('records-loaded', records.value);
};

const handleRowClick = (event) => {
    emit('record-selected', event.data);
};

const handleRowSelect = (event) => {
  emit('record-selected', event.data);
};

const openNew = () => {
    record.value = props.createEmptyRecord();
    submitted.value = false;
    recordDialog.value = true;
};

const hideDialog = () => {
    recordDialog.value = false;
    submitted.value = false;
};

const getEditableFields = () => {
  return props.fields
};

const getTableFields = () => {
    return props.fields.filter((field) => !isFieldHidden(field));
};

const isEmptyValue = (value) => {
    if (typeof value === 'string') {
        return value.trim() === '';
    }

    return value === null || value === undefined;
};

const hasFieldError = (field) => {
  return props.submitted && field.required && isEmptyValue(props.modelValue[field.name]);
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

const isValidRecord = () => {
    return props.fields.every((field) => {
        if (!field.required || !field.editable) {
            return true;
        }

        return !isEmptyValue(record.value[field.name]);
    });
};

const findIndexById = (id) => {
    return records.value.findIndex((item) => item[props.dataKey] === id);
};

const saveRecord = async () => {
    submitted.value = true;

    if (!isValidRecord()) {
        return;
    }

    const recordId = record.value[props.dataKey];

    if (recordId) {
        const updatedRecord = await props.service.createOrUpdate(record.value);

        const index = findIndexById(recordId);

        if (index !== -1) {
            records.value[index] = updatedRecord || { ...updatedRecord };
        }

        emit('record-saved', updatedRecord);

        toast.add({
            severity: 'success',
            summary: 'Successful',
            detail: props.messages.updated,
            life: 3000
        });
    } else {
        const createdRecord = await props.service.createOrUpdate(record.value)

        records.value.push(createdRecord);

        emit('record-saved', createdRecord);

        toast.add({
            severity: 'success',
            summary: 'Successful',
            detail: props.messages.created,
            life: 3000
        });
    }

    recordDialog.value = false;
    record.value = props.createEmptyRecord();
};

const editRecord = (selectedRecord) => {
    record.value = { ...selectedRecord };
    submitted.value = false;
    recordDialog.value = true;
};

const confirmDeleteRecord = (selectedRecord) => {
    record.value = selectedRecord;
    deleteRecordDialog.value = true;
};

const deleteRecord = async () => {
    await props.service.delete(record.value[props.dataKey]);

    const deletedRecord = record.value;

    records.value = records.value.filter((item) => item[props.dataKey] !== record.value[props.dataKey]);
    deleteRecordDialog.value = false;
    record.value = props.createEmptyRecord();

    emit('record-deleted', deletedRecord);

    toast.add({
        severity: 'success',
        summary: 'Successful',
        detail: props.messages.deleted,
        life: 3000
    });
};

const confirmDeleteSelected = () => {
    deleteRecordsDialog.value = true;
};

const getLeftToolBarButtons = () => {
    return leftToolBarButtons.map((button) => {
        if (button.key === 'delete-selected') {
            const hasSelection = Array.isArray(selectedRecords.value)
                ? selectedRecords.value.length > 0
                : !!selectedRecords.value;

            return {
                ...button,
                disabled: !hasSelection
            };
        }

        return button;
    });
};

const handlePanelButtonClick = (button) => {
    if (button.key === 'new') {
        openNew();
        return;
    }

    if (button.key === 'delete-selected') {
        confirmDeleteSelected();
    }
};

const deleteSelectedRecords = async () => {
    if (!selectedRecords.value?.length) {
        return;
    }

    await Promise.all(selectedRecords.value.map((item) => props.service.delete(item[props.dataKey])));

    const selectedIds = selectedRecords.value.map((item) => item[props.dataKey]);
    records.value = records.value.filter((item) => !selectedIds.includes(item[props.dataKey]));

    deleteRecordsDialog.value = false;
    selectedRecords.value = null;

    toast.add({
        severity: 'success',
        summary: 'Successful',
        detail: props.messages.deletedMany,
        life: 3000
    });
};

const handleFormDialogButton = (button) => {
    if (button.key === 'cancel') {
        closeRecordDialog();
        return;
    }

    if (button.key === 'save') {
        saveRecord();
    }
};

const handleDeleteRecordDialogButton = (button) => {
    if (button.key === 'confirm') {
        deleteRecord();
    }
};

const handleDeleteRecordsDialogButton = (button) => {
    if (button.key === 'confirm') {
        deleteSelectedRecords();
    }
};

const closeRecordDialog = () => {
    recordDialog.value = false;
    submitted.value = false;
};

const initFilters = () => {
    filters.value = {
        global: { value: null, matchMode: FilterMatchMode.CONTAINS }
    };
};

const getFieldDisplayValue = (rowData, field) => {
  const value = rowData[field.name];

  if (typeof field.displayTemplate === 'function') {
    return field.displayTemplate(value, rowData, field);
  }

  if (field.type === 'manyToOne') {
    if (!value) {
      return '';
    }

    const optionLabel = field.optionLabel || 'name';

    if (typeof value === 'object') {
      return value[optionLabel] || value.id || '';
    }

    return value;
  }

  return value;
};

</script>

<template>
    <div class="grid">
        <div class="col-12">
            <GenericPanel
                :showToolbar="showToolbar"
                :bodyType="panel"
                :title="title"
                :leftToolBarButtons="getLeftToolBarButtons()"
                @button-click="handlePanelButtonClick"
            >
                <Toast />

                <DataTable
                    :value="records"
                    v-model:selection="selectedRecords"
                    :dataKey="dataKey"
                    :paginator="true"
                    :rows="10"
                    :filters="filters"
                    paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                    :rowsPerPageOptions="[5, 10, 25]"
                    currentPageReportTemplate="Showing {first} to {last} of {totalRecords} records"
                    responsiveLayout="scroll"
                    @row-click="handleRowClick"
                    @row-select="handleRowSelect"
                >
                    <template #header>
                        <div class="flex flex-column md:flex-row md:justify-content-end md:align-items-center">
                            <span class="block mt-2 md:mt-0 p-input-icon-left">
                                <i class="pi pi-search" />
                                <InputText v-model="filters.global.value" placeholder="Search..." />
                            </span>
                        </div>
                    </template>

                    <Column v-if="showToolbar" :selectionMode="getSelectionColumnMode()" headerStyle="width: 3rem" />

                    <Column
                      v-for="field in getTableFields()"
                      :key="field.name"
                      :field="field.name"
                      :header="field.label"
                      :sortable="field.sortable"
                      :headerStyle="`width:${field.width || 'auto'}; min-width:8rem;`"
                  >
                    <template #body="slotProps">
                      <span class="p-column-title">{{ field.label }}</span>
                      {{ getFieldDisplayValue(slotProps.data, field) }}
                    </template>
                  </Column>

                    <Column v-if="showToolbar" headerStyle="min-width:10rem;">
                        <template #body="slotProps">
                            <Button icon="pi pi-pencil" class="p-button-rounded p-button-success mr-2" @click="editRecord(slotProps.data)" />
                            <Button icon="pi pi-trash" class="p-button-rounded p-button-warning mt-2" @click="confirmDeleteRecord(slotProps.data)" />
                        </template>
                    </Column>
                </DataTable>

                <GenericDialog
                    v-model:visible="recordDialog"
                    v-model:modelValue="record"
                    type="form"
                    width="600px"
                    :header="dialogHeader"
                    :fields="getEditableFields()"
                    :submitted="submitted"
                    :buttons="formDialogButtons"
                    @button-click="handleFormDialogButton"
                />

                <GenericDialog
                    v-model:visible="deleteRecordDialog"
                    type="confirm"
                    header="Confirm"
                    message="Are you sure you want to delete this record?"
                    icon="pi pi-exclamation-triangle"
                    :buttons="confirmDialogButtons"
                    @button-click="handleDeleteRecordDialogButton"
                />

                <GenericDialog
                    v-model:visible="deleteRecordsDialog"
                    type="confirm"
                    header="Confirm"
                    message="Are you sure you want to delete the selected records?"
                    icon="pi pi-exclamation-triangle"
                    :buttons="confirmDialogButtons"
                    @button-click="handleDeleteRecordsDialogButton"
                />
            </GenericPanel>
        </div>
    </div>
</template>
