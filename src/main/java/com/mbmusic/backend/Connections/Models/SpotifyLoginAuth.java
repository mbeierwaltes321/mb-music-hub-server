package com.mbmusic.backend.Connections.Models;

//This class models the return object containing the Spotify login
//url and the state used to verify the request
public class SpotifyLoginAuth {

    //The URL that will be used to authenticate spotify
    private String loginUrl;

    //The state string which will be used to verify the login request
    private String state;

    //Gets the login URL
    public String getLoginUrl() {
        return loginUrl;
    }

    //Sets the login URL
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    //Gets the login state
    public String getState() {
        return state;
    }

    //Sets the login state
    public void setState(String state) {
        this.state = state;
    }
    
}