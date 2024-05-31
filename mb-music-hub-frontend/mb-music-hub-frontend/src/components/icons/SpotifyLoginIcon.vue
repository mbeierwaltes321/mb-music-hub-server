<script setup lang="ts">
import { type Ref, ref } from "vue";

//Grab theme and decide on the image based on the theme
const theme : Ref<string | null> = ref(localStorage.getItem("colorTheme"));

//Build image path
const imgSrc : string = `src/assets/stadium-${theme.value}-plain.svg`

</script>

<template>
    <div id="spotifyLoginButton" class="me-2 position-relative">
        <div>
            <p :class="' z-1 position-absolute mt-1 fs-5 text-' + (theme == 'light' ? 'dark' : 'light')">Login</p>
            <div class="z-0">
                <img :src="imgSrc">            
            </div>
        </div>
    </div>
</template>

<style scoped>
    p {
        user-select: none;
        margin-left: 3.3em;
    }
</style>