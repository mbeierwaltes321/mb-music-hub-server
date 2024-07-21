<!--TODO: Rename this component to Layout-->
<script setup lang="ts">
import { VApp, VAppBar, VMain, VFooter } from 'vuetify/components';
import AppFooter from './AppFooter.vue';
import AppHeader from './AppHeader.vue';

</script>

<template>
    <v-app>
        <AppHeader />
        <v-main>
            <!--TODO: Right here place the "Home component. This is where the main page should be displayed"-->
            <div class="h-100 w-100" style="background-color: red ">

            </div>
        </v-main>
        <v-footer :absolute="true" 
                  :app="true">
            <AppFooter/>
        </v-footer>
    </v-app>
</template>

<style scoped>

.v-footer {
    padding: 0%;
}

</style>