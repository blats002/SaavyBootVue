import { reactive } from 'vue';

export const menuState = reactive({
    customMenu: null,
    replaceHome: true
});

export function setAppMenu(menu, replaceHome = true) {
    menuState.customMenu = menu;
    menuState.replaceHome = replaceHome;
}
