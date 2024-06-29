import './assets/main.css'

//Vue/Vue router
import { createApp } from 'vue'
import router from "./router";

//Vuetify
import 'vuetify/styles';
import { createVuetify } from 'vuetify';

//Importing components
import App from './App.vue'

//Config/storage settings
import setUpLocalStorage from './utilities/localStorage';

//Set up the local storage defaults
setUpLocalStorage();

//Use vuetify
const vuetify = createVuetify();

createApp(App)
.use(router)
.use(vuetify)
.mount('#app')