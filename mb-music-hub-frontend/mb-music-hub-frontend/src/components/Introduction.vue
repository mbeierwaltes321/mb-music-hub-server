<script setup lang="ts">
</script>

<template>
    <div class="mt-5">
        <div id="IntroContainer" class="container-large text-center">
            <div class="row">
                <div class="col">
                    <h1 class="text-light">Hi! 👋</h1>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <p id="Welcome" class="fs-2 text-light mt-3">Welcome to MB's Music Hub!</p>
                </div>
            </div>
            <div id="PoweredBy" class="d-flex justify-content-center">
                <p class="fs-3 text-light mt-3">Powered By:</p>
                <img id="spotify-logo" class="ms-3 align-self-center" src="./../assets/Spotify_Logo_RGB_White.png">
            </div>
            <div class="row">
                <a href="#" class="fs-2 text-primary mt-3">Who am I?</a>
            </div>
            <div class="row">
                <button type="button" class="btn btn-primary mt-5"> Let's Go! </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* Set up fadeIn CSS so that the text fades in */
@keyframes fadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}

h1 {
    animation: fadeIn 1s;
}

#Welcome {
    animation: fadeIn 1.5s;
    font-family: 'Abyssinica SIL';
}

#PoweredBy {
    animation: fadeIn 2s;
}

a {
    animation: fadeIn 2.5s;
}

button {
    animation: fadeIn 3.5s;
}

#spotify-logo {
    width: 13.36em;
    height: 4em;
}

</style>