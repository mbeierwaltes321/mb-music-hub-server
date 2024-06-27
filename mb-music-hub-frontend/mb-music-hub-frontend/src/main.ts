import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from "./router";

import setUpLocalStorage from './utilities/localStorage';

//Set up the local storage defaults
setUpLocalStorage();

createApp(App)
.use(router)
.mount('#app')