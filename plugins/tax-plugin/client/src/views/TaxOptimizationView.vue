<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import Card from 'primevue/card';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';
import Tag from 'primevue/tag';
import ProgressSpinner from 'primevue/progressspinner';
import Divider from 'primevue/divider';

const loading = ref(false);
const selectedYear = ref(2025);
const selectedBusinessType = ref('SOLE_PROPRIETOR');

const years = [
    { label: '2026 (Upcoming / Q1)', value: 2026 },
    { label: '2025 (Annual Tax Filing)', value: 2025 },
    { label: '2024 (Prior Fiscal Year)', value: 2024 }
];

const businessTypes = [
    { label: 'Sole Proprietorship / Self-Employed / Freelancer', value: 'SOLE_PROPRIETOR' },
    { label: 'Mixed Income (Compensation + Trade/Business)', value: 'MIXED_INCOME' },
    { label: 'Domestic Corporation / Partnership (CREATE Act)', value: 'DOMESTIC_CORPORATION' }
];

const simulationData = ref(null);

const grossRevenue = ref(1850000);
const cogs = ref(420000);
const opex = ref(510000);
const cwt2307 = ref(37000);

const fetchSimulation = async () => {
    loading.value = true;
    try {
        const res = await axios.get('/api/tax/simulate', {
            params: {
                year: selectedYear.value,
                grossRevenue: grossRevenue.value,
                cogs: cogs.value,
                opex: opex.value,
                cwt2307: cwt2307.value
            }
        });
        simulationData.value = res.data;
    } catch (e) {
        console.error('Failed to simulate tax regimes:', e);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchSimulation();
});

const formatMoney = (val) => {
    if (val === null || val === undefined) return '₱0.00';
    return '₱' + Number(val).toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};
</script>

<template>
    <div class="card p-4">
        <!-- Header Banner -->
        <div class="flex flex-column md:flex-row md:align-items-center justify-content-between gap-3 mb-4">
            <div>
                <h2 class="m-0 font-bold text-900 flex align-items-center gap-2">
                    <i class="pi pi-compass text-primary text-2xl"></i>
                    Tax Regime Optimization Simulator
                </h2>
                <p class="text-500 m-0 mt-1">
                    Compare Philippine BIR tax regimes (8% Flat Rate vs. 40% OSD vs. Itemized Deductions) in real-time
                </p>
            </div>
            <div class="flex align-items-center gap-2">
                <Dropdown v-model="selectedYear" :options="years" optionLabel="label" optionValue="value" @change="fetchSimulation" class="w-14rem" />
                <Button label="Recalculate" icon="pi pi-refresh" severity="primary" :loading="loading" @click="fetchSimulation" />
            </div>
        </div>

        <!-- Simulation Inputs Card -->
        <div class="surface-ground p-3 border-round mb-4 border-1 surface-border">
            <div class="grid p-fluid">
                <div class="col-12 md:col-3">
                    <label class="font-semibold text-700 block mb-2">Gross Revenue / Receipts</label>
                    <InputNumber v-model="grossRevenue" mode="currency" currency="PHP" locale="en-PH" @input="fetchSimulation" />
                </div>
                <div class="col-12 md:col-3">
                    <label class="font-semibold text-700 block mb-2">Cost of Goods Sold (COGS)</label>
                    <InputNumber v-model="cogs" mode="currency" currency="PHP" locale="en-PH" @input="fetchSimulation" />
                </div>
                <div class="col-12 md:col-3">
                    <label class="font-semibold text-700 block mb-2">Allowable Operating Expenses (OPEX)</label>
                    <InputNumber v-model="opex" mode="currency" currency="PHP" locale="en-PH" @input="fetchSimulation" />
                </div>
                <div class="col-12 md:col-3">
                    <label class="font-semibold text-700 block mb-2">Form 2307 CWT Credits</label>
                    <InputNumber v-model="cwt2307" mode="currency" currency="PHP" locale="en-PH" @input="fetchSimulation" />
                </div>
            </div>
        </div>

        <!-- Recommendation Banner -->
        <div v-if="simulationData" class="p-3 mb-4 border-round flex align-items-center justify-content-between" style="background: rgba(16, 185, 129, 0.1); border-left: 5px solid #10b981;">
            <div class="flex align-items-center gap-3">
                <i class="pi pi-check-circle text-green-500 text-3xl"></i>
                <div>
                    <div class="font-bold text-900 text-lg">Recommended Optimal Choice: {{ simulationData.recommendedRegime }}</div>
                    <div class="text-700 text-sm mt-1">{{ simulationData.recommendationSummary }}</div>
                </div>
            </div>
            <div class="text-right">
                <span class="text-500 text-xs block">Estimated Maximum Tax Savings</span>
                <span class="text-green-600 font-bold text-xl">{{ formatMoney(simulationData.maxEstimatedSavings) }}</span>
            </div>
        </div>

        <!-- Comparative Regime Cards -->
        <div v-if="simulationData && simulationData.regimeComparisons" class="grid mb-4">
            <div
                v-for="regime in simulationData.regimeComparisons"
                :key="regime.regime"
                class="col-12 md:col-4"
            >
                <div
                    class="p-4 border-round h-full flex flex-column justify-content-between border-2 transition-all shadow-1"
                    :style="regime.isRecommended ? 'border-color: #10b981; background: #ffffff;' : 'border-color: var(--surface-border); background: var(--surface-card);'"
                >
                    <div>
                        <div class="flex align-items-center justify-content-between mb-3">
                            <h3 class="m-0 font-bold text-900 text-lg">{{ regime.regimeName }}</h3>
                            <Tag v-if="regime.isRecommended" value="OPTIMAL" severity="success" icon="pi pi-star-fill" />
                            <Tag v-else value="ALTERNATIVE" severity="secondary" />
                        </div>
                        <p class="text-600 text-xs line-height-3 mb-3">{{ regime.description }}</p>

                        <Divider class="my-2" />

                        <div class="space-y-2">
                            <div class="flex justify-content-between text-sm py-1">
                                <span class="text-600">Gross Receipts:</span>
                                <span class="font-semibold text-900">{{ formatMoney(regime.grossRevenue) }}</span>
                            </div>
                            <div class="flex justify-content-between text-sm py-1">
                                <span class="text-600">Deductions / Allowance:</span>
                                <span class="font-semibold text-orange-600">- {{ formatMoney(regime.allowableDeductions) }}</span>
                            </div>
                            <div class="flex justify-content-between text-sm py-1">
                                <span class="text-600">Taxable Net Income:</span>
                                <span class="font-bold text-900">{{ formatMoney(regime.taxableIncome) }}</span>
                            </div>
                            <div class="flex justify-content-between text-sm py-1">
                                <span class="text-600">Income Tax Due:</span>
                                <span class="font-semibold text-900">{{ formatMoney(regime.incomeTaxDue) }}</span>
                            </div>
                            <div class="flex justify-content-between text-sm py-1">
                                <span class="text-600">Percentage Tax (2551Q 3%):</span>
                                <span class="font-semibold" :class="regime.percentageTax2551Q > 0 ? 'text-900' : 'text-green-600'">
                                    {{ regime.percentageTax2551Q > 0 ? formatMoney(regime.percentageTax2551Q) : 'EXEMPT (₱0.00)' }}
                                </span>
                            </div>
                            <div class="flex justify-content-between text-sm py-1 border-top-1 surface-border pt-2">
                                <span class="text-600">Total Tax Liability:</span>
                                <span class="font-bold text-primary text-base">{{ formatMoney(regime.totalTaxLiability) }}</span>
                            </div>
                            <div class="flex justify-content-between text-sm py-1">
                                <span class="text-600">Less: Form 2307 CWT:</span>
                                <span class="font-semibold text-green-600">- {{ formatMoney(regime.cwt2307Credits) }}</span>
                            </div>
                        </div>

                        <Divider class="my-3" />

                        <div class="surface-ground p-3 border-round flex justify-content-between align-items-center">
                            <span class="font-bold text-800 text-sm">Net Tax Payable:</span>
                            <span class="font-bold text-xl" :class="regime.isRecommended ? 'text-green-600' : 'text-900'">
                                {{ formatMoney(regime.netTaxPayable) }}
                            </span>
                        </div>
                    </div>

                    <div class="mt-4">
                        <ul class="p-0 m-0 list-none text-xs text-600 line-height-3">
                            <li v-for="(highlight, i) in regime.highlights" :key="i" class="flex align-items-center gap-2 mb-1">
                                <i class="pi pi-check text-green-500 text-xs"></i>
                                <span>{{ highlight }}</span>
                            </li>
                        </ul>

                        <div class="mt-3">
                            <router-link :to="'/tax/form-1701a?method=' + (regime.regime === 'FLAT_8_PERCENT' ? '8_PERCENT' : (regime.regime === 'OSD_40_PERCENT' ? 'OSD' : 'ITEMIZED'))">
                                <Button
                                    :label="'Generate Form 1701A (' + (regime.regime === 'FLAT_8_PERCENT' ? '8%' : (regime.regime === 'OSD_40_PERCENT' ? '40% OSD' : 'Itemized')) + ')'"
                                    icon="pi pi-file-pdf"
                                    :severity="regime.isRecommended ? 'success' : 'outlined'"
                                    class="w-full text-xs font-bold"
                                />
                            </router-link>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Schedule 2 Itemized Deductions Reference -->
        <div v-if="simulationData && simulationData.birSchedule2Deductions" class="card mt-4 p-4 surface-card border-1 surface-border">
            <h3 class="font-bold text-900 text-lg mb-2 flex align-items-center gap-2">
                <i class="pi pi-list text-primary"></i>
                BIR Schedule 2 - Itemized Deductions Breakdown (Audited OPEX Rollup)
            </h3>
            <p class="text-500 text-xs mb-3">
                Transactions uploaded and categorized under official tax buckets roll up directly into these allowable deduction schedules.
            </p>
            <div class="grid">
                <div v-for="(amount, label) in simulationData.birSchedule2Deductions" :key="label" class="col-12 md:col-4">
                    <div class="p-2 border-round surface-ground flex justify-content-between align-items-center text-sm">
                        <span class="text-700 text-xs font-semibold">{{ label }}</span>
                        <span class="font-bold text-900">{{ formatMoney(amount) }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
