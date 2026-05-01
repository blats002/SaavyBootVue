<script setup>
import Button from 'primevue/button';
import Toolbar from 'primevue/toolbar';
import Panel from 'primevue/panel';
import Accordion from 'primevue/accordion';
import TabView from 'primevue/tabview';

/*
     * GenericPanel bodyType usage:
     *
     * 1. Default div body:
     *
     *    <GenericPanel>
     *        <p>Regular content</p>
     *    </GenericPanel>
     *
     *    or:
     *
     *    <GenericPanel bodyType="div">
     *        <p>Regular content</p>
     *    </GenericPanel>
     *
     *
     * 2. PrimeVue Panel body:
     *
     *    <GenericPanel title="Details" bodyType="panel">
     *        <p>Content inside a PrimeVue Panel</p>
     *    </GenericPanel>
     *
     *
     * 3. PrimeVue Accordion body:
     *
     *    <GenericPanel bodyType="accordion">
     *        <AccordionTab header="General">
     *            <p>General content</p>
     *        </AccordionTab>
     *
     *        <AccordionTab header="Advanced">
     *            <p>Advanced content</p>
     *        </AccordionTab>
     *    </GenericPanel>
     *
     *
     * 4. PrimeVue TabView body:
     *
     *    <GenericPanel bodyType="tabview">
     *        <TabPanel header="Profile">
     *            <p>Profile content</p>
     *        </TabPanel>
     *
     *        <TabPanel header="Settings">
     *            <p>Settings content</p>
     *        </TabPanel>
     *    </GenericPanel>
     */

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },
  leftToolBarButtons: {
    type: Array,
    default: () => []
  },
  rightToolBarButtons: {
    type: Array,
    default: () => []
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  showToolbar: {
    type: Boolean,
    default: true
  },
  cardClass: {
    type: String,
    default: 'card'
  },
  bodyType: {
    type: String,
    default: 'div',
    validator: (value) => ['div', 'panel', 'accordion', 'tabview'].includes(value)
  }
});

const emit = defineEmits(['button-click']);

const handleButtonClick = (button, section) => {
  emit('button-click', {
    ...button,
    section
  });
};
</script>

<template>
  <div :class="cardClass">
    <div v-if="showHeader && (title || subtitle)" class="generic-panel-header">
      <h5 v-if="title" class="generic-panel-title">
        {{ title }}
      </h5>

      <small v-if="subtitle" class="generic-panel-subtitle">
        {{ subtitle }}
      </small>
    </div>

    <Toolbar v-if="showToolbar" class="generic-panel-toolbar">
      <template #start>
        <div class="flex align-items-center gap-2 flex-wrap">
          <Button
              v-for="button in leftToolBarButtons"
              :key="button.key"
              :label="button.label"
              :icon="button.icon"
              :class="button.class"
              :severity="button.severity"
              :disabled="button.disabled"
              :outlined="button.outlined"
              :text="button.text"
              :rounded="button.rounded"
              @click="handleButtonClick(button, 'left')"
          />
        </div>
      </template>

      <template #end>
        <div class="flex align-items-center gap-2 flex-wrap">
          <Button
              v-for="button in rightToolBarButtons"
              :key="button.key"
              :label="button.label"
              :icon="button.icon"
              :class="button.class"
              :severity="button.severity"
              :disabled="button.disabled"
              :outlined="button.outlined"
              :text="button.text"
              :rounded="button.rounded"
              @click="handleButtonClick(button, 'right')"
          />
        </div>
      </template>
    </Toolbar>

    <div v-if="bodyType === 'div'" class="generic-panel-body">
      <slot/>
    </div>

    <Panel v-else-if="bodyType === 'panel'" :header="title" class="generic-panel-body">
      <slot/>
    </Panel>

    <Accordion v-else-if="bodyType === 'accordion'" class="generic-panel-body">
      <slot/>
    </Accordion>

    <TabView v-else-if="bodyType === 'tabview'" class="generic-panel-body">
      <slot/>
    </TabView>
  </div>
</template>

<style scoped lang="scss">
.generic-panel-header {
  margin-bottom: 1rem;
}

.generic-panel-title {
  margin: 0;
}

.generic-panel-subtitle {
  display: inline-block;
  margin-top: 0.25rem;
  color: var(--text-color-secondary);
}

.generic-panel-toolbar {
  margin-bottom: 1rem;
}

.generic-panel-body {
  width: 100%;
}
</style>