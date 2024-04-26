<script setup lang="ts">
</script>

<template>
    <div id="IntroContainer" class="container text-center">
        <div class="row">
            <div class="col">
                <h1 class="text-light" style="animation: fadeIn 1s;">Hi! 👋</h1>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <p class="fs-2 text-light mt-md-3" style="font-family: 'Abyssinica SIL'; animation: fadeIn 1.5s">Welcome to MB's Music Hub!</p>
            </div>
        </div>
        <div class="row">
            <a href="#" class="fs-2 text-primary mt-md-3" style="animation: fadeIn 2.5s">Who am I?</a>
        </div>
        <div class="row">
            <button type="button" class="btn btn-primary mt-md-5" style="animation: fadeIn 3.5s"> Let's Go! </button>
        </div>
    </div>
</template>

<style>
/* Set up fadeIn CSS so that the text fades in */
@keyframes fadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}
</style>