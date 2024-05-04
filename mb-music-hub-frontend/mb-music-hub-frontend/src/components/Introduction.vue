<script setup lang="ts">
</script>

<template>
    <div>
        <div id="IntroContainer" class="container-large text-center">
            <div class="row">
                <div class="col">
                    <h1 class="text-light">Hi! 👋</h1>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <p class="fs-2 text-light mt-md-3">Welcome to MB's Music Hub!</p>
                </div>
            </div>
            <div class="row">
                <a href="#" class="fs-2 text-primary mt-md-3">Who am I?</a>
            </div>
            <div class="row">
                <button type="button" class="btn btn-primary mt-md-5"> Let's Go! </button>
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

p {
    animation: fadeIn 1.5s;
    font-family: 'Abyssinica SIL';
}

a {
    animation: fadeIn 2.5s;
}

button {
    animation: fadeIn 3.5s;
}


</style>