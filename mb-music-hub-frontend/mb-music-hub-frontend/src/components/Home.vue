<script setup lang="ts">
import { ref, watch } from 'vue';
import { useTheme } from 'vuetify';
import { VContainer, VCol, VRow, VCard, VCardText, VSpacer } from 'vuetify/components';

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
                   class="d-flex flex-column align-items-center">
                <h1>Pick Your Experience</h1>
            </v-col>
            <v-col id="HomeMusicSection"
                   :cols="12"
                   class="d-flex flex-column">
                   <h2>Music</h2>
                   <hr>
                   <v-row align="center" justify="center">
                        <v-col sm="auto"
                               :md="3"
                               class="d-flex justify-center">
                            <v-card title="YouTube Mixes">
                                <v-card-text>The music mixes I listen to while working!</v-card-text>
                            </v-card>
                        </v-col>
                        <v-col sm="auto" 
                               :md="3"
                               :offset-sm="1"
                               class="d-flex justify-center">
                            <v-card title="Spotify Playlists">
                                <v-card-text>Browse the different playlists that I have created!</v-card-text>
                            </v-card>
                        </v-col>
                   </v-row>
            </v-col>
            <v-col :cols="12">              
                <div id="HomePodcastsSection"
                     class="d-flex flex-column align-items-center">
                     <h2>Podcasts</h2>
                     <hr>

                </div>
            </v-col>
        </v-row>
    </v-container>
</template>

<style scoped>

#HomeHeaderSection {
    height: 150px;
    padding-top: 3em;
}

#HomeMusicSection {
    height: 375px;
}

#HomePodcastsSection {
    height: 400px;
    margin-top: 50px;
}

hr {
    width: 100%;
    height: 3px;
    border: none;
    background-color: v-bind(horizontalLineColor);
}

h2 {
    align-self: center
}

.v-card {
    width: 225px;
}


</style>