<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import ProgressSpinner from 'primevue/progressspinner';

const route = useRoute();
const method = ref(route.query.method || '8_PERCENT');
const loading = ref(false);
const formData = ref(null);

const fetchForm = async () => {
    loading.value = true;
    try {
        const res = await axios.get('/api/tax/form-1701a', {
            params: {
                year: 2025,
                method: method.value
            }
        });
        formData.value = res.data;
    } catch (e) {
        console.error('Failed to load Form 1701A:', e);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchForm();
});

const printForm = () => {
    window.print();
};

const formatMoney = (val) => {
    if (val === null || val === undefined) return '0.00';
    return Number(val).toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};
</script>

<template>
    <div class="p-4">
        <!-- Actions Topbar -->
        <div class="flex justify-content-between align-items-center mb-4 no-print">
            <div class="flex align-items-center gap-3">
                <router-link to="/tax/optimize">
                    <Button icon="pi pi-arrow-left" label="Back to Simulator" severity="secondary" outlined />
                </router-link>
                <h2 class="m-0 font-bold text-900">BIR Form No. 1701A (Annual Income Tax Return)</h2>
            </div>
            <div class="flex align-items-center gap-2">
                <Button label="Print / Save as PDF" icon="pi pi-print" severity="primary" @click="printForm" />
            </div>
        </div>

        <!-- Official BIR Form Style Layout -->
        <div v-if="formData" class="surface-card p-5 border-round shadow-2 border-1 surface-border max-w-60rem mx-auto printable-area">
            <!-- Header -->
            <div class="border-2 surface-border p-3 text-center mb-4" style="border-color: #1e293b !important;">
                <div class="text-xs font-bold uppercase tracking-wider text-700">Republic of the Philippines • Department of Finance</div>
                <div class="text-sm font-bold uppercase text-900">Bureau of Internal Revenue</div>
                <h1 class="text-2xl font-bold my-1 text-900">Annual Income Tax Return (BIR Form No. 1701A)</h1>
                <div class="text-xs font-semibold text-600">For Individuals Earning Purely from Business/Profession (TRAIN Act)</div>
            </div>

            <!-- Taxpayer Info -->
            <div class="border-1 surface-border mb-3 p-3">
                <div class="grid">
                    <div class="col-4">
                        <span class="text-xs text-600 block">1. Tax Year</span>
                        <span class="font-bold text-900">{{ formData.taxYear }}</span>
                    </div>
                    <div class="col-4">
                        <span class="text-xs text-600 block">2. TIN</span>
                        <span class="font-bold text-900">{{ formData.tin }}</span>
                    </div>
                    <div class="col-4">
                        <span class="text-xs text-600 block">3. RDO Code</span>
                        <span class="font-bold text-900">{{ formData.rdoCode }}</span>
                    </div>
                    <div class="col-8 mt-2">
                        <span class="text-xs text-600 block">4. Taxpayer's Name</span>
                        <span class="font-bold text-900">{{ formData.taxpayerName }}</span>
                    </div>
                    <div class="col-4 mt-2">
                        <span class="text-xs text-600 block">5. Selected Method</span>
                        <Tag :value="formData.selectedMethod" severity="info" />
                    </div>
                </div>
            </div>

            <!-- Computation of Tax Table -->
            <div class="border-1 surface-border mb-3">
                <div class="bg-primary-50 p-2 font-bold text-900 text-sm border-bottom-1 surface-border">
                    Part IV — Computation of Tax
                </div>
                <table class="w-full text-sm">
                    <tbody>
                        <tr class="border-bottom-1 surface-border">
                            <td class="p-2 text-700">36. Gross Sales / Receipts / Revenues / Fees</td>
                            <td class="p-2 text-right font-bold text-900">₱ {{ formatMoney(formData.grossSalesReceipts) }}</td>
                        </tr>
                        <tr class="border-bottom-1 surface-border">
                            <td class="p-2 text-700">37. Less: Allowable Reduction (₱250k for 8% / 40% OSD)</td>
                            <td class="p-2 text-right font-semibold text-orange-600">- ₱ {{ formatMoney(formData.allowableReduction) }}</td>
                        </tr>
                        <tr class="border-bottom-1 surface-border bg-surface-50 font-bold">
                            <td class="p-2 text-900">38. Taxable Income</td>
                            <td class="p-2 text-right text-900">₱ {{ formatMoney(formData.taxableIncome) }}</td>
                        </tr>
                        <tr class="border-bottom-1 surface-border">
                            <td class="p-2 text-700">39. Applicable Tax Rate</td>
                            <td class="p-2 text-right font-semibold text-900">{{ formData.taxRate ? formData.taxRate + '%' : 'Graduated Table' }}</td>
                        </tr>
                        <tr class="border-bottom-1 surface-border font-bold">
                            <td class="p-2 text-900">40. Total Income Tax Due</td>
                            <td class="p-2 text-right text-primary">₱ {{ formatMoney(formData.incomeTaxDue) }}</td>
                        </tr>
                        <tr class="border-bottom-1 surface-border">
                            <td class="p-2 text-700">41. Less: Creditable Tax Withheld (BIR Form 2307)</td>
                            <td class="p-2 text-right font-semibold text-green-600">- ₱ {{ formatMoney(formData.creditableTaxWithheld2307) }}</td>
                        </tr>
                        <tr class="bg-primary text-white font-bold text-base">
                            <td class="p-3">42. NET TAX PAYABLE / (OVERPAYMENT)</td>
                            <td class="p-3 text-right">₱ {{ formatMoney(formData.netTaxPayable) }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="text-xs text-500 text-center mt-4">
                I declare under the penalties of perjury that this return has been made in good faith, verified by me, and to the best of my knowledge and belief, is true and correct.
            </div>
        </div>
        <div v-else class="card flex justify-content-center p-5">
            <ProgressSpinner />
        </div>
    </div>
</template>

<style scoped>
@media print {
    .no-print {
        display: none !important;
    }
    .printable-area {
        box-shadow: none !important;
        border: none !important;
        width: 100% !important;
        max-width: 100% !important;
        padding: 0 !important;
    }
}
</style>
