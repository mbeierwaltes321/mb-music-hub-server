import { createMemoryHistory, createRouter, type RouteRecordRaw, type Router, type RouterOptions } from "vue-router";

import Introduction from "./components/Introduction.vue";
//TODO - Add about page to put my "about me" page

const routes: RouteRecordRaw[] = [
    { path: "/", component: Introduction }
];

const router: Router = createRouter({
    //TODO - Eventually replace createMemoryHistroy with createWebHistory? Will be covered at end of router tutorial
    history: createMemoryHistory(),
    routes
});

export default router;