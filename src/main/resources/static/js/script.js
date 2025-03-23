let currentTheme = getTheme();

//inital -->

document.addEventListener('DOMContentLoaded',()=>{
    changeTheme();
});

// change the theme
function changeTheme(){
    //set to web page
    changePageTheme(currentTheme, currentTheme);

    //set the listner to change theme button
    const changeThemeButton = document.querySelector('#theme_change_btn');

    changeThemeButton.addEventListener("click",(event)=>{
        let oldTheme = currentTheme;
        if(currentTheme === "dark"){
            //theme to light
            currentTheme = "light";
        }else{
            currentTheme = "dark";
        }

        changePageTheme(currentTheme, oldTheme);
    });
}

//set theme to local storage
function setTheme(theme){
    localStorage.setItem("theme",theme);
}

//get theme from localstorage
function getTheme(){
    let theme = localStorage.getItem("theme");
    return theme ? theme : "light";
}

// change current page theme
function changePageTheme(theme, oldTheme){

     //update the localstorage
     setTheme(currentTheme);
    //  remove the current theme
    if(oldTheme){
        document.querySelector('html').classList.remove(oldTheme);
    }
    // set new
     document.querySelector('html').classList.add(theme);

     // change the text of button
     document.querySelector('#theme_change_btn')
     .querySelector('span').textContent = theme == 'light' ? 'Dark' : 'Light';

}