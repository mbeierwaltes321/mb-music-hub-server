<script setup lang="ts">

//Define interfaces for the footer data
interface FooterLinkData {
    text: string,
    link: string,
    last?: boolean,
    first?: boolean
}

interface FoooterIconData {
    id: string,
    path: string,
    width: string,
    length: string,
    link: string
}

const gitHubIconPath : string = "src/assets/github-mark-white.svg";
const linkedInIconPath : string = "src/assets/LI-In-Bug.png"; 
const spotifyIconPath : string = "src/assets/Spotify_Icon_RGB_White.png";

const imgProperties: FoooterIconData[] = [
    { id: "logo-github", path: gitHubIconPath, width: "50px", length: "50px", link: "https://github.com/mbeierwaltes321"},
    { id: "logo-spotify", path: spotifyIconPath, width: "50px", length: "50px", link: "https://open.spotify.com/user/1256937600?si=2b4a04ed6d3e4ac7"},
    { id: "logo-linkedin", path: linkedInIconPath, width: "50px", length: "58.7963px", link: "https://www.linkedin.com/in/michael-beierwaltes-703a93172"},
]

const footerLinks: FooterLinkData[] = [
    //TODO Fill these with links for eventual routing
    {text: "About Me", link: "", first: true},
    {text: "Contact Me", link: "mailto:mbeierwaltes321@.com"},
    {text: "Terms & Conditions", link: ""},

    //TODO - Either get rid of this or put actual copyright information when needed
    {text: "Copyright Information", link: "", last: true},
]

</script>

<template>
    <div id="FooterContainer" class="container-fluid vh-25">
        <div id="Links" class="d-flex align-items-center h-25 mt-2">
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
        <div class="h-75 row row-cols-1 flex-column">
                <div v-for="footerLink in footerLinks" 
                    :class="`col ` + (footerLink.first === true ? ' mt-3' : '')
                        + (footerLink.last === true? `mt-auto` : ``)">
                    <a :href="footerLink.link" class="text-dark-emphasis">
                        {{ footerLink.text }}
                    </a>
                </div>
        </div>
    </div>
</template>

<style scoped>

#FooterContainer {
    background-color: rgb(75, 0, 0);
    height: 40vh;
}

</style>