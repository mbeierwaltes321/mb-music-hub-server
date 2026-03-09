package com.mbmusic.hubserver.Common;

import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mbmusic.hubserver.Connections.SpotifyApiConnection;
import com.mbmusic.hubserver.Connections.SpotifyApiGateway;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;

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
     * This method builds and retrieves a Spotify Gateway object responsible for reaching out to the Spotify API
     * @param sessionIdString The ID of the front end session for which to retrieve the Spotify token infromation
     */
    public CompletableFuture<SpotifyApiGateway> getSpotifyApiGatewayAsync(String sessionIdString) throws InvalidSessionIdException {
        //Retrieve the token and prepare the Spotify API Client
        return spotifyApiConnection.createApiClient(sessionIdString)
            .thenApply(spotifyApi -> new SpotifyApiGateway(spotifyApi));
    }

    //#endregion
}
