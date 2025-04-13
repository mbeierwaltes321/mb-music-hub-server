<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useTheme } from 'vuetify';
import { VContainer, VCol, VRow, VCard, VCardText, VHover, VCarousel, VCarouselItem, VSheet } from 'vuetify/components';

//Set the horizontal line depending on the color theme
const theme = useTheme();
const horizontalLineColor = ref(theme.global.current.value.dark ? "#8B8B00" : "#00008B");

/**
 * This function navigates the user to the youtube mixes component
 */
function navigateToYoutubeMixes() {
    alert("You clicked on the youtube mixes button!");
}

/**
 * This function navigates the user to the Spotify Playlists component
 */
function navigateToSpotifyPlaylists() {
    alert("You clicked on the Spotify Playlists button");
}

function navigateToPodcasts() {
    alert("You clicked on the podcasts button");
}

const tempWindowObjects = [
    {index: 0, podcastName: "Enciclopedia dei Videogiochi"},
    {index: 1, podcastName: "Conan O'Brien Needs a Friend"},
    {index: 2, podcastName: "Norah Jones Is Playing Along"},
    {index: 3, podcastName: "Italian Stories with Davide"},
    {index: 4, podcastName: "Gitbar - Italian developer podcast"}
]

watch(() => theme.global.current.value.dark, () => {
    horizontalLineColor.value = theme.global.current.value.dark ? "#8B8B00" : "#00008B";
});

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
                   <v-row align="center" justify="center" no-gutters>
                        <v-col sm="auto"
                               :md="3"
                               class="d-flex justify-center">
                            <!--TODO: Add a background image for the hover-->
                            <v-hover>
                                <template #default="{isHovering, props}">
                                    <v-card class="music-items"
                                            title="YouTube Mixes"
                                            v-bind="props"
                                            :color="isHovering ? 'red-darken-4' : undefined"
                                            @click="navigateToYoutubeMixes">
                                        <v-card-text>The music mixes I listen to while working!</v-card-text>
                                    </v-card>
                                </template>
                            </v-hover>
                        </v-col>
                        <v-col sm="auto" 
                               :md="3"
                               :offset-sm="1"
                               class="d-flex justify-center">
                            <!--TODO: Add a background image for the hover-->
                            <v-hover>
                                <template #default="{isHovering, props}">
                                    <v-card class="music-items" 
                                            title="Spotify Playlists"
                                            v-bind="props"
                                            :color="isHovering ? 'light-green-darken-1' : undefined"
                                            @click="navigateToSpotifyPlaylists">
                                        <v-card-text>Browse the different playlists that I have created!</v-card-text>
                                    </v-card>
                                </template>
                            </v-hover>
                        </v-col>
                   </v-row>
            </v-col>
            <v-col :cols="12">              
                <div id="HomePodcastsSection"
                     class="d-flex flex-column align-items-center">
                     <h2>Podcasts</h2>
                     <hr>
                     <v-carousel cycle
                                 show-arrows
                                 :interval="4000"
                                 :color="theme.current.value.dark ? 'white' : 'black'"
                                 :height="300">
                         <v-carousel-item v-for="pod in tempWindowObjects"
                                          :key="pod.index"
                                          :aspect-ratio="1">
                            <v-sheet class="d-flex justify-center align-center w-100 h-100"
                                     @click="navigateToPodcasts">
                                <span class="text-h2">{{ pod.podcastName }}</span>
                            </v-sheet>
                         </v-carousel-item>
                     </v-carousel>
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

.music-items {
    width: 225px;
}


</style>