<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '../../service/JPAService';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import ProgressSpinner from 'primevue/progressspinner';
import GenericMasterDetail from '../../components/GenericMasterDetail.vue';

const props = defineProps({
    tabs: {
        type: Array,
        default: () => []
    }
});

const isDataLoaded = ref(false);
const loadedTabs = ref([]);

onMounted(async () => {
    try {
        const promises = props.tabs.map(async (tab) => {
            const service = typeof tab.service === 'string' ? createJpaService(tab.service) : (tab.service || createJpaService(tab.endpoint));
            const [master, details] = await Promise.all([
                service.getMasterMeta(),
                service.getDetailMeta()
            ]);
            return {
                header: tab.header,
                title: tab.title || tab.header,
                subtitle: tab.subtitle || '',
                layout: tab.layout || 'modal',
                master,
                details
            };
        });
        loadedTabs.value = await Promise.all(promises);
        isDataLoaded.value = true;
    } catch (e) {
        console.error('Failed loading tab metadata:', e);
        isDataLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isDataLoaded">
        <TabView class="generic-panel-body">
            <TabPanel v-for="tab in loadedTabs" :key="tab.header" :header="tab.header">
                <GenericMasterDetail
                    :title="tab.title"
                    :subtitle="tab.subtitle"
                    :layout="tab.layout"
                    :master="tab.master"
                    :details="tab.details"
                />
            </TabPanel>
        </TabView>
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>

<style scoped></style>
