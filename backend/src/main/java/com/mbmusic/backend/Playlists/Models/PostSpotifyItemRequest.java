package com.mbmusic.backend.Playlists.Models;

import java.util.List;

import com.mbmusic.backend.Connections.Models.SpotifyTokenInfo;

//This class models the request body for the post spotify item endpoint
public class PostSpotifyItemRequest {
    
    //#region " Members "

    //The token information associated with the request
    private SpotifyTokenInfo authTokens;

    //The ID of the playlist which the items will be added
    private String playlistId;

    //The array of spotify items that will be added to the playlist
    private List<String> spotifyItems;

    //#endregion

    //#region " Getters / Setters "

    public SpotifyTokenInfo getAuthTokens() {
        return authTokens;
    }

    public void setAuthTokens(SpotifyTokenInfo authTokens) {
        this.authTokens = authTokens;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public List<String> getSpotifyItems() {
        return spotifyItems;
    }

    public void setSpotifyItems(List<String> spotifyItems) {
        this.spotifyItems = spotifyItems;
    }

    //#endregion

}