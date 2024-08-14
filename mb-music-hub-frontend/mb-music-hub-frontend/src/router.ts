import { createMemoryHistory, createRouter, type RouteRecordRaw, type Router, type RouterOptions } from "vue-router";

import Introduction from "./components/Introduction.vue";
import Layout from "./components/layout/Layout.vue";
//TODO - Add remaining routes

const routes: RouteRecordRaw[] = [
    { path: "/", component: Layout },
    { path: "/intro", component: Introduction}
];

const router: Router = createRouter({
    //TODO - Eventually replace createMemoryHistroy with createWebHistory? Will be covered at end of router tutorial
    history: createMemoryHistory(),
    routes
});

export default router;