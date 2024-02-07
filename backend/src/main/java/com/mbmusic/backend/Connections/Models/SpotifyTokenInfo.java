package com.mbmusic.backend.Connections.Models;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

//This class models the return object containing the Spotify access
//token and the corresponding refresh token
public class SpotifyTokenInfo {

    //#region " Members "
    //The access token that will be used to authenticate spotify requests
    private String accessToken;

    //The refresh token used to re-authenticate the spotify api
    private String refreshToken;

    //The amount of time in seconds before the spotify token expires
    private int expiresIn;

    //The datetime which the token was generated/refreshed
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime tokenGeneratedAt;

    //#endregion

    //#region " Getters and Setters "

    public LocalDateTime getTokenGeneratedAt() {
        return tokenGeneratedAt;
    }

    public void setTokenGeneratedAt(LocalDateTime tokenGeneratedAt) {
        this.tokenGeneratedAt = tokenGeneratedAt;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    //Gets the spotfy access token
    public String getAccessToken() {
        return accessToken;
    }

    //Sets the spotify access token
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    //Gets the spotify refresh token
    public String getRefreshToken() {
        return refreshToken;
    }

    //Sets the refresh access token
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    //#endregion
    
}