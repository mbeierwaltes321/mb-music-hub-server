
/**
 * This function initializes the theme of the app
 */
const initializeTheme = () => {
    //Set the light mode/dark mode
    if(!localStorage.getItem("colorTheme"))
        localStorage.setItem("colorTheme", "light");
}

/**
 * This function sets up the starting page
 */
const setFirstTime = () => {
    //Set the start page to the intro, if not visited before
    if(!localStorage.getItem("firstVisit"))
        localStorage.setItem("firstVisit", "true");
}

/**
 * This method calls all methods to set up the app
 */
const setUpLocalStorage = () => {
    initializeTheme();
    setFirstTime();
}

/**
 * This method updates the app's theme in local storage
 * @param theme The theme for which to update the local storage
 */
export const setThemeInLocalStorage = (theme: string) => {
    localStorage.setItem("colorTheme", theme);
}

export default setUpLocalStorage;