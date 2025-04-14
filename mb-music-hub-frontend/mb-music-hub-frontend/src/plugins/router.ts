import { createWebHistory, createRouter, type RouteRecordRaw, type Router, type RouterOptions } from "vue-router";

import Introduction from "../components/Introduction.vue";
import Layout from "../components/layout/Layout.vue";
import Home from "@/components/Home.vue";
import AboutMe from "@/components/AboutMe.vue";
import NotFound from "@/components/error-handling/NotFound.vue";

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
                name: "AboutMe",
                component: AboutMe
            },
            {
                path: ":pathMatch(.*)*",
                name: "NotFound",
                component: NotFound
            }
        ]
    }
];

const router: Router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;