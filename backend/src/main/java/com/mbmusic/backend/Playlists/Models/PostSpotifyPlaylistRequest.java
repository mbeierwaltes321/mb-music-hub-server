package com.mbmusic.backend.Playlists.Models;

import java.util.List;

import com.mbmusic.backend.Connections.Models.SpotifyTokenInfo;

//This class models the request body for the post spotify playlist endpoint
public class PostSpotifyPlaylistRequest {
    
    //#region " Members "

    //The token information associated with the request
    private SpotifyTokenInfo authTokens;

    //The name of the playlist to create
    private String playlistName;

    //The description of the playlist to create
    private String playlistDescription;

    //Array of items (tracks, episodes, etc.) to add to the new playlist
    private List<String> spotifyURIs;

    //Indicates whether the newly created playlist is public or private
    private boolean isPublic;

    //#endregion

    //#region " Getters/Setters "
    public SpotifyTokenInfo getAuthTokens() {
        return authTokens;
    }

    public void setAuthTokens(SpotifyTokenInfo authTokens) {
        this.authTokens = authTokens;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistDescription() {
        return playlistDescription;
    }

    public void setPlaylistDescription(String playlistDescription) {
        this.playlistDescription = playlistDescription;
    }

    public List<String> getSpotifyURIs() {
        return spotifyURIs;
    }

    public void setSpotifyURIs(List<String> spotifyURIs) {
        this.spotifyURIs = spotifyURIs;
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    //#endregion

}
