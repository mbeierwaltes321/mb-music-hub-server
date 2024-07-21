<script setup lang="ts">
import { VAppBar, VMenu, VAppBarTitle, VBtn } from "vuetify/components";
import { type Ref, ref } from "vue";

const theme : Ref<string | null> = ref(localStorage.getItem("colorTheme"));

//TODO - Eventually frame this so that it works with Vue router.
const mediaItems = [
    { title: "Playlists", value: 0 },
    { title: "Podcasts", value: 1 },
    { title: "YouTube Mixes", value: 2 }
];

</script>

<template>
    <!--TODO: Solve sizing issue in mobile. Title gets minimized-->
    <v-app-bar>
        <v-app-bar-title>MB's Music Hub</v-app-bar-title>
        <v-menu>
            <template #activator="{props}">
                <v-btn v-bind="props"
                       variant="text"
                       class="text-none"
                       append-icon="fas fa-caret-down"
                       size="x-large">Media</v-btn>
            </template>
            <v-list :items="mediaItems" />
        </v-menu>
        <v-btn class="text-none"
               variant="outlined"
               rounded="xl"
               append-icon="fab fa-spotify"
               size="x-large">
               Login
        </v-btn>
    </v-app-bar>
</template>