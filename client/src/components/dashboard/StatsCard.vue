<!-- components/StatsCard.vue -->
<script setup>
import { computed } from 'vue';
import Card from "primevue/card";
import AuthService from '../../service/AuthService';
import { pluginState } from '../../plugins/pluginState';

const props = defineProps({
  title: String,
  icon : String,
  content: [String, Number],
  footer: Object,
  plugin: {
    type: String,
    default: null
  },
  role: {
    type: [String, Array],
    default: null
  }
});

const isRoleAuthorized = computed(() => {
  if (props.plugin && !pluginState.isPluginEnabled(props.plugin)) return false;
  if (!props.role) return true;
  return AuthService.hasRole(props.role);
});

const getSeverityClass = (severity) => {
  switch (severity) {
    case 'success':
      return 'text-green-500'
    case 'warning':
      return 'text-yellow-500'
    case 'danger':
      return 'text-red-500'
    default:
      return ''
  }
}

</script>

<template>
  <div v-if="isRoleAuthorized" class="card mb-0">
    <div class="flex justify-content-between mb-3">
      <div>
        <span class="block text-500 font-medium mb-3">{{ title }}</span>
        <div class="text-900 font-medium text-xl">{{ content }}</div>
      </div>
      <div class="flex align-items-center justify-content-center border-round" style="width: 2.5rem; height: 2.5rem">
        <i :class="icon"></i>
      </div>
    </div>
    <div v-if="footer">
      <span :class="[getSeverityClass(footer.severity), 'font-medium']">
      {{ footer.value+" "}}
    </span>
      <span class="text-500">
      {{ footer.content }}
    </span>
    </div>
  </div>
</template>

<style scoped>
</style>