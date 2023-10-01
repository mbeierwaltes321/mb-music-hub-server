package com.mbmusic.backend.Connections;

import org.springframework.stereotype.Component;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

// This class handles the connection between the music hub
// and the Spotify API
@Component
public class SpotifyApiConnection {
    
    //#region " Members "
        
    //The client ID used for generating the access token
    private final String clientId = System.getenv("SpotifyClientId");

    //The client secret used for generating the access token
    private final String clientSecret = System.getenv("SpotifyClientSecret");

    //The api object that contains the connection information
    private final SpotifyApi apiClient = new SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .build();

    private final ClientCredentialsRequest clientCredentialsRequest = apiClient.clientCredentials()
        .build();

    //#endregion

    //#region " Methods "
    
    //Getter for the Spotify API client, first refreshes the access token
    //TODO - Eventually phase this out in favor for a scheduled trigger on refreshing the access token
    public SpotifyApi getApiClient() {

        try {
            //First grab the credentails, and set the access token
            ClientCredentials creds = clientCredentialsRequest.execute();
            apiClient.setAccessToken(creds.getAccessToken());
        } catch (Exception e) {
            // Print out exception
            System.out.println(e.getMessage());
            
            //Failed, so return null;
            return null;
        }

        return apiClient;
    }


    //#endregion
}
