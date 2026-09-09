import CustomerInvoiceManagement from './views/pages/CustomerInvoiceManagement.vue';
import VendorInvoiceManagement from './views/pages/VendorInvoiceManagement.vue';
import PartyManagement from './views/pages/PartyManagement.vue';

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
        }
    ],
    menu: [
        {
            label: 'Invoice Tracking',
            items: [
                {
                    label: 'Parties / Accounts',
                    icon: 'pi pi-fw pi-building',
                    to: '/pages/parties'
                },
                {
                    label: 'Customer Invoices',
                    icon: 'pi pi-fw pi-money-bill',
                    to: '/pages/customer-invoices'
                },
                {
                    label: 'Vendor Invoices',
                    icon: 'pi pi-fw pi-receipt',
                    to: '/pages/vendor-invoices'
                }
            ]
        }
    ]
};
