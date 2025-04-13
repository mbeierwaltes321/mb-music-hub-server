import { createWebHistory, createRouter, type RouteRecordRaw, type Router, type RouterOptions } from "vue-router";

import Introduction from "../components/Introduction.vue";
import Layout from "../components/layout/Layout.vue";
import Home from "@/components/Home.vue";
import AboutMe from "@/components/AboutMe.vue";
//TODO - Add remaining routes

//TODO - Add a 404 page and a route to catch it: https://router.vuejs.org/guide/essentials/dynamic-matching.html#Catch-all-404-Not-found-Route
const routes: RouteRecordRaw[] = [
    { 
        path: "/intro",
        name: "introduction",
        component: Introduction 
    },
    { 
        path: "/",
        component: Layout,
        children: [
            {
                path: "",
                name: "home",
                component: Home
            },
            {
                path: "aboutme",
                name: "aboutme",
                component: AboutMe
            }
        ]
    }
];

const router: Router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;