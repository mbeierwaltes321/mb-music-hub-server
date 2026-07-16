package com.mbmusic.hubserver.Playlists.Models;

public class PostSpotifyPlaylistResponse {

    //Default constructor
    public PostSpotifyPlaylistResponse() {

    }

    //Constructor with parameters
    public PostSpotifyPlaylistResponse(boolean success, String playlistId) {
        this.success = success;
        this.newPlaylistId = playlistId;
    }    

    //Constructor with error parameters
    public PostSpotifyPlaylistResponse(boolean success, String playlistId, String errorMessage) {
        this.success = success;
        this.newPlaylistId = playlistId;
        this.errorMessage = errorMessage;
    }
    
    //#region " Members "
    //Flag that indicates whether the playlist creation was a success
    private boolean success;

    //The ID of the new playlist
    private String newPlaylistId;

    //The error message if present
    private String errorMessage = "";

    //#endregion

    //#region " Getters/Setters "

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getNewPlaylistId() {
        return newPlaylistId;
    }

    public void setNewPlaylistId(String newPlaylistId) {
        this.newPlaylistId = newPlaylistId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    //#endregion
}
