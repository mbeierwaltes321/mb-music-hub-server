<script setup lang="ts">
import Introduction from "./components/Introduction.vue";
import AppHeader from "./components/AppHeader.vue";

//Set the light mode/dark mode
if(!localStorage.getItem("colorTheme"))
  localStorage.setItem("colorTheme", "dark");

</script>

<template>
    <Introduction class="d-flex justify-content-center h-auto"></Introduction>
</template>

<style scoped>
#app-header {
  min-height: 10vh;
}
</style>