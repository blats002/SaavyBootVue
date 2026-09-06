import { createRouter, createWebHistory } from 'vue-router';
import AppLayout from '../layout/AppLayout.vue';
import AuthService from '../service/AuthService';

export function createSaavyRouter(config = {}) {
    const customRoutes = config.routes || [];
    const dashboardComponent = config.dashboard || (() => import('../views/Dashboard.vue'));

    const baseChildren = [
        {
            path: '/',
            name: 'dashboard',
            component: dashboardComponent
        },
        {
            path: '/pages/suppliers',
            name: 'supplier',
            component: () => import('../views/pages/SupplierManagement.vue')
        },
        {
            path: '/pages/products',
            name: 'product',
            component: () => import('../views/pages/ProductManagement.vue')
        },
        {
            path: '/pages/users',
            name: 'users',
            component: () => import('../views/pages/UserManagement.vue')
        },
        {
            path: '/pages/roles',
            name: 'roles',
            component: () => import('../views/pages/RoleManagement.vue')
        },
        ...customRoutes
    ];

    const router = createRouter({
        history: createWebHistory(),
        routes: [
            {
                path: '/',
                component: AppLayout,
                children: baseChildren
            },
            {
                path: '/landing',
                name: 'landing',
                component: () => import('../views/pages/Landing.vue')
            },
            {
                path: '/pages/notfound',
                name: 'notfound',
                component: () => import('../views/pages/NotFound.vue')
            },
            {
                path: '/auth/login',
                name: 'login',
                component: () => import('../views/pages/auth/Login.vue')
            },
            {
                path: '/auth/access',
                name: 'accessDenied',
                component: () => import('../views/pages/auth/Access.vue')
            },
            {
                path: '/auth/error',
                name: 'error',
                component: () => import('../views/pages/auth/Error.vue')
            }
        ]
    });

    // Navigation guard for authenticated routes
    router.beforeEach((to, from, next) => {
        const publicPages = ['/auth/login', '/landing', '/pages/notfound', '/auth/access', '/auth/error'];
        const authRequired = !publicPages.includes(to.path);
        const loggedIn = AuthService.isAuthenticated();

        if (authRequired && !loggedIn) {
            return next({
                path: '/auth/login',
                query: { redirect: to.fullPath }
            });
        }

        if (to.path === '/auth/login' && loggedIn) {
            return next('/');
        }

        // Role-based access control for Administration routes
        const adminRoutes = ['/pages/users', '/pages/roles'];
        if (adminRoutes.includes(to.path) && !AuthService.hasRole('ROLE_ADMIN')) {
            return next('/auth/access');
        }

        next();
    });

    return router;
}

const router = createSaavyRouter();
export default router;
