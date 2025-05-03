package com.mbmusic.hubserver;

import java.time.LocalDateTime;

import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

//This class contains common methods used in testing the backend application
class TestCommon {

    //This method utilizes the credentials from the environment variables to generate
    //the necessary spotify token information
    static SpotifyTokenInfo getSpotifyTokenInfo() {
        //First get the token information from the environment
        final String spotifyAccessToken = System.getenv("SpotifyAccessToken");
        final String spotifyRefreshToken = System.getenv("SpotifyRefreshToken");
        final LocalDateTime generatedAt = LocalDateTime.parse(System.getenv("SpotifyTokenGeneratedAt"));

        //Now create the parameters
        SpotifyTokenInfo authTokens = new SpotifyTokenInfo();
        authTokens.setAccessToken(spotifyAccessToken);
        authTokens.setRefreshToken(spotifyRefreshToken);
        authTokens.setTokenGeneratedAt(generatedAt);
        authTokens.setExpiresIn(3600);
                
        return authTokens;
    }
}
