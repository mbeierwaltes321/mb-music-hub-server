<script setup lang="ts">
import { ref, watch } from 'vue';
import { useTheme } from 'vuetify';
import { VContainer, VCol, VRow } from 'vuetify/components';

//Set the horizontal line depending on the color theme
const theme = useTheme();
const horizontalLineColor = ref(theme.global.current.value.dark ? "#8B8B00" : "#00008B");

watch(() => theme.global.current.value.dark, () => {
    horizontalLineColor.value = theme.global.current.value.dark ? "#8B8B00" : "#00008B";
})

</script>

<template>
    <v-container id="HomeContainer"
                 :fluid="true"
                 class="p-0 fill-height">
        <v-row class="fill-height"
               no-gutters>
            <v-col id="HomeHeaderSection"
                   :cols="12"
                   class="d-flex flex-column align-items-center justify-content-end">
                <h1>Select Your Experience</h1>
                <hr>
            </v-col>
            <v-col id="HomeMusicSection"
                   :cols="12">
            </v-col>
            <v-col :cols="12">              
                <div id="HomePodcastsSection"
                     class="bg-primary">

                </div>
            </v-col>
        </v-row>
    </v-container>
</template>

<style scoped>

#HomeHeaderSection {
    height: 150px
}

#HomeMusicSection {
    height: 400px;
}

#HomePodcastsSection {
    height: 400px
}

hr {
    width: 100%;
    height: 3px;
    border: none;
    background-color: v-bind(horizontalLineColor);
}

</style>