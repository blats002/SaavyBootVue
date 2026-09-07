<script setup>
import { onMounted, reactive, ref, watch } from 'vue';
import ProductService from '@core/service/ProductService';
import { useLayout } from '@core/layout/composables/layout';
import StatsCard from '@core/components/dashboard/StatsCard.vue';
import axios from 'axios';

const { isDarkTheme } = useLayout();

const products = ref(null);
const lineData = reactive({
    labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
    datasets: [
        {
            label: 'First Dataset',
            data: [65, 59, 80, 81, 56, 55, 40],
            fill: false,
            backgroundColor: '#2f4860',
            borderColor: '#2f4860',
            tension: 0.4
        },
        {
            label: 'Second Dataset',
            data: [28, 48, 40, 19, 86, 27, 90],
            fill: false,
            backgroundColor: '#00bb7e',
            borderColor: '#00bb7e',
            tension: 0.4
        }
    ]
});
const items = ref([
    { label: 'Add New', icon: 'pi pi-fw pi-plus' },
    { label: 'Remove', icon: 'pi pi-fw pi-minus' }
]);
const lineOptions = ref(null);
const productService = new ProductService();

onMounted(() => {
    productService.getProductsSmall().then((data) => (products.value = data));
    loadCards();
});

const formatCurrency = (value) => {
    return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
};
const applyLightTheme = () => {
    lineOptions.value = {
        plugins: {
            legend: {
                labels: {
                    color: '#495057'
                }
            }
        },
        scales: {
            x: {
                ticks: {
                    color: '#495057'
                },
                grid: {
                    color: '#ebedef'
                }
            },
            y: {
                ticks: {
                    color: '#495057'
                },
                grid: {
                    color: '#ebedef'
                }
            }
        }
    };
};

const applyDarkTheme = () => {
    lineOptions.value = {
        plugins: {
            legend: {
                labels: {
                    color: '#ebedef'
                }
            }
        },
        scales: {
            x: {
                ticks: {
                    color: '#ebedef'
                },
                grid: {
                    color: 'rgba(160, 167, 181, .3)'
                }
            },
            y: {
                ticks: {
                    color: '#ebedef'
                },
                grid: {
                    color: 'rgba(160, 167, 181, .3)'
                }
            }
        }
    };
};

watch(
    isDarkTheme,
    (val) => {
        if (val) {
            applyDarkTheme();
        } else {
            applyLightTheme();
        }
    },
    { immediate: true }
);

const totalUsers = ref(null);
const usersInStock = ref(null);
const usersOutOfStock = ref(null);
const usersLowStock = ref(null);

const loadCards = async () => {
    try {
        const response = await axios.get('/api/users/count');
        totalUsers.value = response.data.total;
        usersInStock.value = response.data.inStock;
        usersOutOfStock.value = response.data.outOfStock;
        usersLowStock.value = response.data.lowStock;
    } catch (e) {
        console.error('Failed loading stats:', e);
    }
};
</script>

<template>
    <div class="grid">
<!--        <StatsCard title="Users" :total="totalUsers" :inStock="usersInStock" :outOfStock="usersOutOfStock" :lowStock="usersLowStock" icon="pi-users" entity="users" />-->
<!--        <StatsCard title="Roles" :total="totalUsers" :inStock="usersInStock" :outOfStock="usersOutOfStock" :lowStock="usersLowStock" icon="pi-shield" entity="roles" />-->
        <StatsCard title="Invoices" :total="totalUsers" :inStock="usersInStock" :outOfStock="usersOutOfStock" :lowStock="usersLowStock" icon="pi-file" entity="customer-invoices" />
        <StatsCard title="Payments" :total="totalUsers" :inStock="usersInStock" :outOfStock="usersOutOfStock" :lowStock="usersLowStock" icon="pi-dollar" entity="customer-payments" />
    </div>
</template>

<style scoped></style>
