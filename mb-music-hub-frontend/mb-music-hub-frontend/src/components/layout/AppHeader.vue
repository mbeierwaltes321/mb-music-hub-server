<script setup lang="ts">
import { VAppBar, VMenu, VAppBarTitle, VBtn, VAppBarNavIcon, VList, VListItem } from "vuetify/components";
import { computed, type Ref, ref, watch } from "vue";
import { useDisplay, useTheme } from "vuetify";
import { setThemeInLocalStorage } from "@/utilities/localStorage";

const theme = useTheme();

//Determine if the current screen is in the medium breakpoint or higher (960px or greater)
const { mdAndUp } = useDisplay();
const drawer: Ref<boolean | null> = ref<boolean | null>(null);

//TODO - Eventually frame this so that it works with Vue router.
const mediaItems = computed(() => [
    { title: "Playlists", value: 0, click: navigateToPlaylists },
    { title: "Podcasts", value: 1, click: navigateToPodcasts },
    { title: "YouTube Mixes", value: 2, click: navigateToYoutubeMixes }
]);


/**
 * This function changes the theme in the application from light mode to dark mode
 */
function toggleTheme() {
    const newTheme: string = theme.global.current.value.dark ? 'light' : 'dark';
    theme.global.name.value = newTheme;
    setThemeInLocalStorage(newTheme);
}

/**
 * This function navigates the user to the playlists component
 */
 function navigateToPlaylists() {
    alert("You clicked on the playlists button!");
}

/**
 * This function navigates the user to the podcasts component
 */
 function navigateToPodcasts() {
    alert("You clicked on the podcasts button!");
}

/**
 * This function navigates the user to the youtube mixes component
 */
 function navigateToYoutubeMixes() {
    alert("You clicked on the youtube mixes button!");
}


//Add watch that closes the drawer if the screen changes
watch(() => mdAndUp.value, () => {
    drawer.value = false;
})

</script>

<template>
    <v-app-bar>
        <v-app-bar-nav-icon v-if="!mdAndUp" 
                            @click.stop="drawer = !drawer" />
        <v-app-bar-title>MB's Music Hub</v-app-bar-title>
        <v-menu v-if="mdAndUp">
            <template #activator="{ props }">
                <v-btn v-bind="props" 
                       variant="text" 
                       class="text-none" 
                       append-icon="fas fa-caret-down"
                       size="large">Media</v-btn>
            </template>
            <v-list>
                <v-list-item v-for="item in mediaItems"
                             :key="item.value"
                             :title="item.title"
                             @click="item.click"
                             />
            </v-list>
        </v-menu>
        <v-btn v-if="mdAndUp" 
               :icon="theme.current.value.dark ? 'fas fa-moon' : 'far fa-sun'" 
               @click="toggleTheme"/>
        <v-btn class="text-none"
               variant="outlined" 
               rounded="xl" 
               append-icon="fab fa-spotify" 
               size="large">
            Login
        </v-btn>
    </v-app-bar>
    <v-navigation-drawer v-model="drawer" temporary>
        <v-list>
            <v-list-item v-for="item in mediaItems"
                         :key="item.value"
                         :title="item.title"
                         @click="item.click"/>
            <v-list-item :key="mediaItems.length"
                         :prepend-icon="theme.current.value.dark ? 'fas fa-moon' : 'far fa-sun'"
                         :ripple="false"
                         @click="toggleTheme" />
        </v-list>
    </v-navigation-drawer>
</template>