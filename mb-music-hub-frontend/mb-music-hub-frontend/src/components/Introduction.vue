<template>
    <div class="h-auto mt-5">
        <div id="IntroContainer" class="container-md text-center">
            <div class="row">
                <div class="col">
                    <h1 class="text-light">Hi! 👋</h1>
                </div>
            </div>
            <div class="row justify-content-center">
                <div class="col-10">
                    <p id="Welcome" class="fs-2 text-light mt-3">Welcome to MB's Music Hub!</p>
                </div>
            </div>
            <div id="PoweredBy" class="row justify-content-center">
                <p class="col-md-auto fs-4 text-light mt-3">Powered By:</p>
                <img id="spotify-logo" class="col-7 ms-1" src="./../assets/Spotify_Logo_RGB_White.png">
            </div>
            <!-- <div class="row justify-content-center">
                <a href="#" class="col-md-auto fs-2 text-primary mt-3">Who am I?</a>
            </div> -->
            <div class="row justify-content-center">
                <button type="button" class="col-4 fs-5 btn btn-primary mt-4"> Let's Go! </button>
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