import TaxOptimizationView from './views/TaxOptimizationView.vue';
import BirForm1701AView from './views/BirForm1701AView.vue';
import BirForm1702RtView from './views/BirForm1702RtView.vue';

export default {
    name: 'tax-plugin',
    routes: [
        {
            path: '/tax/optimize',
            name: 'tax-optimization',
            component: TaxOptimizationView,
            meta: { breadcrumb: ['Tax Compliance', 'Optimization Simulator'] }
        },
        {
            path: '/tax/form-1701a',
            name: 'tax-form-1701a',
            component: BirForm1701AView,
            meta: { breadcrumb: ['Tax Compliance', 'BIR Form 1701A'] }
        },
        {
            path: '/tax/form-1702rt',
            name: 'tax-form-1702rt',
            component: BirForm1702RtView,
            meta: { breadcrumb: ['Tax Compliance', 'BIR Form 1702-RT'] }
        }
    ],
    menu: [
        {
            label: 'Tax Compliance & Returns',
            items: [
                {
                    label: 'Tax Regime Optimizer',
                    icon: 'pi pi-fw pi-compass',
                    to: '/tax/optimize'
                },
                {
                    label: 'BIR Form 1701A (Sole Prop)',
                    icon: 'pi pi-fw pi-file-pdf',
                    to: '/tax/form-1701a'
                },
                {
                    label: 'BIR Form 1702-RT (Corp)',
                    icon: 'pi pi-fw pi-building',
                    to: '/tax/form-1702rt'
                }
            ]
        }
    ]
};
