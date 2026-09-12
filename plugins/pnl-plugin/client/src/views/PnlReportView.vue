<script setup>
import { ref, onMounted, computed } from 'vue';
import GenericPanel from '@core/components/GenericPanel.vue';
import GenericFileUploadDialog from '@core/components/GenericFileUploadDialog.vue';
import PnlReportService from '../service/PnlReportService';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ProgressSpinner from 'primevue/progressspinner';
import Tag from 'primevue/tag';
import Menu from 'primevue/menu';
import { useToast } from 'primevue/usetoast';
import AuthService from '@core/service/AuthService';
import html2canvas from 'html2canvas';

const toast = useToast();

const selectedYear = ref(2026);
const yearOptions = [
    { label: '2024', value: 2024 },
    { label: '2025', value: 2025 },
    { label: '2026', value: 2026 },
    { label: '2027', value: 2027 }
];

const reportData = ref(null);
const isLoading = ref(false);
const showImportDialog = ref(false);

// Drilldown state
const showDrilldown = ref(false);
const drilldownTitle = ref('');
const drilldownLoading = ref(false);
const drilldownRecords = ref([]);

const canManageFinancials = computed(() => {
    return AuthService.hasRole(['ROLE_ADMIN', 'ROLE_MANAGER']);
});

const loadReport = async () => {
    isLoading.value = true;
    try {
        reportData.value = await PnlReportService.getReport(selectedYear.value);
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: 'Error Loading Report',
            detail: e.response?.data?.message || e.message || 'Failed to load P&L Report',
            life: 4000
        });
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => {
    loadReport();
});

const activeCurrency = computed(() => {
    return reportData.value?.currency || 'PHP';
});

const formatCurrency = (val) => {
    if (val === null || val === undefined) return '-';
    const num = Number(val);
    if (isNaN(num)) return '-';
    if (num === 0) return '-';
    const curr = activeCurrency.value;
    const locale = curr === 'PHP' ? 'en-PH' : curr === 'EUR' ? 'de-DE' : curr === 'GBP' ? 'en-GB' : curr === 'JPY' ? 'ja-JP' : 'en-US';
    return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: curr,
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    }).format(num);
};

const formatPercent = (val) => {
    if (val === null || val === undefined) return '-';
    const num = Number(val);
    if (isNaN(num)) return '-';
    return num.toFixed(1) + '%';
};

const openDrilldown = async (row, periodKey, periodLabel) => {
    if (!row.accountId) return;
    const amount = row.periodValues ? row.periodValues[periodKey] : null;
    if (!amount || Number(amount) === 0) return;

    drilldownTitle.value = `Transactions for [${row.code}] ${row.name} (${periodLabel || periodKey})`;
    showDrilldown.value = true;
    drilldownLoading.value = true;
    drilldownRecords.value = [];

    try {
        drilldownRecords.value = await PnlReportService.getDrilldown({
            accountId: row.accountId,
            period: periodKey,
            year: selectedYear.value
        });
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: 'Drilldown Error',
            detail: 'Failed to fetch transaction details',
            life: 3000
        });
    } finally {
        drilldownLoading.value = false;
    }
};

const downloadTemplate = async () => {
    try {
        await PnlReportService.downloadTemplate();
        toast.add({
            severity: 'info',
            summary: 'Template Downloaded',
            detail: 'Canonical CSV template ready for editing',
            life: 3000
        });
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: 'Download Error',
            detail: 'Failed to download template',
            life: 3000
        });
    }
};

const exportMenuRef = ref(null);
const isExporting = ref(false);

const exportMenuItems = [
    {
        label: 'Excel Spreadsheet (.xlsx)',
        icon: 'pi pi-file-excel text-green-600',
        command: () => exportToExcel()
    },
    {
        label: 'High-Res Image (.png)',
        icon: 'pi pi-image text-blue-600',
        command: () => exportToImage()
    },
    {
        separator: true
    },
    {
        label: 'Raw Data (.csv)',
        icon: 'pi pi-file text-orange-600',
        command: () => exportToCsv()
    }
];

const exportToExcel = async () => {
    if (!reportData.value) return;
    isExporting.value = true;
    try {
        await PnlReportService.downloadExcel(selectedYear.value);
        toast.add({
            severity: 'success',
            summary: 'Excel Downloaded',
            detail: `PnL_Statement_${selectedYear.value}.xlsx generated with accounting styles`,
            life: 3000
        });
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: 'Excel Export Failed',
            detail: e.response?.data?.message || e.message || 'Could not export Excel file',
            life: 3000
        });
    } finally {
        isExporting.value = false;
    }
};

const exportToImage = async () => {
    const el = document.getElementById('pnl-matrix-table-container');
    if (!el) return;
    isExporting.value = true;
    try {
        const prevOverflow = el.style.overflow;
        const prevWidth = el.style.width;
        const prevMaxWidth = el.style.maxWidth;

        // Expand container so html2canvas renders all months (Jan - Dec) and Full Year Total without scroll clipping
        el.style.overflow = 'visible';
        el.style.width = 'max-content';
        el.style.maxWidth = 'none';

        const fullWidth = el.scrollWidth || el.offsetWidth;
        const fullHeight = el.scrollHeight || el.offsetHeight;

        const canvas = await html2canvas(el, {
            scale: 2,
            useCORS: true,
            backgroundColor: '#ffffff',
            logging: false,
            width: fullWidth,
            height: fullHeight,
            windowWidth: fullWidth + 100,
            windowHeight: fullHeight + 100,
            x: 0,
            y: 0,
            scrollX: 0,
            scrollY: 0
        });

        // Restore original styles
        el.style.overflow = prevOverflow;
        el.style.width = prevWidth;
        el.style.maxWidth = prevMaxWidth;

        const image = canvas.toDataURL('image/png');
        const link = document.createElement('a');
        link.href = image;
        link.download = `PnL_Statement_${selectedYear.value}.png`;
        link.click();
        toast.add({
            severity: 'success',
            summary: 'Image Downloaded',
            detail: `PnL_Statement_${selectedYear.value}.png captured successfully (Jan to Dec + Full Year Total)`,
            life: 3000
        });
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: 'Image Export Failed',
            detail: e.message || 'Could not generate report snapshot image',
            life: 3000
        });
    } finally {
        isExporting.value = false;
    }
};

const exportToCsv = () => {
    if (!reportData.value) return;
    const periods = reportData.value.periods || [];
    const periodLabels = reportData.value.periodLabels || [];

    let csv = ['Line Item,Account Code,Subcategory,' + periodLabels.join(',') + ',Full Year Total'];

    const appendRow = (r) => {
        const vals = periods.map((p) => (r.periodValues && r.periodValues[p] !== undefined ? r.periodValues[p] : '0'));
        csv.push(`"${r.name || ''}","${r.code || ''}","${r.subcategory || ''}",${vals.join(',')},${r.totalValue || '0'}`);
    };

    (reportData.value.sections || []).forEach((sec) => {
        csv.push(`"--- ${sec.title} ---",,,,`);
        (sec.rows || []).forEach(appendRow);
        if (sec.subtotalRow) appendRow(sec.subtotalRow);
    });

    if (reportData.value.grossProfitRow) appendRow(reportData.value.grossProfitRow);
    if (reportData.value.grossMarginPercentRow) appendRow(reportData.value.grossMarginPercentRow);
    if (reportData.value.operatingIncomeRow) appendRow(reportData.value.operatingIncomeRow);
    if (reportData.value.operatingMarginPercentRow) appendRow(reportData.value.operatingMarginPercentRow);
    if (reportData.value.netIncomeRow) appendRow(reportData.value.netIncomeRow);
    if (reportData.value.netMarginPercentRow) appendRow(reportData.value.netMarginPercentRow);

    const blob = new Blob([csv.join('\n')], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `PnL_Report_${selectedYear.value}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
};

const handleCsvUpload = (file) => {
    return PnlReportService.uploadCsv(file);
};

const getLeftButtons = () => {
    const buttons = [
        {
            key: 'refresh',
            label: 'Refresh',
            icon: 'pi pi-refresh',
            class: 'p-button-secondary'
        },
        {
            key: 'template',
            label: 'Get CSV Template',
            icon: 'pi pi-download',
            class: 'p-button-outlined p-button-info'
        }
    ];

    if (canManageFinancials.value) {
        buttons.push({
            key: 'import',
            label: 'Import CSV',
            icon: 'pi pi-upload',
            class: 'p-button-success'
        });
    }

    buttons.push({
        key: 'export',
        label: 'Export Statement',
        icon: 'pi pi-download',
        class: 'p-button-outlined p-button-secondary'
    });

    return buttons;
};

const handlePanelButtonClick = (button) => {
    if (button.key === 'refresh') loadReport();
    if (button.key === 'template') downloadTemplate();
    if (button.key === 'import') showImportDialog.value = true;
    if (button.key === 'export') {
        const targetEvent = button.originalEvent || button.event || window.event;
        exportMenuRef.value.toggle(targetEvent);
    }
};
</script>

<template>
    <div class="grid">
        <div class="col-12">
            <!-- Contextual Export Menu Popup -->
            <Menu ref="exportMenuRef" :model="exportMenuItems" :popup="true" />

            <GenericPanel
                title="Profit & Loss (P&L) Statement"
                subtitle="Income statement with multi-period rollup, margin analytics, and audit drilldowns"
                :role="['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER']"
                :leftToolBarButtons="getLeftButtons()"
                @button-click="handlePanelButtonClick"
            >
                <!-- Year selector filter bar -->
                <div class="flex justify-content-between align-items-center mb-3 p-3 bg-surface-50 border-round border-1 border-200">
                    <div class="flex align-items-center gap-3">
                        <label class="font-semibold text-700">Financial Fiscal Year:</label>
                        <Dropdown
                            v-model="selectedYear"
                            :options="yearOptions"
                            optionLabel="label"
                            optionValue="value"
                            class="w-10rem"
                            @change="loadReport"
                        />
                    </div>
                    <div class="text-sm text-500">
                        <i class="pi pi-info-circle mr-1" />
                        Click any monthly amount to inspect underlying transactions
                    </div>
                </div>

                <!-- Loading State -->
                <div v-if="isLoading" class="flex justify-content-center p-6">
                    <ProgressSpinner />
                </div>

                <!-- Report Matrix Table -->
                <div id="pnl-matrix-table-container" v-else-if="reportData" class="overflow-x-auto border-round border-1 border-300 shadow-1 bg-white p-2">
                    <table class="pnl-matrix-table">
                        <thead>
                            <tr class="header-row">
                                <th class="sticky-col first-col">Account / Line Item</th>
                                <th class="sticky-col second-col">Code</th>
                                <th class="sticky-col third-col">Subcategory</th>
                                <th v-for="(label, idx) in reportData.periodLabels" :key="idx" class="period-col text-right">
                                    {{ label }}
                                </th>
                                <th class="total-col text-right">Full Year</th>
                            </tr>
                        </thead>
                        <tbody>
                            <!-- SECTIONS (Revenue, COGS, OPEX, Tax) -->
                            <template v-for="sec in reportData.sections" :key="sec.sectionKey">
                                <tr class="section-header-row">
                                    <td :colspan="3 + reportData.periods.length + 1">
                                        <i class="pi pi-angle-down mr-2 text-primary" />
                                        <span class="font-bold text-primary">{{ sec.title }}</span>
                                    </td>
                                </tr>

                                <tr v-for="row in sec.rows" :key="row.accountId" class="data-row hover:surface-100">
                                    <td class="sticky-col first-col pl-4">{{ row.name }}</td>
                                    <td class="sticky-col second-col font-mono text-500">{{ row.code }}</td>
                                    <td class="sticky-col third-col text-500 text-xs">{{ row.subcategory || '-' }}</td>
                                    <td
                                        v-for="(pKey, pIdx) in reportData.periods"
                                        :key="pIdx"
                                        class="text-right period-cell font-mono"
                                        :class="{ 'clickable-cell': Number(row.periodValues[pKey]) > 0 }"
                                        @click="openDrilldown(row, pKey, reportData.periodLabels[pIdx])"
                                    >
                                        {{ formatCurrency(row.periodValues[pKey]) }}
                                    </td>
                                    <td class="text-right total-cell font-bold font-mono">
                                        {{ formatCurrency(row.totalValue) }}
                                    </td>
                                </tr>

                                <!-- Subtotal Row -->
                                <tr v-if="sec.subtotalRow" class="subtotal-row">
                                    <td class="sticky-col first-col pl-4 font-bold">{{ sec.subtotalRow.name }}</td>
                                    <td class="sticky-col second-col"></td>
                                    <td class="sticky-col third-col"></td>
                                    <td v-for="(pKey, pIdx) in reportData.periods" :key="pIdx" class="text-right font-bold font-mono">
                                        {{ formatCurrency(sec.subtotalRow.periodValues[pKey]) }}
                                    </td>
                                    <td class="text-right font-bold font-mono text-900">
                                        {{ formatCurrency(sec.subtotalRow.totalValue) }}
                                    </td>
                                </tr>

                                <!-- GROSS PROFIT & MARGIN INSERTION AFTER COGS -->
                                <template v-if="sec.sectionKey === 'COGS'">
                                    <tr class="calculated-highlight-row">
                                        <td class="sticky-col first-col font-bold text-900 text-base">
                                            <i class="pi pi-star-fill text-yellow-500 mr-2" />
                                            {{ reportData.grossProfitRow?.name }}
                                        </td>
                                        <td class="sticky-col second-col"></td>
                                        <td class="sticky-col third-col"></td>
                                        <td
                                            v-for="(pKey, pIdx) in reportData.periods"
                                            :key="pIdx"
                                            class="text-right font-bold font-mono text-base"
                                            :class="Number(reportData.grossProfitRow?.periodValues[pKey]) >= 0 ? 'text-green-700' : 'text-red-700'"
                                        >
                                            {{ formatCurrency(reportData.grossProfitRow?.periodValues[pKey]) }}
                                        </td>
                                        <td class="text-right font-bold font-mono text-base text-green-800">
                                            {{ formatCurrency(reportData.grossProfitRow?.totalValue) }}
                                        </td>
                                    </tr>

                                    <tr class="margin-row">
                                        <td class="sticky-col first-col pl-4 font-semibold text-600 text-sm">
                                            {{ reportData.grossMarginPercentRow?.name }}
                                        </td>
                                        <td class="sticky-col second-col"></td>
                                        <td class="sticky-col third-col"></td>
                                        <td v-for="(pKey, pIdx) in reportData.periods" :key="pIdx" class="text-right font-mono text-sm text-600">
                                            {{ formatPercent(reportData.grossMarginPercentRow?.periodValues[pKey]) }}
                                        </td>
                                        <td class="text-right font-bold font-mono text-sm text-700">
                                            {{ formatPercent(reportData.grossMarginPercentRow?.totalValue) }}
                                        </td>
                                    </tr>
                                </template>

                                <!-- OPERATING INCOME (EBITDA) INSERTION AFTER OPEX -->
                                <template v-if="sec.sectionKey === 'OPEX'">
                                    <tr class="calculated-highlight-row">
                                        <td class="sticky-col first-col font-bold text-900 text-base">
                                            <i class="pi pi-chart-line text-blue-500 mr-2" />
                                            {{ reportData.operatingIncomeRow?.name }}
                                        </td>
                                        <td class="sticky-col second-col"></td>
                                        <td class="sticky-col third-col"></td>
                                        <td
                                            v-for="(pKey, pIdx) in reportData.periods"
                                            :key="pIdx"
                                            class="text-right font-bold font-mono text-base"
                                            :class="Number(reportData.operatingIncomeRow?.periodValues[pKey]) >= 0 ? 'text-blue-700' : 'text-red-700'"
                                        >
                                            {{ formatCurrency(reportData.operatingIncomeRow?.periodValues[pKey]) }}
                                        </td>
                                        <td class="text-right font-bold font-mono text-base text-blue-800">
                                            {{ formatCurrency(reportData.operatingIncomeRow?.totalValue) }}
                                        </td>
                                    </tr>

                                    <tr class="margin-row">
                                        <td class="sticky-col first-col pl-4 font-semibold text-600 text-sm">
                                            {{ reportData.operatingMarginPercentRow?.name }}
                                        </td>
                                        <td class="sticky-col second-col"></td>
                                        <td class="sticky-col third-col"></td>
                                        <td v-for="(pKey, pIdx) in reportData.periods" :key="pIdx" class="text-right font-mono text-sm text-600">
                                            {{ formatPercent(reportData.operatingMarginPercentRow?.periodValues[pKey]) }}
                                        </td>
                                        <td class="text-right font-bold font-mono text-sm text-700">
                                            {{ formatPercent(reportData.operatingMarginPercentRow?.totalValue) }}
                                        </td>
                                    </tr>
                                </template>
                            </template>

                            <!-- FINAL NET INCOME SECTION -->
                            <tr class="net-income-row">
                                <td class="sticky-col first-col font-bold text-white text-lg">
                                    <i class="pi pi-check-circle mr-2" />
                                    {{ reportData.netIncomeRow?.name }}
                                </td>
                                <td class="sticky-col second-col"></td>
                                <td class="sticky-col third-col"></td>
                                <td
                                    v-for="(pKey, pIdx) in reportData.periods"
                                    :key="pIdx"
                                    class="text-right font-bold font-mono text-lg text-white"
                                >
                                    {{ formatCurrency(reportData.netIncomeRow?.periodValues[pKey]) }}
                                </td>
                                <td class="text-right font-bold font-mono text-lg text-yellow-300">
                                    {{ formatCurrency(reportData.netIncomeRow?.totalValue) }}
                                </td>
                            </tr>

                            <tr class="net-margin-row">
                                <td class="sticky-col first-col pl-4 font-semibold text-sm">
                                    {{ reportData.netMarginPercentRow?.name }}
                                </td>
                                <td class="sticky-col second-col"></td>
                                <td class="sticky-col third-col"></td>
                                <td v-for="(pKey, pIdx) in reportData.periods" :key="pIdx" class="text-right font-mono text-sm font-semibold">
                                    {{ formatPercent(reportData.netMarginPercentRow?.periodValues[pKey]) }}
                                </td>
                                <td class="text-right font-bold font-mono text-sm font-bold">
                                    {{ formatPercent(reportData.netMarginPercentRow?.totalValue) }}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </GenericPanel>
        </div>
    </div>

    <!-- Generic File Upload Modal -->
    <GenericFileUploadDialog
        v-model:visible="showImportDialog"
        title="Import Financial Data (CSV Template)"
        description="Upload your transactions using the canonical P&L report template. Any unrecognized account codes will be automatically registered into the Chart of Accounts."
        :showTemplate="true"
        templateTitle="Canonical Report Template (CSV)"
        :onDownloadTemplate="downloadTemplate"
        :onUpload="handleCsvUpload"
        :role="['ROLE_ADMIN', 'ROLE_MANAGER']"
        @success="loadReport"
    />

    <!-- Transaction Drilldown Dialog -->
    <Dialog
        v-model:visible="showDrilldown"
        :header="drilldownTitle"
        :modal="true"
        :style="{ width: '800px' }"
    >
        <div v-if="drilldownLoading" class="flex justify-content-center p-5">
            <ProgressSpinner />
        </div>
        <DataTable
            v-else
            :value="drilldownRecords"
            paginator
            :rows="5"
            responsiveLayout="scroll"
            class="p-datatable-sm"
        >
            <template #empty>
                <div class="text-center p-3 text-500">No underlying transactions found.</div>
            </template>
            <Column field="entryDate" header="Date" sortable headerStyle="width: 8rem" />
            <Column field="referenceId" header="Reference" headerStyle="width: 9rem">
                <template #body="slotProps">
                    <Tag v-if="slotProps.data.referenceId" :value="slotProps.data.referenceId" severity="info" />
                    <span v-else class="text-400">-</span>
                </template>
            </Column>
            <Column field="description" header="Description" />
            <Column field="entityName" header="Entity" headerStyle="width: 7rem" />
            <Column field="amount" header="Amount" sortable class="text-right font-mono" headerStyle="width: 8rem">
                <template #body="slotProps">
                    <span class="font-bold font-mono">{{ formatCurrency(slotProps.data.amount) }}</span>
                </template>
            </Column>
        </DataTable>
        <template #footer>
            <Button label="Close" icon="pi pi-check" @click="showDrilldown = false" />
        </template>
    </Dialog>
</template>

<style scoped lang="scss">
.pnl-matrix-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.875rem;

    th, td {
        padding: 0.5rem 0.75rem;
        border-bottom: 1px solid var(--surface-border, #e2e8f0);
        white-space: nowrap;
    }

    .header-row {
        background-color: var(--surface-100, #f8fafc);
        font-weight: 600;
        color: var(--text-color, #1e293b);
    }

    .section-header-row {
        background-color: var(--primary-50, #eff6ff);
        font-size: 0.95rem;
        font-weight: 700;
        border-top: 2px solid var(--primary-200, #bfdbfe);
    }

    .subtotal-row {
        background-color: var(--surface-50, #f8fafc);
        border-top: 1px solid var(--surface-300, #cbd5e1);
        border-bottom: 2px solid var(--surface-300, #cbd5e1);
    }

    .calculated-highlight-row {
        background-color: var(--surface-100, #f1f5f9);
        border-top: 2px solid var(--surface-400, #94a3b8);
        border-bottom: 1px dashed var(--surface-300, #cbd5e1);
    }

    .margin-row {
        background-color: var(--surface-50, #f8fafc);
        border-bottom: 2px solid var(--surface-300, #cbd5e1);
    }

    .net-income-row {
        background-color: var(--primary-700, #1d4ed8);
        border-top: 3px double var(--primary-900, #1e3a8a);
    }

    .net-margin-row {
        background-color: var(--primary-100, #dbeafe);
        color: var(--primary-900, #1e3a8a);
        border-bottom: 3px double var(--primary-400, #60a5fa);
    }

    .period-col {
        min-width: 7.5rem;
    }

    .total-col {
        min-width: 8.5rem;
        background-color: var(--surface-200, #e2e8f0);
        font-weight: 700;
    }

    .total-cell {
        background-color: var(--surface-50, #f8fafc);
    }

    .clickable-cell {
        cursor: pointer;
        color: var(--primary-color, #2563eb);
        transition: background-color 0.15s;

        &:hover {
            background-color: var(--primary-100, #dbeafe);
            text-decoration: underline;
        }
    }
}
</style>
