import { createWebHistory, createRouter, type RouteRecordRaw, type Router, type RouterOptions } from "vue-router";

import Introduction from "../components/Introduction.vue";
import Layout from "../components/layout/Layout.vue";
//TODO - Add remaining routes

//TODO - Add a 404 page and a route to catch it: https://router.vuejs.org/guide/essentials/dynamic-matching.html#Catch-all-404-Not-found-Route
const routes = [
    { path: "/", name: "introduction", component: Introduction },
    { path: "/home", name: "layout", component: Layout }
];

const router: Router = createRouter({
    //TODO - Eventually replace createMemoryHistroy with createWebHistory? Will be covered at end of router tutorial
    history: createWebHistory(),
    routes
});

export default router;