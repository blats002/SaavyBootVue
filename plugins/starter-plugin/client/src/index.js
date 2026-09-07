import SampleItemManagement from './views/pages/SampleItemManagement.vue';
import Dashboard from './views/Dashboard.vue';

export default {
    name: 'starter-plugin',
    routes: [
        {
            path: '/pages/sample-items',
            name: 'sample-item-management',
            component: SampleItemManagement,
            meta: { breadcrumb: ['Pages', 'Sample Item Management'] }
        }
    ],
    menu: [
        {
            label: 'Starter Features',
            items: [
                {
                    label: 'Dashboard',
                    icon: 'pi pi-fw pi-home',
                    to: '/'
                },
                {
                    label: 'Sample Items',
                    icon: 'pi pi-fw pi-box',
                    to: '/pages/sample-items'
                }
            ]
        }
    ],
    dashboard: Dashboard
};
