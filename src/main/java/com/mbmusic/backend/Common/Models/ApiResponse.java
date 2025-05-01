package com.mbmusic.backend.Common.Models;

import java.io.Serializable;

//This class sets up the general response of a request from the server
public class ApiResponse<T> implements Serializable{
    //#region " Members "
    // This member contains the token information which is returned to the user
    private com.mbmusic.backend.Connections.Models.SpotifyTokenInfo spotifyTokenInfo;

    //This field resembles the content returned to the user 
    private T responseContent;

    //#endregion

    //#region " Getters/Setters "

    public com.mbmusic.backend.Connections.Models.SpotifyTokenInfo getSpotifyTokenInfo() {
        return spotifyTokenInfo;
    }

    public void setSpotifyTokenInfo(com.mbmusic.backend.Connections.Models.SpotifyTokenInfo spotifyTokenInfo) {
        this.spotifyTokenInfo = spotifyTokenInfo;
    }

    public Object getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(T responseContent) {
        this.responseContent = responseContent;
    }

    //#endregion

}
