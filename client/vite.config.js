import { fileURLToPath, URL } from 'node:url';
import fs from 'node:fs';
import path from 'node:path';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default defineConfig(() => {
    // Check if downstream project provided an appConfig path via env or convention
    const envAppConfig = process.env.APP_CONFIG_PATH;
    const conventionalAppConfig = path.resolve(__dirname, '../../client/src/config/appConfig.js');
    const defaultAppConfig = path.resolve(__dirname, './src/config/defaultAppConfig.js');

    let appConfigPath = defaultAppConfig;
    if (envAppConfig && fs.existsSync(envAppConfig)) {
        appConfigPath = path.resolve(envAppConfig);
    } else if (fs.existsSync(conventionalAppConfig)) {
        appConfigPath = conventionalAppConfig;
    }

    console.log(`[SaavyBootVue] Using app config: ${appConfigPath}`);

    return {
        plugins: [vue()],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url)),
                '@core': fileURLToPath(new URL('./src', import.meta.url)),
                '@plugins': path.resolve(__dirname, '../plugins'),
                '@app-config': appConfigPath,
                'vue': fileURLToPath(new URL('./node_modules/vue', import.meta.url)),
                'axios': fileURLToPath(new URL('./node_modules/axios', import.meta.url)),
                'primevue': fileURLToPath(new URL('./node_modules/primevue', import.meta.url))
            }
        },
        server: {
            fs: {
                allow: ['..', '../plugins', '../../plugins', '../../..']
            }
        },
        build: {
            target: 'esnext'
        }

    };
});
