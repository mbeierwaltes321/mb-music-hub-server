<script setup lang="ts">
import { onBeforeMount } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

onBeforeMount(() => {
    //Default the user to the intro page if it's their first time visitng
    if (localStorage.getItem("firstVisit") === "true") {
        router.replace({name: "introduction"});
    }
})

</script>

<template>
    <RouterView></RouterView>
</template>
