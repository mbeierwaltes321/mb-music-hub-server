package com.mbmusic.backend.Connections;

import java.time.LocalDateTime;

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
    private static final String clientId = System.getenv("SpotifyClientId");

    //The client secret used for generating the access token
    private static final String clientSecret = System.getenv("SpotifyClientSecret");

    //The time date and time which the access token was last generated
    private static LocalDateTime tokenAccessed;

    //The api object that contains the connection information
    private static final SpotifyApi apiClient = new SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .build();

    private final ClientCredentialsRequest clientCredentialsRequest = apiClient.clientCredentials()
        .build();

    //#endregion

    //#region " Methods "
    
    //Getter for the Spotify API client, first refreshes the access token
    public SpotifyApi getApiClient() {

        //First, check if the access token exists, or if it expired
        if (apiClient.getAccessToken() == null || java.time.LocalDateTime.now().isAfter(tokenAccessed.plusHours(1))) {
            if(refreshAccessToken() == false) {
                //Return null, there was an error setting the credentials
                return null;
            }
        }
        
        return apiClient;
    }

    //This method refreshes the access token for the api client
    private boolean refreshAccessToken() {

        //First create a return variable
        boolean refreshSuccessful = false;

        try {
            //First grab the credentails, and set the access token
            ClientCredentials creds = clientCredentialsRequest.execute();
            apiClient.setAccessToken(creds.getAccessToken());

            //Update the access token time
            tokenAccessed = java.time.LocalDateTime.now();

            //Success, set the return variable to true
            refreshSuccessful = true;
        } catch (Exception e) {
            // Print out exception
            System.out.println(e.getMessage());
            
            //Failed, so return variable remains false
        }

        return refreshSuccessful;
    } 


    //#endregion
}
