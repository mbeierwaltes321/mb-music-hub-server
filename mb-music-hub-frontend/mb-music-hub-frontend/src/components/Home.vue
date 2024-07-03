<script setup lang="ts">
import { VCard, VContainer, VCol, VRow } from 'vuetify/components';
import AppFooter from './AppFooter.vue';
import AppHeader from './AppHeader.vue';

</script>

<template>
    <v-container id="HomeContainer" :fluid="true" class="p-0">
        <v-row no-gutters class="h-100">
            <v-col cols="12" class="position-fixed">
                <AppHeader />                
            </v-col>
            <v-col cols="12">
                <div style="height: 400px; width: 2000px; ">

                </div>
            </v-col>
            <v-col cols="12" align-self="end">
                <AppFooter />
            </v-col>
        </v-row>
    </v-container>
</template>

<style scoped>

#HomeContainer {
  height: 100vh;
}

</style>