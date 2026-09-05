import axios from 'axios';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';
const TOKEN_KEY = 'jwt_auth_token';
const USER_KEY = 'jwt_auth_user';

export const AuthService = {
    async login(username, password) {
        const response = await axios.post(`${SERVER_URL}/api/auth/login`, {
            username,
            password
        });

        if (response.data && response.data.token) {
            localStorage.setItem(TOKEN_KEY, response.data.token);
            localStorage.setItem(USER_KEY, JSON.stringify(response.data));
        }

        return response.data;
    },

    logout() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    },

    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },

    getCurrentUser() {
        const userStr = localStorage.getItem(USER_KEY);
        if (!userStr) return null;
        try {
            return JSON.parse(userStr);
        } catch {
            return null;
        }
    },

    isAuthenticated() {
        const token = this.getToken();
        return !!token;
    },

    hasRole(roles) {
        const user = this.getCurrentUser();
        if (!user || !user.roles || !roles) return false;

        let requiredRoles = [];
        if (Array.isArray(roles)) {
            requiredRoles = roles;
        } else if (typeof roles === 'string') {
            requiredRoles = roles.split(',').map((r) => r.trim());
        }

        return requiredRoles.some((role) => user.roles.includes(role));
    },

    hasAllRoles(roles) {
        const user = this.getCurrentUser();
        if (!user || !user.roles || !roles) return false;

        let requiredRoles = [];
        if (Array.isArray(roles)) {
            requiredRoles = roles;
        } else if (typeof roles === 'string') {
            requiredRoles = roles.split(',').map((r) => r.trim());
        }

        return requiredRoles.every((role) => user.roles.includes(role));
    },

    async fetchCurrentUser() {
        const token = this.getToken();
        if (!token) return null;

        try {
            const response = await axios.get(`${SERVER_URL}/api/auth/me`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            if (response.data) {
                const currentUser = this.getCurrentUser() || {};
                const updated = { ...currentUser, ...response.data };
                localStorage.setItem(USER_KEY, JSON.stringify(updated));
                return updated;
            }
        } catch (error) {
            if (error.response && error.response.status === 401) {
                this.logout();
            }
        }
        return null;
    }
};

export default AuthService;
