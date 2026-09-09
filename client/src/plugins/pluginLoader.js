/**
 * Dynamic Plugin Loader for SaavyBootVue
 * Automatically discovers, imports, and registers all plugins under plugins/[plugin-name]/client/src/index.js
 */
import { pluginState } from './pluginState';

// Vite glob import scanning all plugin manifests relative to this file
const pluginModules = import.meta.glob('/../plugins/*/client/src/index.js', { eager: true });
const fallbackPluginModules = import.meta.glob('../../plugins/*/client/src/index.js', { eager: true });

export function loadDiscoveredPlugins() {
    const plugins = [];
    const merged = { ...fallbackPluginModules, ...pluginModules };

    for (const path in merged) {
        const module = merged[path];
        const plugin = module.default || module;
        if (plugin) {
            plugins.push({
                sourcePath: path,
                name: plugin.name || path,
                routes: (plugin.routes || []).map((r) => ({
                    ...r,
                    meta: { ...(r.meta || {}), pluginName: plugin.name || path }
                })),
                menu: plugin.menu || [],
                dashboard: plugin.dashboard || null
            });
        }
    }
    return plugins;
}

export function getPluginRoutes() {
    const plugins = loadDiscoveredPlugins();
    return plugins.flatMap((p) => p.routes);
}

export function getPluginMenus() {
    const plugins = loadDiscoveredPlugins();
    return plugins
        .filter((p) => pluginState.isPluginEnabled(p.name))
        .flatMap((p) => p.menu);
}

export function getPluginDashboard() {
    const plugins = loadDiscoveredPlugins();
    const pluginWithDashboard = plugins
        .slice()
        .reverse()
        .find((p) => p.dashboard && pluginState.isPluginEnabled(p.name));
    return pluginWithDashboard ? pluginWithDashboard.dashboard : null;
}
