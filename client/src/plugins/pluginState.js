import { reactive } from 'vue';
import axios from 'axios';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

export const pluginState = reactive({
    activePlugins: {},
    isLoaded: false,

    async fetchActivePlugins() {
        try {
            const token = localStorage.getItem('token');
            const headers = token ? { Authorization: `Bearer ${token}` } : {};
            const response = await axios.get(`${SERVER_URL}/api/plugins/active`, { headers });
            if (response.data && typeof response.data === 'object') {
                this.activePlugins = response.data;
            }
            this.isLoaded = true;
        } catch (e) {
            // If offline or not authenticated yet, fallback to all enabled
            console.warn('Could not fetch active plugin status, defaulting to all active:', e.message);
            this.isLoaded = true;
        }
    },

    isPluginEnabled(name) {
        if (!name) return true;
        // If not explicitly set to false, default to true
        return this.activePlugins[name] !== false;
    },

    setPluginStatus(name, enabled) {
        this.activePlugins[name] = enabled;
    }
});
