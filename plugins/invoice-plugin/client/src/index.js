import CustomerInvoiceManagement from './views/pages/CustomerInvoiceManagement.vue';
import VendorInvoiceManagement from './views/pages/VendorInvoiceManagement.vue';
import PartyManagement from './views/pages/PartyManagement.vue';
import TaxCategoryManagement from './views/pages/TaxCategoryManagement.vue';
import VendorRuleManagement from './views/pages/VendorRuleManagement.vue';

export default {
    name: 'invoice-plugin',
    routes: [
        {
            path: '/pages/parties',
            name: 'party-management',
            component: PartyManagement,
            meta: { breadcrumb: ['Pages', 'Party Management'] }
        },
        {
            path: '/pages/customer-invoices',
            name: 'customer-invoice-management',
            component: CustomerInvoiceManagement,
            meta: { breadcrumb: ['Pages', 'Customer Invoice Management'] }
        },
        {
            path: '/pages/vendor-invoices',
            name: 'vendor-invoice-management',
            component: VendorInvoiceManagement,
            meta: { breadcrumb: ['Pages', 'Vendor Invoice Management'] }
        },
        {
            path: '/pages/tax-categories',
            name: 'tax-category-management',
            component: TaxCategoryManagement,
            meta: { breadcrumb: ['Tax & Invoices', 'Tax Buckets & Categories'] }
        },
        {
            path: '/pages/vendor-rules',
            name: 'vendor-rule-management',
            component: VendorRuleManagement,
            meta: { breadcrumb: ['Tax & Invoices', 'Vendor Routing Rules'] }
        }
    ],
    menu: [
        {
            label: 'Invoice & Tax Routing',
            items: [
                {
                    label: 'Parties / Accounts',
                    icon: 'pi pi-fw pi-building',
                    to: '/pages/parties'
                },
                {
                    label: 'Sales Invoices (Revenue)',
                    icon: 'pi pi-fw pi-money-bill',
                    to: '/pages/customer-invoices'
                },
                {
                    label: 'Vendor Bills & Receipts',
                    icon: 'pi pi-fw pi-receipt',
                    to: '/pages/vendor-invoices'
                },
                {
                    label: 'Tax Buckets & Categories',
                    icon: 'pi pi-fw pi-tags',
                    to: '/pages/tax-categories'
                },
                {
                    label: 'Vendor Routing Rules',
                    icon: 'pi pi-fw pi-sliders-h',
                    to: '/pages/vendor-rules'
                }
            ]
        }
    ]
};
