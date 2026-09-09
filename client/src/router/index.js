import { createRouter, createWebHistory } from 'vue-router';
import AppLayout from '../layout/AppLayout.vue';
import AuthService from '../service/AuthService';
import { getPluginRoutes, getPluginDashboard } from '../plugins/pluginLoader';
import { pluginState } from '../plugins/pluginState';

export function createSaavyRouter(config = {}) {
    const allPluginRoutes = [...getPluginRoutes(), ...(config.routes || [])];
    const standardRoutes = allPluginRoutes.filter(r => !r.meta?.standalone);
    const standaloneRoutes = allPluginRoutes.filter(r => r.meta?.standalone);
    const dashboardComponent = config.dashboard || getPluginDashboard() || (() => import('../views/Dashboard.vue'));

    const baseChildren = [
        {
            path: '/',
            name: 'dashboard',
            component: dashboardComponent
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
        {
            path: '/pages/plugins',
            name: 'plugins',
            component: () => import('../views/pages/PluginManagement.vue')
        },
        ...standardRoutes
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
            },
            ...standaloneRoutes
        ]
    });

    // Navigation guard for authenticated routes
    router.beforeEach(async (to, from, next) => {
        const publicPages = ['/auth/login', '/landing', '/pages/notfound', '/auth/access', '/auth/error', '/bundy-clock'];
        const isPublic =
            publicPages.some((p) => to.path === p || to.path.startsWith(p + '/')) ||
            to.meta?.public === true ||
            to.meta?.standalone === true ||
            to.matched.some((r) => r.meta?.public === true || r.meta?.standalone === true);
        const authRequired = !isPublic;
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

        // Ensure active plugins status is loaded if logged in
        if (loggedIn && !pluginState.isLoaded) {
            await pluginState.fetchActivePlugins();
        }

        // Check if route belongs to a disabled plugin
        if (to.meta && to.meta.pluginName && loggedIn) {
            if (!pluginState.isPluginEnabled(to.meta.pluginName)) {
                return next('/pages/notfound');
            }
        }

        // Role-based access control for Administration routes
        const adminRoutes = ['/pages/users', '/pages/roles', '/pages/plugins'];
        if (adminRoutes.includes(to.path) && !AuthService.hasRole('ROLE_ADMIN')) {
            return next('/auth/access');
        }

        next();
    });

    return router;
}

const router = createSaavyRouter();
export default router;
