<script setup lang="ts">
import Introduction from "./components/Introduction.vue";
import AppHeader from "./components/AppHeader.vue";
</script>

<template>
    <AppHeader id="app-header" class="d-flex "/>
    <Introduction class="d-flex justify-content-center h-auto"></Introduction>
  <!-- TODO - Define footer -->
</template>

<style scoped>
#app-header {
  min-height: 15vh;
}
</style>