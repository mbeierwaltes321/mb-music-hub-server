
/**
 * This function initializes the theme of the app
 */
const initializeTheme = () => {
    //Set the light mode/dark mode
    if(!localStorage.getItem("colorTheme"))
        localStorage.setItem("colorTheme", "dark");
}

/**
 * This function sets up the starting page
 */
const initializeIntro = () => {
    //Set the start page to the intro, if not visited before
    if(!localStorage.getItem("start"))
        localStorage.setItem("start", "intro");
}

/**
 * This method calls all methods to set up the app
 */
const setUpLocalStorage = () => {
    initializeTheme();
    initializeIntro();
}

export default setUpLocalStorage;