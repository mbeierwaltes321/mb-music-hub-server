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
public abstract class DataAccessBase {

    //#region Members

    private SpotifyApiConnection spotifyApiConnection;

    @Autowired
    public final void setSpotifyApiConnection(SpotifyApiConnection spotifyApiConnection) {
        this.spotifyApiConnection = spotifyApiConnection;
    }

    protected SpotifyApi spotifyClient;
    
    //#endregion

    //#region Methods

    /**
     * This constructor performs all operations necessary priror to retrieving data
     * @param sessionIdString String representation of the session id being passed in from the user
     * @throws Exception 
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     */
    protected DataAccessBase(String sessionIdString) throws Exception {
        try {
            prepareSpotifyClient(sessionIdString);
        } catch (Exception e) {
            throw e;
        }
    } 

    /**
     * This method retrieves the Spotify Token information necessary for reaching out to the Spotify API
     * @param sessionIdString The ID of the front end session for which to retrieve the SpotifyTokenInformation
     * @throws IOException 
     * @throws SpotifyWebApiException 
     * @throws ParseException 
     */
    private void prepareSpotifyClient(String sessionIdString) throws InterruptedException, ExecutionException, ParseException, SpotifyWebApiException, IOException {
        if (sessionIdString == null)
            spotifyClient = null;
        
        //Retrieve the token and prepare the Spotify API Client
        spotifyClient = spotifyApiConnection.createApiClient(sessionIdString).get();
    }

    //#endregion
}
