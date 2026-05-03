<script setup>
import {computed, ref} from 'vue';
import TabPanel from 'primevue/tabpanel';
import GenericCrud from './GenericCrud.vue';
import GenericPanel from './GenericPanel.vue';
import TabView from "primevue/tabview";

/**
 * GenericMasterDetail Component
 *
 * A reusable master-detail CRUD component that displays a master record list on the left
 * and related detail records in tabs on the right. When a master record is selected,
 * the detail tabs automatically load and filter data based on the selected parent.
 *
 * @component
 *
 * @example
 * // Basic usage with Ministry (master) and Teams (detail)
 * <GenericMasterDetail
 *   title="Ministry Management"
 *   subtitle="Manage ministries and their teams"
 *   :master="{
 *     title: 'Ministries',
 *     dialogHeader: 'Ministry',
 *     dataKey: 'id',
 *     fields: [
 *       { name: 'name', label: 'Name', type: 'text', required: true },
 *       { name: 'description', label: 'Description', type: 'textarea' },
 *       { name: 'active', label: 'Active', type: 'boolean' }
 *     ],
 *     service: ministryService,
 *     createEmptyRecord: () => ({ name: '', description: '', active: true })
 *   }"
 *   :details="[
 *     {
 *       key: 'teams',
 *       title: 'Teams',
 *       dialogHeader: 'Team',
 *       dataKey: 'id',
 *       parentField: 'ministry',
 *       fields: [
 *         { name: 'ministry', label: 'Ministry', type: 'reference', hidden: true },
 *         { name: 'name', label: 'Team Name', type: 'text', required: true },
 *         { name: 'description', label: 'Description', type: 'textarea' },
 *         { name: 'active', label: 'Active', type: 'boolean' }
 *       ],
 *       service: {
 *         findByParent: (ministry) => teamService.findByMinistryId(ministry.id),
 *         createOrUpdate: (team, ministry) => teamService.save({ ...team, ministry }),
 *         delete: (id) => teamService.delete(id)
 *       },
 *       createEmptyRecord: (ministry) => ({
 *         name: '',
 *         description: '',
 *         active: true,
 *         ministry
 *       })
 *     }
 *   ]"
 *   dataKey="id"
 * />
 *
 * @prop {String} title - Main title displayed in the detail panel (default: 'Master Detail')
 * @prop {String} subtitle - Subtitle for the detail panel; if empty, shows selected master record label
 * @prop {Object} master - Configuration object for the master CRUD
 * @prop {String} master.title - Title for the master panel
 * @prop {String} master.dialogHeader - Header text for master record edit dialog
 * @prop {String} [master.dataKey='id'] - Primary key field name for master records
 * @prop {Array} master.fields - Field definitions for master CRUD (see GenericCrud for field structure)
 * @prop {Object} master.service - Service object with findAll, createOrUpdate, delete methods
 * @prop {Function} master.createEmptyRecord - Function returning empty master record template
 * @prop {Object} [master.messages] - Custom messages for master CRUD operations
 * @prop {String} [master.optionLabel] - Field name to use for displaying selected master label
 *
 * @prop {Array} details - Array of detail configuration objects for tabs
 * @prop {String} [details[].key] - Unique key for the detail tab (defaults to title)
 * @prop {String} details[].title - Title for the detail tab
 * @prop {String} details[].dialogHeader - Header text for detail record edit dialog
 * @prop {String} [details[].dataKey='id'] - Primary key field name for detail records
 * @prop {String} details[].parentField - Field name in detail record that references parent master
 * @prop {Array} details[].fields - Field definitions for detail CRUD
 * @prop {Object} details[].service - Service object for detail operations
 * @prop {Function} details[].service.findByParent - Fetch details by parent master record
 * @prop {Function} [details[].service.findAll] - Alternative method to fetch all details
 * @prop {Function} details[].service.createOrUpdate - Save detail record (receives record and parent)
 * @prop {Function} details[].service.delete - Delete detail record by ID
 * @prop {Function} [details[].createEmptyRecord] - Function returning empty detail record (receives parent)
 * @prop {Function} [details[].parentValue] - Custom function to extract parent value from master record
 * @prop {Object} [details[].messages] - Custom messages for detail CRUD operations
 *
 * @prop {String} [dataKey='id'] - Default primary key field name used when not specified in master/details
 *
 * @emits record-selected - Emitted from master CRUD when a record is selected (bubbled from GenericCrud)
 *
 * Features:
 * - Automatic parent-child relationship management
 * - Detail tabs only load data when master record is selected
 * - Parent field automatically hidden and populated in detail records
 * - Supports multiple detail tabs per master
 * - Refresh mechanism when master selection changes
 * - Custom parent value extraction via parentValue function
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
    }
});

const selectedMasterRecord = ref(null);
const detailRefreshKey = ref(0);

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
};

const getDetailKey = (detail) => {
    return `${detail.key || detail.title}-${selectedMasterId.value || 'none'}-${detailRefreshKey.value}`;
};

const getDetailTitle = (detail) => {
    if (!hasSelectedMaster.value) {
        return detail.title;
    }

    return detail.title;
};

const getDetailService = (detail) => {
    return {
        findAll: async () => {
            if (!hasSelectedMaster.value) {
                return [];
            }

            if (detail.service?.findByParent) {
                return detail.service.findByParent(selectedMasterRecord.value);
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
        delete: async (id) => {
            if (detail.service?.delete) {
                return detail.service.delete(id, selectedMasterRecord.value);
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
</script>

<template>

  <GroupLayout class="p-fluid" :columns="2">
    <GenericCrud
        :title="master.title"
        :dialogHeader="master.dialogHeader"
        :dataKey="master.dataKey || dataKey"
        :fields="master.fields"
        :service="master.service"
        :createEmptyRecord="master.createEmptyRecord"
        :messages="master.messages"
        @record-selected="handleMasterSelected"
    />
    <GenericPanel
        :title="title"
        :subtitle="subtitle || getSelectedMasterLabel()"
        :showToolbar="false"
    >
      <TabView class="generic-panel-body">
        <TabPanel
            v-for="detail in details"
            :key="getDetailKey(detail)"
            :header="getDetailTitle(detail)"
        >

          <div v-if="!hasSelectedMaster" class="p-3 text-color-secondary">
            Select a parent record before managing {{ detail.title }}.
          </div>
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
          />
        </TabPanel>
      </TabView>
    </GenericPanel>
  </GroupLayout>
</template>
