<script setup>
import { computed, ref, watch } from 'vue';
import Chart from 'primevue/chart';
import { useLayout } from '../../layout/composables/layout';
import AuthService from '../../service/AuthService';
import { pluginState } from '../../plugins/pluginState';

const props = defineProps({
    title: {
        type: String,
        default: ''
    },
    type: {
        type: String,
        default: 'line'
    },
    data: {
        type: Object,
        required: true
    },
    options: {
        type: Object,
        default: null
    },
    plugin: {
        type: String,
        default: null
    },
    role: {
        type: [String, Array],
        default: null
    }
});

const { isDarkTheme } = useLayout();
const computedOptions = ref({});

const isRoleAuthorized = computed(() => {
    if (props.plugin && !pluginState.isPluginEnabled(props.plugin)) return false;
    if (!props.role) return true;
    return AuthService.hasRole(props.role);
});

const generateThemeOptions = (isDark) => {
    if (props.options) {
        return props.options;
    }

    const textColor = isDark ? '#ebedef' : '#495057';
    const textColorSecondary = isDark ? 'rgba(255, 255, 255, 0.6)' : '#6c757d';
    const surfaceBorder = isDark ? 'rgba(160, 167, 181, .3)' : '#ebedef';

    // Circular charts (pie, doughnut, polarArea) do not use x/y axes
    const isCartesian = ['line', 'bar'].includes(props.type);

    const baseConfig = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                labels: {
                    color: textColor
                }
            }
        }
    };

    if (isCartesian) {
        baseConfig.scales = {
            x: {
                ticks: {
                    color: textColorSecondary
                },
                grid: {
                    color: surfaceBorder
                }
            },
            y: {
                ticks: {
                    color: textColorSecondary
                },
                grid: {
                    color: surfaceBorder
                }
            }
        };
    }

    return baseConfig;
};

watch(
    isDarkTheme,
    (val) => {
        computedOptions.value = generateThemeOptions(val);
    },
    { immediate: true }
);
</script>

<template>
    <div v-if="isRoleAuthorized" class="card mb-4 h-full">
        <div class="flex justify-content-between align-items-center mb-4">
            <h5 v-if="title" class="m-0">{{ title }}</h5>
            <slot name="header-action"></slot>
        </div>
        <div style="position: relative; min-height: 280px; height: 320px;">
            <Chart :type="type" :data="data" :options="computedOptions" class="h-full w-full" />
        </div>
    </div>
</template>

<style scoped></style>
