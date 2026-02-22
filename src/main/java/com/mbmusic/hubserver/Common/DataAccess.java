package com.mbmusic.hubserver.Common;

import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mbmusic.hubserver.Connections.SpotifyApiConnection;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;

import se.michaelthelin.spotify.SpotifyApi;

/**
 * This class provides base functionality for all data access classes
 */
@Component
public class DataAccess {

    //#region Members

    private SpotifyApiConnection spotifyApiConnection;
    
    //#endregion

    @Autowired(required = true)
    public DataAccess(SpotifyApiConnection spotifyApiConnection) {
        this.spotifyApiConnection = spotifyApiConnection;
    }

    //#region Methods

    /**
     * This method retrieves the Spotify Token information necessary for reaching out to the Spotify API
     * @param sessionIdString The ID of the front end session for which to retrieve the SpotifyTokenInformation
     */
    public CompletableFuture<SpotifyApi> getSpotifyClient(String sessionIdString) throws InvalidSessionIdException {
        //Retrieve the token and prepare the Spotify API Client
        return spotifyApiConnection.createApiClient(sessionIdString);
    }

    //#endregion
}
