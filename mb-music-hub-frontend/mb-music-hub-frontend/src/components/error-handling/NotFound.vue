<script setup lang="ts">
import { useRoute } from 'vue-router';

const route = useRoute();

console.log(route.params);
</script>

<template>
    <h1>TODO - Implement Not Found Page :)</h1>
    <p>Oops! There is no page associated with {{ $route.fullPath }} </p>
    <p>Params:</p>
    <ul>
        <li v-for="param in route.params.pathMatch"> {{ param }}</li>
    </ul>
</template>