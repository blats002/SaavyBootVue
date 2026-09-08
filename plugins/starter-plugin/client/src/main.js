import { createSaavyApp } from '@core/app';
import router from './router';
import starterPlugin from './index';

createSaavyApp({
    router,
    plugins: [starterPlugin]
});
