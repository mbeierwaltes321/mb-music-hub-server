<script setup lang="ts">
import { VApp, VAppBar, VMain, VFooter } from 'vuetify/components';
import AppFooter from './AppFooter.vue';
import AppHeader from './AppHeader.vue';
import Home from '../Home.vue';
import { RouterView } from 'vue-router';

</script>

<template>
    <v-app>
        <AppHeader />
        <v-main>
            <RouterView></RouterView>
        </v-main>
        <AppFooter/>    
    </v-app>
</template>

<style scoped>

.v-footer {
    padding: 0%;
}

</style>