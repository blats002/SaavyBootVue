<script setup>
import { computed, onMounted, ref } from 'vue';
import axios from 'axios';
import StatsCard from '../components/dashboard/StatsCard.vue';
import DashboardChart from '../components/dashboard/DashboardChart.vue';
import DashboardTable from '../components/dashboard/DashboardTable.vue';
import { pluginState } from '../plugins/pluginState';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

const cards = ref([]);
const charts = ref([]);
const tables = ref([]);
const tabTitles = ref({});
const loading = ref(true);

const visibleCards = computed(() => {
    return cards.value.filter((card) => !card.plugin || pluginState.isPluginEnabled(card.plugin));
});

const visibleCharts = computed(() => {
    return charts.value.filter((chart) => !chart.plugin || pluginState.isPluginEnabled(chart.plugin));
});

const visibleTables = computed(() => {
    return tables.value.filter((table) => !table.plugin || pluginState.isPluginEnabled(table.plugin));
});

const formatPluginTitle = (name) => {
    if (!name) return 'General';
    return name
        .split(/[-_]/)
        .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
        .join(' ');
};

const pluginTabs = computed(() => {
    const plugins = new Set();
    visibleCards.value.forEach((c) => {
        if (c.plugin) plugins.add(c.plugin);
    });
    visibleCharts.value.forEach((c) => {
        if (c.plugin) plugins.add(c.plugin);
    });
    visibleTables.value.forEach((t) => {
        if (t.plugin) plugins.add(t.plugin);
    });

    const hasCore =
        visibleCards.value.some((c) => !c.plugin) ||
        visibleCharts.value.some((c) => !c.plugin) ||
        visibleTables.value.some((t) => !t.plugin);

    const tabs = [];

    if (hasCore) {
        tabs.push({
            id: 'core',
            title: tabTitles.value['core'] || 'General',
            icon: 'pi pi-chart-line'
        });
    }

    plugins.forEach((p) => {
        const title = tabTitles.value[p] || formatPluginTitle(p);
        tabs.push({
            id: p,
            title,
            icon: 'pi pi-box'
        });
    });

    return tabs;
});

const getCardsForPlugin = (pluginId) => {
    if (pluginId === 'core') return visibleCards.value.filter((c) => !c.plugin);
    return visibleCards.value.filter((c) => c.plugin === pluginId);
};

const getChartsForPlugin = (pluginId) => {
    if (pluginId === 'core') return visibleCharts.value.filter((c) => !c.plugin);
    return visibleCharts.value.filter((c) => c.plugin === pluginId);
};

const getTablesForPlugin = (pluginId) => {
    if (pluginId === 'core') return visibleTables.value.filter((t) => !t.plugin);
    return visibleTables.value.filter((t) => t.plugin === pluginId);
};

const loadDashboardData = async () => {
    loading.value = true;
    try {
        await pluginState.fetchActivePlugins();
        const [cardsRes, chartsRes, tablesRes, tabsRes] = await Promise.allSettled([
            axios.get(`${SERVER_URL}/api/dashboard/cards`),
            axios.get(`${SERVER_URL}/api/dashboard/charts`),
            axios.get(`${SERVER_URL}/api/dashboard/tables`),
            axios.get(`${SERVER_URL}/api/dashboard/tabs`)
        ]);

        if (cardsRes.status === 'fulfilled' && Array.isArray(cardsRes.value?.data)) {
            cards.value = cardsRes.value.data;
        }
        if (chartsRes.status === 'fulfilled' && Array.isArray(chartsRes.value?.data)) {
            charts.value = chartsRes.value.data;
        }
        if (tablesRes.status === 'fulfilled' && Array.isArray(tablesRes.value?.data)) {
            tables.value = tablesRes.value.data;
        }
        if (tabsRes.status === 'fulfilled' && typeof tabsRes.value?.data === 'object' && tabsRes.value?.data !== null) {
            tabTitles.value = tabsRes.value.data;
        }
    } catch (e) {
        console.error('Failed loading dashboard data:', e);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadDashboardData();
});
</script>

<template>
    <div v-if="loading" class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>

    <div v-else>
        <!-- No widgets state -->
        <div v-if="pluginTabs.length === 0" class="card text-center p-5 text-color-secondary">
            <i class="pi pi-chart-bar text-4xl mb-3"></i>
            <p>No dashboard widgets available.</p>
        </div>

        <!-- Plugin Tabs Dashboard -->
        <TabView v-else>
            <TabPanel v-for="tab in pluginTabs" :key="tab.id">
                <template #header>
                    <i :class="tab.icon" class="mr-2"></i>
                    <span>{{ tab.title }}</span>
                </template>

                <!-- Metric Cards Section -->
                <div class="grid mt-2" v-if="getCardsForPlugin(tab.id).length > 0">
                    <div v-for="(card, index) in getCardsForPlugin(tab.id)" :key="'card-' + tab.id + '-' + index" class="col-12 lg:col-6 xl:col-3">
                        <StatsCard :title="card.title" :content="card.content" :icon="card.icon" :footer="card.footer" :role="card.role" :plugin="card.plugin" />
                    </div>
                </div>

                <!-- Charts Section -->
                <div class="grid" v-if="getChartsForPlugin(tab.id).length > 0">
                    <div v-for="(chart, index) in getChartsForPlugin(tab.id)" :key="'chart-' + tab.id + '-' + (chart.id || index)" :class="chart.colSpan || 'col-12 xl:col-6'">
                        <DashboardChart :title="chart.title" :type="chart.type || 'line'" :data="chart.data" :options="chart.options" :role="chart.role" :plugin="chart.plugin" />
                    </div>
                </div>

                <!-- Data Tables Section -->
                <div class="grid" v-if="getTablesForPlugin(tab.id).length > 0">
                    <div v-for="(table, index) in getTablesForPlugin(tab.id)" :key="'table-' + tab.id + '-' + (table.id || index)" :class="table.colSpan || 'col-12 xl:col-6'">
                        <DashboardTable :title="table.title" :columns="table.columns || []" :data="table.data || []" :paginator="table.paginator !== false" :rows="table.rows || 5" :action="table.action" :role="table.role" :plugin="table.plugin" />
                    </div>
                </div>

                <!-- Empty State for Tab -->
                <div v-if="getCardsForPlugin(tab.id).length === 0 && getChartsForPlugin(tab.id).length === 0 && getTablesForPlugin(tab.id).length === 0" class="card text-center p-5 text-color-secondary">
                    <i class="pi pi-inbox text-4xl mb-3"></i>
                    <p>No dashboard widgets available for {{ tab.title }}.</p>
                </div>
            </TabPanel>
        </TabView>
    </div>
</template>

<style scoped></style>
