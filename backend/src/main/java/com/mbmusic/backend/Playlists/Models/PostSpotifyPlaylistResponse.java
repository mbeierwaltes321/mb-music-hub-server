package com.mbmusic.backend.Playlists.Models;

public class PostSpotifyPlaylistResponse {

    //Default constructor
    public PostSpotifyPlaylistResponse() {

    }

    //Constructor with parameters
    public PostSpotifyPlaylistResponse(boolean success, String playlistId, int status) {
        this.success = success;
        this.newPlaylistId = playlistId;
        this.status = status;
    }    

    //Constructor with error parameters
    public PostSpotifyPlaylistResponse(boolean success, String playlistId, int status, String errorMessage) {
        this.success = success;
        this.newPlaylistId = playlistId;
        this.status = status;
        this.errorMessage = errorMessage;
    }
    
    //#region " Members "
    //Flag that indicates whether the playlist creation was a success
    private boolean success;

    //The ID of the new playlist
    private String newPlaylistId;

    //The error message if present
    private String errorMessage = "";

    //The status code of the response
    private int status;

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    //#endregion
}
