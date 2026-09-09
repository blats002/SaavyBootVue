<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import AuthService from '../../service/AuthService';
import { pluginState } from '../../plugins/pluginState';

const props = defineProps({
    title: {
        type: String,
        default: ''
    },
    columns: {
        type: Array,
        required: true
    },
    data: {
        type: Array,
        required: true
    },
    paginator: {
        type: Boolean,
        default: true
    },
    rows: {
        type: Number,
        default: 5
    },
    action: {
        type: Object,
        default: null
    },
    plugin: {
        type: String,
        default: null
    },
    role: {
        type: [String, Array],
        default: null
    }
});

const router = useRouter();

const isRoleAuthorized = computed(() => {
    if (props.plugin && !pluginState.isPluginEnabled(props.plugin)) return false;
    if (!props.role) return true;
    return AuthService.hasRole(props.role);
});

const formatCurrency = (val) => {
    if (val === null || val === undefined || isNaN(val)) return '-';
    return Number(val).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
};

const formatDate = (val) => {
    if (!val) return '-';
    try {
        const date = new Date(val);
        return date.toLocaleDateString();
    } catch {
        return val;
    }
};

const getBadgeSeverity = (status) => {
    if (!status) return 'info';
    const s = String(status).toUpperCase();
    if (['PAID', 'ACTIVE', 'COMPLETED', 'SUCCESS', 'DELIVERED', 'PROFITABLE', 'NET POSITIVE'].includes(s)) return 'success';
    if (['PENDING', 'IN_PROGRESS', 'WARNING', 'LOW_STOCK', 'BREAKEVEN', 'NEUTRAL'].includes(s)) return 'warning';
    if (['OVERDUE', 'CANCELLED', 'FAILED', 'DANGER', 'OUT_OF_STOCK', 'LOSS', 'NET NEGATIVE'].includes(s)) return 'danger';
    return 'info';
};

const handleActionClick = () => {
    if (props.action && props.action.to) {
        router.push(props.action.to);
    } else if (props.action && props.action.url) {
        window.open(props.action.url, '_blank');
    }
};
</script>

<template>
    <div v-if="isRoleAuthorized" class="card mb-4 h-full">
        <div class="flex justify-content-between align-items-center mb-4">
            <h5 v-if="title" class="m-0">{{ title }}</h5>
            <Button
                v-if="action"
                :label="action.label || 'View All'"
                :icon="action.icon || 'pi pi-arrow-right'"
                class="p-button-text p-button-sm"
                @click="handleActionClick"
            />
        </div>

        <DataTable
            :value="data"
            :rows="rows"
            :paginator="paginator && data && data.length > rows"
            responsiveLayout="scroll"
            class="p-datatable-sm"
        >
            <Column
                v-for="col in columns"
                :key="col.field"
                :field="col.field"
                :header="col.header || col.field"
                :sortable="col.sortable !== false"
                :style="col.width ? { width: col.width } : {}"
            >
                <template #body="slotProps">
                    <!-- Currency Formatter -->
                    <template v-if="col.type === 'currency'">
                        {{ formatCurrency(slotProps.data[col.field]) }}
                    </template>

                    <!-- Status Badge Formatter -->
                    <template v-else-if="col.type === 'badge'">
                        <Tag
                            :value="slotProps.data[col.field]"
                            :severity="col.severityMap ? (col.severityMap[slotProps.data[col.field]] || 'info') : getBadgeSeverity(slotProps.data[col.field])"
                        />
                    </template>

                    <!-- Date Formatter -->
                    <template v-else-if="col.type === 'date'">
                        {{ formatDate(slotProps.data[col.field]) }}
                    </template>

                    <!-- Default Text Formatter -->
                    <template v-else>
                        {{ slotProps.data[col.field] }}
                    </template>
                </template>
            </Column>

            <template #empty>
                <div class="text-center p-3 text-500">No data available</div>
            </template>
        </DataTable>
    </div>
</template>

<style scoped></style>
