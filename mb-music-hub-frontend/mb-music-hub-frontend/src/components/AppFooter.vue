<script setup lang="ts">

const gitHubLogoPath : string = "src/assets/github-mark-white.svg";
const linkedInIconPath : string = "src/assets/LI-In-Bug.png"; 
const spotifyIconPath : string = "src/assets/Spotify_Icon_RGB_White.png";

const imgProperties = [
    { id: "logo-github", path: gitHubLogoPath, width: "50px", length: "50px", link: "https://github.com/mbeierwaltes321"},
    { id: "logo-spotify", path: spotifyIconPath, width: "50px", length: "50px", link: "https://open.spotify.com/user/1256937600?si=2b4a04ed6d3e4ac7"},
    { id: "logo-linkedin", path: linkedInIconPath, width: "50px", length: "58.7963px", link: "www.linkedin.com/in/michael-beierwaltes-703a93172"},
]

</script>

<template>
    <div class="container-fluid h-25">
        <div id="Links" class="d-flex align-items-center mt-2">
            <div v-for="imgProp in imgProperties"> 
                <a :href="imgProp.link">
                    <img v-if="imgProp.path != ''" 
                        :id="imgProp.id" 
                        class="m-2" 
                        :src="imgProp.path" 
                        :length="imgProp.length"
                        :width="imgProp.width"/>
                    <p v-else id="tempIcon" class="m-2">
                        TEMP
                    </p>
                </a>
            </div>
        </div>
    </div>
</template>