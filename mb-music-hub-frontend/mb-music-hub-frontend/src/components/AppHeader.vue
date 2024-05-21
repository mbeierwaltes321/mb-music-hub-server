<script setup lang="ts">

</script>

<template>
    <nav id="NavigationBar" class="navbar navbar-expand-lg bg-light">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">MB's Music Hub</a>
            <div class="collapse navbar-collapse" id="navbarMediaSelection">
                <ul class="navbar-nav">
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
            </div>
            <div class="d-flex flex-row-reverse">
                <button id="LoginHamburger" class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMediaSelection" aria-controls="navbarSupportedContent" aria-expanded="false">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div id="spotifyLoginButton" class="me-2">
                    <div>
                        <img src="./../assets/stadium-light-plain.svg"> 
                    </div>
                </div>
            </div>
        </div>
    </nav>
</template>

<style scoped>
#app-logo {
    width: 50px;
    height: 50px;
}
</style>