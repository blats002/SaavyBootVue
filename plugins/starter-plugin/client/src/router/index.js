import { createSaavyRouter } from '@core/router';
import SampleItemManagement from '@/views/pages/SampleItemManagement.vue';
import Dashboard from '@/views/Dashboard.vue';

export const routes = [
    {
        path: '/pages/sample-items',
        name: 'sample-item-management',
        component: SampleItemManagement,
        meta: { breadcrumb: ['Pages', 'Sample Item Management'] }
    }
];

export const router = createSaavyRouter({
    routes: routes,
    dashboard: Dashboard
});

export default router;
