<script setup>
import { onMounted, reactive, ref, watch } from 'vue';
import axios from 'axios';
import ProductService from '../service/ProductService';
import { useLayout } from '../layout/composables/layout';
import StatsCard from '../components/dashboard/StatsCard.vue';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

const { isDarkTheme } = useLayout();

const products = ref(null);
const cards = ref([]);
const loadingCards = ref(true);

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

const loadCards = async () => {
    loadingCards.value = true;
    try {
        const response = await axios.get(`${SERVER_URL}/api/dashboard/cards`);
        if (response.data && Array.isArray(response.data)) {
            cards.value = response.data;
        }
    } catch (e) {
        console.error('Failed loading dashboard cards:', e);
    } finally {
        loadingCards.value = false;
    }
};

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
</script>

<template>
    <div class="grid">
        <!-- Dynamic Cards from Host and Active Plugins -->
        <template v-if="cards && cards.length > 0">
            <div v-for="(card, index) in cards" :key="index" class="col-12 lg:col-6 xl:col-3">
                <StatsCard 
                    :title="card.title" 
                    :content="card.content" 
                    :icon="card.icon" 
                    :footer="card.footer" 
                    :role="card.role"
                />
            </div>
        </template>
<!--        <template v-else>-->
<!--            <div class="col-12 lg:col-6 xl:col-3">-->
<!--                <div class="card mb-0">-->
<!--                    <div class="flex justify-content-between mb-3">-->
<!--                        <div>-->
<!--                            <span class="block text-500 font-medium mb-3">Orders</span>-->
<!--                            <div class="text-900 font-medium text-xl">152</div>-->
<!--                        </div>-->
<!--                        <div class="flex align-items-center justify-content-center bg-blue-100 border-round" style="width: 2.5rem; height: 2.5rem">-->
<!--                            <i class="pi pi-shopping-cart text-blue-500 text-xl"></i>-->
<!--                        </div>-->
<!--                    </div>-->
<!--                    <span class="text-green-500 font-medium">24 new </span>-->
<!--                    <span class="text-500">since last visit</span>-->
<!--                </div>-->
<!--            </div>-->
<!--            <div class="col-12 lg:col-6 xl:col-3">-->
<!--                <div class="card mb-0">-->
<!--                    <div class="flex justify-content-between mb-3">-->
<!--                        <div>-->
<!--                            <span class="block text-500 font-medium mb-3">Revenue</span>-->
<!--                            <div class="text-900 font-medium text-xl">$2,100</div>-->
<!--                        </div>-->
<!--                        <div class="flex align-items-center justify-content-center bg-orange-100 border-round" style="width: 2.5rem; height: 2.5rem">-->
<!--                            <i class="pi pi-map-marker text-orange-500 text-xl"></i>-->
<!--                        </div>-->
<!--                    </div>-->
<!--                    <span class="text-green-500 font-medium">%52+ </span>-->
<!--                    <span class="text-500">since last week</span>-->
<!--                </div>-->
<!--            </div>-->
<!--            <div class="col-12 lg:col-6 xl:col-3">-->
<!--                <div class="card mb-0">-->
<!--                    <div class="flex justify-content-between mb-3">-->
<!--                        <div>-->
<!--                            <span class="block text-500 font-medium mb-3">Customers</span>-->
<!--                            <div class="text-900 font-medium text-xl">28,441</div>-->
<!--                        </div>-->
<!--                        <div class="flex align-items-center justify-content-center bg-cyan-100 border-round" style="width: 2.5rem; height: 2.5rem">-->
<!--                            <i class="pi pi-inbox text-cyan-500 text-xl"></i>-->
<!--                        </div>-->
<!--                    </div>-->
<!--                    <span class="text-green-500 font-medium">520 </span>-->
<!--                    <span class="text-500">newly registered</span>-->
<!--                </div>-->
<!--            </div>-->
<!--            <div class="col-12 lg:col-6 xl:col-3">-->
<!--                <div class="card mb-0">-->
<!--                    <div class="flex justify-content-between mb-3">-->
<!--                        <div>-->
<!--                            <span class="block text-500 font-medium mb-3">Comments</span>-->
<!--                            <div class="text-900 font-medium text-xl">152 Unread</div>-->
<!--                        </div>-->
<!--                        <div class="flex align-items-center justify-content-center bg-purple-100 border-round" style="width: 2.5rem; height: 2.5rem">-->
<!--                            <i class="pi pi-comment text-purple-500 text-xl"></i>-->
<!--                        </div>-->
<!--                    </div>-->
<!--                    <span class="text-green-500 font-medium">85 </span>-->
<!--                    <span class="text-500">responded</span>-->
<!--                </div>-->
<!--            </div>-->
<!--        </template>-->

<!--        <div class="col-12 xl:col-6">-->
<!--            <div class="card">-->
<!--                <h5>Recent Activity</h5>-->
<!--                <DataTable :value="products" :rows="5" :paginator="true" responsiveLayout="scroll">-->
<!--                    <Column style="width: 15%">-->
<!--                        <template #header> Image </template>-->
<!--                        <template #body="slotProps">-->
<!--                            <img :src="'demo/images/product/' + slotProps.data.image" :alt="slotProps.data.image" width="50" class="shadow-2" />-->
<!--                        </template>-->
<!--                    </Column>-->
<!--                    <Column field="name" header="Name" :sortable="true" style="width: 35%"></Column>-->
<!--                    <Column field="price" header="Price" :sortable="true" style="width: 35%">-->
<!--                        <template #body="slotProps">-->
<!--                            {{ formatCurrency(slotProps.data.price) }}-->
<!--                        </template>-->
<!--                    </Column>-->
<!--                    <Column style="width: 15%">-->
<!--                        <template #header> View </template>-->
<!--                        <template #body>-->
<!--                            <Button icon="pi pi-search" type="button" class="p-button-text"></Button>-->
<!--                        </template>-->
<!--                    </Column>-->
<!--                </DataTable>-->
<!--            </div>-->
<!--            <div class="card">-->
<!--                <div class="flex justify-content-between align-items-center mb-5">-->
<!--                    <h5>Overview</h5>-->
<!--                    <div>-->
<!--                        <Button icon="pi pi-ellipsis-v" class="p-button-text p-button-plain p-button-rounded" @click="$refs.menu2.toggle($event)"></Button>-->
<!--                        <Menu ref="menu2" :popup="true" :model="items"></Menu>-->
<!--                    </div>-->
<!--                </div>-->
<!--                <ul class="list-none p-0 m-0">-->
<!--                    <li class="flex flex-column md:flex-row md:align-items-center md:justify-content-between mb-4">-->
<!--                        <div>-->
<!--                            <span class="text-900 font-medium mr-2 mb-1 md:mb-0">Product Alpha</span>-->
<!--                            <div class="mt-1 text-600">Electronics</div>-->
<!--                        </div>-->
<!--                        <div class="mt-2 md:mt-0 flex align-items-center">-->
<!--                            <div class="surface-300 border-round overflow-hidden w-10rem lg:w-6rem" style="height: 8px">-->
<!--                                <div class="bg-orange-500 h-full" style="width: 50%"></div>-->
<!--                            </div>-->
<!--                            <span class="text-orange-500 ml-3 font-medium">%50</span>-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li class="flex flex-column md:flex-row md:align-items-center md:justify-content-between mb-4">-->
<!--                        <div>-->
<!--                            <span class="text-900 font-medium mr-2 mb-1 md:mb-0">Product Beta</span>-->
<!--                            <div class="mt-1 text-600">Services</div>-->
<!--                        </div>-->
<!--                        <div class="mt-2 md:mt-0 ml-0 md:ml-8 flex align-items-center">-->
<!--                            <div class="surface-300 border-round overflow-hidden w-10rem lg:w-6rem" style="height: 8px">-->
<!--                                <div class="bg-cyan-500 h-full" style="width: 16%"></div>-->
<!--                            </div>-->
<!--                            <span class="text-cyan-500 ml-3 font-medium">%16</span>-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li class="flex flex-column md:flex-row md:align-items-center md:justify-content-between mb-4">-->
<!--                        <div>-->
<!--                            <span class="text-900 font-medium mr-2 mb-1 md:mb-0">Product Gamma</span>-->
<!--                            <div class="mt-1 text-600">Hardware</div>-->
<!--                        </div>-->
<!--                        <div class="mt-2 md:mt-0 ml-0 md:ml-8 flex align-items-center">-->
<!--                            <div class="surface-300 border-round overflow-hidden w-10rem lg:w-6rem" style="height: 8px">-->
<!--                                <div class="bg-green-500 h-full" style="width: 35%"></div>-->
<!--                            </div>-->
<!--                            <span class="text-green-500 ml-3 font-medium">%35</span>-->
<!--                        </div>-->
<!--                    </li>-->
<!--                </ul>-->
<!--            </div>-->
<!--        </div>-->
<!--        <div class="col-12 xl:col-6">-->
<!--            <div class="card">-->
<!--                <h5>Activity Trends</h5>-->
<!--                <Chart type="line" :data="lineData" :options="lineOptions" />-->
<!--            </div>-->
<!--            <div class="card">-->
<!--                <div class="flex align-items-center justify-content-between mb-4">-->
<!--                    <h5>Notifications</h5>-->
<!--                    <div>-->
<!--                        <Button icon="pi pi-ellipsis-v" class="p-button-text p-button-plain p-button-rounded" @click="$refs.menu1.toggle($event)"></Button>-->
<!--                        <Menu ref="menu1" :popup="true" :model="items"></Menu>-->
<!--                    </div>-->
<!--                </div>-->

<!--                <span class="block text-600 font-medium mb-3">TODAY</span>-->
<!--                <ul class="p-0 mx-0 mt-0 mb-4 list-none">-->
<!--                    <li class="flex align-items-center py-2 border-bottom-1 surface-border">-->
<!--                        <div class="w-3rem h-3rem flex align-items-center justify-content-center bg-blue-100 border-circle mr-3 flex-shrink-0">-->
<!--                            <i class="pi pi-check-circle text-xl text-blue-500"></i>-->
<!--                        </div>-->
<!--                        <span class="text-900 line-height-3"-->
<!--                            >System Health Check-->
<!--                            <span class="text-700">completed successfully</span>-->
<!--                        </span>-->
<!--                    </li>-->
<!--                </ul>-->
<!--            </div>-->
<!--        </div>-->
    </div>
</template>
