<script setup>
import { computed, ref, watch } from 'vue';
import TabPanel from 'primevue/tabpanel';
import GenericCrud from './GenericCrud.vue';
import GenericPanel from './GenericPanel.vue';
import TabView from 'primevue/tabview';
import Dialog from 'primevue/dialog';
import GroupLayout from './GroupLayout.vue';
import AuthService from '../service/AuthService';

/**
 * GenericMasterDetail Component
 *
 * A versatile master-detail CRUD component supporting two presentation modes:
 * - 'split' (default): Side-by-side 2-column layout (master on left, detail tabs on right)
 * - 'modal': Full-width master table with detail tabs rendered inside a popup modal dialog
 *
 * @prop {String} title - Main title displayed in the detail panel (default: 'Master Detail')
 * @prop {String} subtitle - Subtitle for the detail panel; if empty, shows selected master record label
 * @prop {Object} master - Configuration object for the master CRUD
 * @prop {Array} details - Array of detail configuration objects for child tabs
 * @prop {String} [dataKey='id'] - Default primary key field name
 * @prop {String|Array} [role=null] - Role(s) required to view component
 * @prop {String} [layout='split'] - Presentation layout: 'split' (side-by-side) or 'modal' (dialog popup)
 * @prop {String} [trigger='auto'] - Selection trigger: 'auto', 'click', or 'double-click'
 * @prop {Object} [dialogStyle] - Dialog style object for modal mode (default: { width: '75vw' })
 */

const props = defineProps({
    title: {
        type: String,
        default: 'Master Detail'
    },
    subtitle: {
        type: String,
        default: ''
    },
    master: {
        type: Object,
        required: true
    },
    details: {
        type: Array,
        default: () => []
    },
    dataKey: {
        type: String,
        default: 'id'
    },
    role: {
        type: [String, Array],
        default: null
    },
    layout: {
        type: String,
        default: 'split',
        validator: (value) => ['split', 'modal'].includes(value)
    },
    trigger: {
        type: String,
        default: 'auto',
        validator: (value) => ['auto', 'click', 'double-click'].includes(value)
    },
    dialogStyle: {
        type: Object,
        default: () => ({ width: '75vw' })
    }
});

const isRoleAuthorized = computed(() => {
    if (!props.role) return true;
    return AuthService.hasRole(props.role);
});

const isModal = computed(() => props.layout === 'modal');

const selectedMasterRecord = ref(null);
const detailRefreshKey = ref(0);
const showDetailDialog = ref(false);

const selectedMasterId = computed(() => {
    const masterDataKey = props.master.dataKey || props.dataKey;
    return selectedMasterRecord.value?.[masterDataKey] || null;
});

const hasSelectedMaster = computed(() => {
    return selectedMasterRecord.value !== null && selectedMasterRecord.value !== undefined;
});

const handleMasterSelected = (record) => {
    selectedMasterRecord.value = record;
    detailRefreshKey.value += 1;

    if (isModal.value && props.trigger === 'click') {
        showDetailDialog.value = true;
    }
};

const handleMasterDoubleClicked = (record) => {
    selectedMasterRecord.value = record;
    detailRefreshKey.value += 1;

    if (isModal.value) {
        showDetailDialog.value = true;
    }
};

const handleMasterDetailClicked = (record) => {
    selectedMasterRecord.value = record;
    detailRefreshKey.value += 1;

    if (isModal.value) {
        showDetailDialog.value = true;
    }
};

watch(showDetailDialog, (visible) => {
    if (visible) {
        detailRefreshKey.value += 1;
    }
});

const getDetailKey = (detail) => {
    return `${detail.key || detail.title}-${selectedMasterId.value || 'none'}-${detailRefreshKey.value}`;
};

const getDetailTitle = (detail) => {
    return detail.title;
};

const getDetailService = (detail) => {
    return {
        findAll: async () => {
            if (!hasSelectedMaster.value) {
                return [];
            }

            if (detail.service?.findByParent) {
                return detail.service.findByParent(detail.parentField, selectedMasterRecord.value);
            }

            if (detail.service?.findAll) {
                return detail.service.findAll(selectedMasterRecord.value);
            }

            return [];
        },
        createOrUpdate: async (record) => {
            const recordWithParent = applyParentToDetailRecord(detail, record);

            if (detail.service?.createOrUpdate) {
                return detail.service.createOrUpdate(recordWithParent, selectedMasterRecord.value);
            }

            return recordWithParent;
        },
        delete: async (idOrPayload) => {
            if (detail.service?.delete) {
                return detail.service.delete(idOrPayload, selectedMasterRecord.value);
            }

            return null;
        },
        findAllWithPage: async (payload) => {
            if (!hasSelectedMaster.value) {
                return [];
            }

            if (detail.service?.findByParentWithPage) {
                return await detail.service.findByParentWithPage(detail.parentField, selectedMasterRecord.value, payload);
            }

            if (detail.service?.findByParent) {
                return await detail.service.findByParent(detail.parentField, selectedMasterRecord.value, payload);
            }

            if (detail.service?.findAllWithPage) {
                return detail.service.findAllWithPage(payload);
            }
            return null;
        }
    };
};

const getDetailFields = (detail) => {
    return detail.fields.map((field) => {
        if (field.name === detail.parentField) {
            return {
                ...field,
                editable: false,
                hidden: true
            };
        }

        return field;
    });
};

const getParentValue = (detail) => {
    if (!hasSelectedMaster.value) {
        return null;
    }

    if (detail.parentValue) {
        return detail.parentValue(selectedMasterRecord.value);
    }

    return selectedMasterRecord.value;
};

const applyParentToDetailRecord = (detail, record) => {
    if (!detail.parentField) {
        return record;
    }

    return {
        ...record,
        [detail.parentField]: getParentValue(detail)
    };
};

const getDetailCreateEmptyRecord = (detail) => {
    return () => {
        const emptyRecord = detail.createEmptyRecord ? detail.createEmptyRecord(selectedMasterRecord.value) : {};
        return applyParentToDetailRecord(detail, emptyRecord);
    };
};

const getSelectedMasterLabel = () => {
    if (!hasSelectedMaster.value) {
        return 'Select a parent record to view details.';
    }

    const labelField = props.master.optionLabel || props.master.labelField || 'name';
    const label = selectedMasterRecord.value[labelField] || selectedMasterRecord.value[props.dataKey];

    return `Selected: ${label}`;
};

const getMasterTitle = (master) => {
    return master.title;
};
</script>

<template>
    <template v-if="isRoleAuthorized">
        <!-- Modal / Dialog Layout -->
        <template v-if="isModal">
            <GenericCrud
                class="col-12 h-full flex flex-column"
                :title="getMasterTitle(master)"
                :dialogHeader="master.dialogHeader"
                :dataKey="master.dataKey || dataKey"
                :fields="master.fields"
                :service="master.service"
                :createEmptyRecord="master.createEmptyRecord"
                :messages="master.messages"
                :showDetailButton="true"
                :detailButtonTooltip="'View ' + (details?.[0]?.title || 'Details')"
                @record-selected="handleMasterSelected"
                @record-double-click="handleMasterDoubleClicked"
                @record-detail="handleMasterDetailClicked"
            />

            <Dialog
                v-model:visible="showDetailDialog"
                :header="master.dialogHeader || title"
                modal
                closable
                :style="dialogStyle"
                :maximizable="true"
            >
                <GenericPanel class="col-12 h-full flex flex-column" :title="title" :subtitle="subtitle || getSelectedMasterLabel()" :showToolbar="false">
                    <TabView class="generic-panel-body">
                        <TabPanel v-for="detail in details" :key="getDetailKey(detail)" :header="getDetailTitle(detail)">
                            <div v-if="!hasSelectedMaster" class="p-3 text-color-secondary">Select a parent record before managing {{ detail.title }}.</div>
                            <GenericCrud
                                v-else
                                :key="getDetailKey(detail)"
                                :refreshKey="detailRefreshKey"
                                :title="detail.title"
                                :dialogHeader="detail.dialogHeader"
                                :dataKey="detail.dataKey || dataKey"
                                :fields="getDetailFields(detail)"
                                :service="getDetailService(detail)"
                                :createEmptyRecord="getDetailCreateEmptyRecord(detail)"
                                :messages="detail.messages"
                                :deleteWithPayload="detail.deleteWithPayload"
                            />
                        </TabPanel>
                    </TabView>
                </GenericPanel>
            </Dialog>
        </template>

        <!-- Side-by-Side (Split 2-column) Layout -->
        <template v-else>
            <GroupLayout class="p-fluid" :columns="2">
                <GenericCrud
                    class="col-12 h-full flex flex-column"
                    :title="getMasterTitle(master)"
                    :dialogHeader="master.dialogHeader"
                    :dataKey="master.dataKey || dataKey"
                    :fields="master.fields"
                    :service="master.service"
                    :createEmptyRecord="master.createEmptyRecord"
                    :messages="master.messages"
                    @record-selected="handleMasterSelected"
                    @record-double-click="handleMasterDoubleClicked"
                    @record-detail="handleMasterDetailClicked"
                />
                <GenericPanel class="col-12 h-full flex flex-column" :title="title" :subtitle="subtitle || getSelectedMasterLabel()" :showToolbar="false">
                    <TabView class="generic-panel-body">
                        <TabPanel v-for="detail in details" :key="getDetailKey(detail)" :header="getDetailTitle(detail)">
                            <div v-if="!hasSelectedMaster" class="p-3 text-color-secondary">Select a parent record before managing {{ detail.title }}.</div>
                            <GenericCrud
                                v-else
                                :key="getDetailKey(detail)"
                                :refreshKey="detailRefreshKey"
                                :title="detail.title"
                                :dialogHeader="detail.dialogHeader"
                                :dataKey="detail.dataKey || dataKey"
                                :fields="getDetailFields(detail)"
                                :service="getDetailService(detail)"
                                :createEmptyRecord="getDetailCreateEmptyRecord(detail)"
                                :messages="detail.messages"
                                :deleteWithPayload="detail.deleteWithPayload"
                            />
                        </TabPanel>
                    </TabView>
                </GenericPanel>
            </GroupLayout>
        </template>
    </template>
</template>