import BundyClockKiosk from './views/pages/BundyClockKiosk.vue';
import AttendanceManagement from './views/pages/AttendanceManagement.vue';
import BadgeGenerator from './views/pages/BadgeGenerator.vue';

export default {
    name: 'attendance-plugin',
    routes: [
        {
            path: '/bundy-clock',
            name: 'bundy-clock-kiosk',
            component: BundyClockKiosk,
            meta: {
                standalone: true,
                public: true,
                breadcrumb: ['Time & Attendance', 'Bundy Clock Kiosk']
            }
        },
        {
            path: '/employee-badges',
            name: 'badge-generator',
            component: BadgeGenerator,
            meta: { breadcrumb: ['Time & Attendance', 'Employee Badges'] }
        },
        {
            path: '/attendance-records',
            name: 'attendance-management',
            component: AttendanceManagement,
            meta: { breadcrumb: ['Time & Attendance', 'Timesheet Records'] }
        }
    ],
    menu: [
        {
            label: 'Time & Attendance',
            items: [
                {
                    label: 'Bundy Clock Kiosk',
                    icon: 'pi pi-fw pi-qrcode',
                    to: '/bundy-clock'
                },
                {
                    label: 'Employee Badges & QR',
                    icon: 'pi pi-fw pi-id-card',
                    to: '/employee-badges'
                },
                {
                    label: 'Timesheet Records',
                    icon: 'pi pi-fw pi-calendar',
                    to: '/attendance-records'
                }
            ]
        }
    ]
};
