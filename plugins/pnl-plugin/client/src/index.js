import PnlReportView from './views/PnlReportView.vue';
import PnlLedgerManagement from './views/PnlLedgerManagement.vue';
import PnlAccountManagement from './views/PnlAccountManagement.vue';

export default {
    name: 'pnl-plugin',
    routes: [
        {
            path: '/pages/pnl-report',
            name: 'pnl-report',
            component: PnlReportView,
            meta: {
                breadcrumb: ['Financials', 'P&L Statement'],
                role: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER']
            }
        },
        {
            path: '/pages/pnl-ledger',
            name: 'pnl-ledger-management',
            component: PnlLedgerManagement,
            meta: {
                breadcrumb: ['Financials', 'Ledger Entries'],
                role: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER']
            }
        },
        {
            path: '/pages/pnl-accounts',
            name: 'pnl-account-management',
            component: PnlAccountManagement,
            meta: {
                breadcrumb: ['Financials', 'Chart of Accounts'],
                role: ['ROLE_ADMIN', 'ROLE_MANAGER']
            }
        }
    ],
    menu: [
        {
            label: 'Financials & P&L',
            role: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER'],
            items: [
                {
                    label: 'P&L Statement',
                    icon: 'pi pi-fw pi-chart-bar',
                    to: '/pages/pnl-report',
                    role: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER']
                },
                {
                    label: 'Ledger Entries',
                    icon: 'pi pi-fw pi-book',
                    to: '/pages/pnl-ledger',
                    role: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER']
                },
                {
                    label: 'Chart of Accounts',
                    icon: 'pi pi-fw pi-list',
                    to: '/pages/pnl-accounts',
                    role: ['ROLE_ADMIN', 'ROLE_MANAGER']
                }
            ]
        }
    ]
};
