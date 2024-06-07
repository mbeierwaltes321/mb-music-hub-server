<script setup lang="ts">
import SpotifyLoginIcon from "./icons/SpotifyLoginIcon.vue";
import { type Ref, ref } from "vue";

const theme : Ref<string | null> = ref(localStorage.getItem("colorTheme"));

</script>

<template>
    <nav id="NavigationBar" :class="'navbar navbar-expand-lg bg-' + theme" :data-bs-theme="theme">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">MB's Music Hub</a>
            <div class="collapse navbar-collapse" id="navbarMediaSelection">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="#">Home</a>
                    </li>
                    <li class="nav-item dropdown">
                        <button class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown" aria-expanded="false">
                            Media
                        </button>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="#">Playlists</a></li>
                            <li><a class="dropdown-item" href="#">Podcasts</a></li>
                            <li><a class="dropdown-item" href="#">YouTube Mixes</a></li>
                        </ul>
                    </li>
                </ul>
                <SpotifyLoginIcon/>
            </div>
            <div class="d-flex flex-row-reverse">
                <button id="LoginHamburger" class="navbar-toggler mt-2" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMediaSelection" aria-controls="navbarSupportedContent" aria-expanded="false">
                    <span class="navbar-toggler-icon"></span>
                </button>
            </div>
        </div>
    </nav>
</template>