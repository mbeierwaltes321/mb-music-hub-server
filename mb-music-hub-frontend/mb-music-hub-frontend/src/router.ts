import { createMemoryHistory, createRouter, type RouteRecordRaw, type Router, type RouterOptions } from "vue-router";

import Introduction from "./components/Introduction.vue";
import Home from "./components/Home.vue";
//TODO - Add remaining routes

const routes: RouteRecordRaw[] = [
    { path: "/", component: Introduction }
];

const router: Router = createRouter({
    //TODO - Eventually replace createMemoryHistroy with createWebHistory? Will be covered at end of router tutorial
    history: createMemoryHistory(),
    routes
});

export default router;