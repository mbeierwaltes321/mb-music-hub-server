package com.mbmusic.hubserver.Common;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mbmusic.hubserver.Connections.SpotifyApiConnection;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

/**
 * This class provides base functionality for all data access classes
 */
@Component
public class DataAccess {

    //#region Members

    private SpotifyApiConnection spotifyApiConnection;

    private SpotifyApi spotifyClient;
    
    //#endregion

    @Autowired(required = true)
    public DataAccess(SpotifyApiConnection spotifyApiConnection) {
        this.spotifyApiConnection = spotifyApiConnection;
    }

    //#region Getters/Setters

    public SpotifyApi getSpotifyClient(String sessionIdString) {
        try {
            prepareSpotifyClient(sessionIdString);
            return spotifyClient;
        } catch (Exception e) {
            return null;
        }
    }

    //#endregion

    //#region Methods

    /**
     * This method retrieves the Spotify Token information necessary for reaching out to the Spotify API
     * @param sessionIdString The ID of the front end session for which to retrieve the SpotifyTokenInformation
     * @throws IOException 
     * @throws SpotifyWebApiException 
     * @throws ParseException 
     */
    public void prepareSpotifyClient(String sessionIdString) throws InterruptedException, ExecutionException, ParseException, SpotifyWebApiException, IOException {
        if (sessionIdString == null)
            spotifyClient = null;
        
        //Retrieve the token and prepare the Spotify API Client
        spotifyClient = spotifyApiConnection.createApiClient(sessionIdString).get();
    }

    //#endregion
}
