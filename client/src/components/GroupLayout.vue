<script setup>
import { computed, useSlots } from 'vue';

/*
 * GroupLayout
 *
 * Use this component when you want to place multiple components side by side
 * using PrimeFlex grid classes.
 *
 * It automatically wraps each child component in a responsive column.
 *
 * Props:
 *
 * columns:
 *   Controls how many columns appear on medium screens and larger.
 *   Supported values: 1, 2, 3, 4, 6, 12
 *
 * gapClass:
 *   Optional extra PrimeFlex spacing class.
 *
 *
 * Example 1: Two GenericPanels side by side
 *
 * <GroupLayout :columns="2">
 *     <GenericPanel title="Left Panel">
 *         Left content
 *     </GenericPanel>
 *
 *     <GenericPanel title="Right Panel">
 *         Right content
 *     </GenericPanel>
 * </GroupLayout>
 *
 *
 * Example 2: Three GenericPanels side by side
 *
 * <GroupLayout :columns="3">
 *     <GenericPanel title="Profile">
 *         Profile content
 *     </GenericPanel>
 *
 *     <GenericPanel title="Settings">
 *         Settings content
 *     </GenericPanel>
 *
 *     <GenericPanel title="History">
 *         History content
 *     </GenericPanel>
 * </GroupLayout>
 *
 *
 * Example 3: GroupLayout inside a GenericPanel body
 *
 * <GenericPanel title="User Details">
 *     <GroupLayout :columns="2">
 *         <GenericPanel title="Profile">
 *             Profile content
 *         </GenericPanel>
 *
 *         <GenericPanel title="Settings">
 *             Settings content
 *         </GenericPanel>
 *     </GroupLayout>
 * </GenericPanel>
 *
 *
 * Example 4: One full-width column
 *
 * <GroupLayout :columns="1">
 *     <GenericPanel title="Full Width Panel">
 *         Full width content
 *     </GenericPanel>
 * </GroupLayout>
 *
 *
 * Example 5: GenericPanel with tabs beside another GenericPanel
 *
 * <GroupLayout :columns="2">
 *     <GenericPanel
 *         title="User Tabs"
 *         bodyType="tabview"
 *         :showToolbar="false"
 *     >
 *         <TabPanel header="Profile">
 *             <p>Profile content</p>
 *         </TabPanel>
 *
 *         <TabPanel header="Settings">
 *             <p>Settings content</p>
 *         </TabPanel>
 *     </GenericPanel>
 *
 *     <GenericPanel title="Summary">
 *         Summary content
 *     </GenericPanel>
 * </GroupLayout>
 */

const props = defineProps({
  columns: {
    type: Number,
    default: 2,
    validator: (value) => [1, 2, 3, 4, 6, 12].includes(value)
  },
  gapClass: {
    type: String,
    default: ''
  }
});

const slots = useSlots();

const defaultSlotItems = computed(() => slots.default?.() || []);

const getColumnClass = (columns) => {
  const columnSize = 12 / columns;

  return `col-12 md:col-${columnSize}`;
};
</script>

<template>
  <div :class="['grid', gapClass]">
    <div
        v-for="(slotItem, index) in defaultSlotItems"
        :key="index"
        :class="getColumnClass(props.columns)"
    >
      <component :is="slotItem" />
    </div>
  </div>
</template>