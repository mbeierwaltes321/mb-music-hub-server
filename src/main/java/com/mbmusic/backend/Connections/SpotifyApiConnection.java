package com.mbmusic.backend.Connections;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.mbmusic.backend.Connections.Models.SpotifyTokenInfo;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;

// This class handles the connection between the music hub
// and the Spotify API
@Component
public class SpotifyApiConnection {
    
    //#region " Members "
        
    //The client ID used for generating the access token
    private static final String clientId = System.getenv("SpotifyClientId");

    //The client secret used for generating the access token
    private static final String clientSecret = System.getenv("SpotifyClientSecret");

    //The redirection URI for authorization requests
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080/api/conn/redirect"); 

    //The api object that contains the connection information
    private final SpotifyApi apiClient = new SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setRedirectUri(redirectUri)
        .build();

    //#endregion

    //#region " Methods "

    //Simple getter that returns the Spotify API client
    public SpotifyApi getApiClient() {
        
        return apiClient;
    }

    //This method sets the authroization tokens with the Spotify API client,
    //refreshes the token if necessary, updates the tokens object, and the finally returns
    //the object
    public SpotifyApi getApiClient(SpotifyTokenInfo authTokens)
        throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException{

        //First populate the client with the correct tokens
        this.apiClient.setAccessToken(authTokens.getAccessToken());
        this.apiClient.setRefreshToken(authTokens.getRefreshToken());
        
        //Now see if the authorization token is expired.
        //The LocalDateTime should already be in UTC from the frontend
        LocalDateTime authDateTime = authTokens.getTokenGeneratedAt();

        //Get the current time in UTC
        //Get a UTC ZoneId
        final ZoneId UTC = ZoneId.of("UTC");
        final LocalDateTime currentTimeUTC = LocalDateTime.now(UTC);

        //Determine whether the token is expired and needs to be refreshed
        boolean tokenExpired = currentTimeUTC.isAfter(authDateTime.plusSeconds(authTokens.getExpiresIn()));
        if(tokenExpired) {
            //Create an authorization code refresh request
            final AuthorizationCodeRefreshRequest refreshRequest = this.apiClient.authorizationCodeRefresh().build();

            //Perform the refresh
            final AuthorizationCodeCredentials newCreds = refreshRequest.execute();

            //Now update the API Client's credntials
            this.apiClient.setAccessToken(newCreds.getAccessToken());

            //Finally, set the auth token object's fields
            authTokens.setTokenGeneratedAt(currentTimeUTC);
            authTokens.setExpiresIn(newCreds.getExpiresIn());
            authTokens.setAccessToken(this.apiClient.getAccessToken());

        }
        
        //Return the api client
        return apiClient;

    }

    //#endregion
}
