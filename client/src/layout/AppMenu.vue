<script setup>
import { computed } from 'vue';
import AppMenuItem from './AppMenuItem.vue';
import { menuState } from './menuConfig';

const defaultHomeMenu = {
    label: 'Home',
    items: [
        {
            label: 'Suppliers',
            icon: 'pi pi-fw pi-building',
            to: '/pages/suppliers'
        },
        {
            label: 'Products',
            icon: 'pi pi-fw pi-box',
            to: '/pages/products'
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
    if (menuState.customMenu && menuState.customMenu.length > 0) {
        return [
            ...menuState.customMenu,
            adminMenu
        ];
    }
    return [
        defaultHomeMenu,
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
