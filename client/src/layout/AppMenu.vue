<script setup>
import { computed } from 'vue';
import AppMenuItem from './AppMenuItem.vue';
import { menuState } from './menuConfig';
import { getPluginMenus } from '../plugins/pluginLoader';

const defaultHomeMenu = {
    label: 'Home',
    items: [
        {
          label: 'Dashboard',
          icon: 'pi pi-fw pi-home',
          to: '/'
        }
    ]
};

const adminMenu = {
    label: 'Administration',
    role: 'ROLE_ADMIN',
    items: [
        {
            label: 'Users',
            role: 'ROLE_ADMIN',
            icon: 'pi pi-fw pi-users',
            to: '/pages/users'
        },
        {
            label: 'Roles',
            role: 'ROLE_ADMIN',
            icon: 'pi pi-fw pi-shield',
            to: '/pages/roles'
        }
    ]
};

const model = computed(() => {
    const pluginMenus = getPluginMenus();
    if (menuState.customMenu && menuState.customMenu.length > 0) {
        return [
            ...menuState.customMenu,
            ...pluginMenus,
            adminMenu
        ];
    }
    return [
        defaultHomeMenu,
        ...pluginMenus,
        adminMenu
    ];
});
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item.label || i">
            <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
            <li v-if="item.separator" class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
