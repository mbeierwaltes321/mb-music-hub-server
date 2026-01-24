package com.mbmusic.hubserver.Connections;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;

/**
 * This class handles building a connection to the Spotify API
 */
@Component
public class SpotifyApiConnection {
    
    //#region " Members "

    @Autowired
    private SpotifyApi.Builder spotifyApiClientBuilder;

    @Autowired 
    ValkeyClient valkeyClient;

    //#endregion

    //#region " Methods "

    /**
     * Retrieves a new Spotify API Client without any authorization information.
     * @return 
     */
    public SpotifyApi createApiClient() {
        return spotifyApiClientBuilder.build();
    }

    public CompletableFuture<SpotifyApi> createApiClient(String sessionId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException, InterruptedException, ExecutionException {
        UUID sessionUuid = UUID.fromString(sessionId);

        return valkeyClient.getSpotifyAPITokenAsync(sessionUuid)
        .thenApply(this::buildClientFromTokens);
    }

    /**
     * This method creates a Spotify API client with the provided authorization token information. It handles token refreshing if necessary.
     * @param authTokens The authorization token information to set to the Spotify API Client
     * @return The Spotify API Client object
     * @throws IOException
     * @throws SpotifyWebApiException
     * @throws org.apache.hc.core5.http.ParseException
     */
    private SpotifyApi buildClientFromTokens(SpotifyTokenInfo authTokens) {

        try {
            if (authTokens == null)
                return null;
            
            final SpotifyApi apiClient = createApiClient();

            //First populate the client with the correct tokens
            apiClient.setAccessToken(authTokens.getAccessToken());
            apiClient.setRefreshToken(authTokens.getRefreshToken());
            
            //Now see if the authorization token is expired.
            //The LocalDateTime should already be in UTC from the frontend
            LocalDateTime authDateTime = authTokens.getTokenGeneratedAt();

            //Get the current time in UTC
            //Get a UTC ZoneId
            final ZoneId UTC = ZoneId.of("UTC");
            final LocalDateTime currentTimeUTC = LocalDateTime.now(UTC);

            //Determine whether the token is expired and needs to be refreshed
            boolean tokenExpired = currentTimeUTC.isAfter(authDateTime.plusSeconds(authTokens.getExpiresIn()));
            if (tokenExpired) {
                //Create an authorization code refresh request
                final AuthorizationCodeRefreshRequest refreshRequest = apiClient.authorizationCodeRefresh().build();

                //Perform the refresh
                final AuthorizationCodeCredentials newCreds = refreshRequest.execute();

                //Now update the API Client's credntials
                apiClient.setAccessToken(newCreds.getAccessToken());

                //Finally, set the auth token object's fields
                authTokens.setTokenGeneratedAt(currentTimeUTC);
                authTokens.setExpiresIn(newCreds.getExpiresIn());
                authTokens.setAccessToken(apiClient.getAccessToken());

            }
            
            //Return the api client
            return apiClient;

        } catch (Exception e) {
            throw new CompletionException(e);
        }
        

    }

    //#endregion
}
