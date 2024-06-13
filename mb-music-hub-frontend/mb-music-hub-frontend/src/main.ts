import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'

//Set the light mode/dark mode
if(!localStorage.getItem("colorTheme"))
    localStorage.setItem("colorTheme", "dark");

createApp(App).mount('#app')