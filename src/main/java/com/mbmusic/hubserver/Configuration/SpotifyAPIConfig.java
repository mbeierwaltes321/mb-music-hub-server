package com.mbmusic.hubserver.Configuration;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;

/**
 * This class contains all the configuration needed to build a Spotify API Client
 */
@Configuration
public class SpotifyAPIConfig {

    //#region Members
        
    //The client ID used for generating the access token
    private static final String clientId = System.getenv("SpotifyClientId");

    //The client secret used for generating the access token
    private static final String clientSecret = System.getenv("SpotifyClientSecret");

    //The redirection URI for authorization requests
    //TODO - Eventually set this to an environment variable
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080/api/conn/redirect"); 

    //The api object that contains the connection information
    private final SpotifyApi.Builder spotifyApiClientBuilder = new SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setRedirectUri(redirectUri);

    //#endregion

    //#region Methods

    @Bean
    public SpotifyApi.Builder getSpotifyApiBuilder() {
        return spotifyApiClientBuilder;
    }

    //#endregion

}
