package com.mbmusic.hubserver.Connections;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Connections.Exceptions.SpotifyClientBuildException;
import com.mbmusic.hubserver.Connections.Exceptions.ValkeyOperationException;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

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
    public SpotifyApi createEmptyApiClient() {
        return spotifyApiClientBuilder.build();
    }

    /**
     * This method returns a {@link SpotifyApiGateway} object containing no authorization information
     * @return A {@link SpotifyApiGateway} object
     */
    public SpotifyApiGateway createEmptySpotifyApiGateway() {
        return new SpotifyApiGateway(createEmptyApiClient());
    }

    /**
     * This method creates a new Spotify Api client with the token information from the provided session id
     * @param sessionId The ID of the session for which to retrieve token information
     * @return A future with the populated Spotify API client
     * @throws InvalidSessionIdException
     */
    public CompletableFuture<SpotifyApi> createApiClientAsync(String sessionId) throws InvalidSessionIdException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new InvalidSessionIdException("Invalid Session Id");
        }

        UUID sessionUuid = UUID.fromString(sessionId);
        return valkeyClient.getSpotifyAPITokenAsync(sessionUuid)
        .thenCompose(this::buildSpotifyClientFromTokens);
    }

    /**
     * This method creates a Spotify API client with the provided authorization token information. It handles token refreshing if necessary.
     * @param authTokenInfo The authorization token information to set to the Spotify API Client
     * @return The Spotify API Client object
     */
    private CompletableFuture<SpotifyApi> buildSpotifyClientFromTokens(Pair<UUID, SpotifyTokenInfo> authTokenInfo) {

        if (authTokenInfo == null || authTokenInfo.getFirst() == null || authTokenInfo.getSecond() == null) {
            throw new SpotifyClientBuildException("Invalid token information for Spotify Client");
        }
        
        UUID sessionId = authTokenInfo.getFirst();
        SpotifyTokenInfo authTokens = authTokenInfo.getSecond();
        
        final SpotifyApi apiClient = createEmptyApiClient();

        //Now see if the authorization token is expired.
        //The LocalDateTime should already be in UTC from the frontend
        LocalDateTime authDateTime = authTokens.getTokenGeneratedAt();

        //Get the current time in UTC
        //Get a UTC ZoneId
        final ZoneId UTC = ZoneId.of("UTC");
        final LocalDateTime currentTimeUTC = LocalDateTime.now(UTC);

        //For debugging purposes; always triggers the token referesh
        //final LocalDateTime tempTimeUTC = LocalDateTime.MAX;

        boolean tokenExpired = currentTimeUTC.isAfter(authDateTime.plusSeconds(authTokens.getExpiresIn()));
        // boolean tokenExpired = tempTimeUTC.isAfter(authDateTime.plusSeconds(authTokens.getExpiresIn()));
        if (tokenExpired) {
            //Refresh the token, set the Spotify API, and then return the SpotifyAPI
            return refreshSpotifyTokenAync(sessionId, authTokens, apiClient, currentTimeUTC)
                .thenApply(updatedTokens -> {
                    apiClient.setAccessToken(updatedTokens.getAccessToken());
                    apiClient.setRefreshToken(updatedTokens.getRefreshToken());
                    return apiClient;
                });
        }

        apiClient.setAccessToken(authTokens.getAccessToken());
        apiClient.setRefreshToken(authTokens.getRefreshToken());

        return CompletableFuture.completedFuture(apiClient);
    }

    /**
     * This method performs a token refresh and updates the token information for the provided session id
     * @param sessionId The ID of the session for which to update the token information
     * @param authTokens The tokens information to be refereshed
     * @param apiClient The Spotify API for which to perform the refresh request
     * @param currentTimeUTC The current time in UTC
     * @return The spotify token information with updated tokens
     */
    private CompletableFuture<SpotifyTokenInfo> refreshSpotifyTokenAync(UUID sessionId, SpotifyTokenInfo authTokens,
        SpotifyApi apiClient, LocalDateTime currentTimeUTC) {

            SpotifyApiGateway spotifyApiGateway = new SpotifyApiGateway(apiClient);

            CompletableFuture<AuthorizationCodeCredentials> newCredsFuture = 
                spotifyApiGateway.refreshAuthorizationTokensAsync(authTokens.getRefreshToken());

            return newCredsFuture.thenApply(updatedTokens -> {

                //Set the auth token object's fields
                authTokens.setTokenGeneratedAt(currentTimeUTC);
                authTokens.setExpiresIn(updatedTokens.getExpiresIn());
                authTokens.setAccessToken(updatedTokens.getAccessToken());

                //This request may not return updated refresh tokens; only update it if it did
                if (updatedTokens.getRefreshToken() != null && !updatedTokens.getRefreshToken().isBlank()) {
                    authTokens.setRefreshToken(updatedTokens.getRefreshToken());
                }

                return authTokens;
            }).thenCompose(updatedTokens -> {
                String errorPrefix = String.format("Error updating token information in Valkey for session %s: ", sessionId.toString());
                try {
                    return valkeyClient.upsertSpotifyAPITokenAsync(sessionId, updatedTokens)
                        .thenApply(inserted -> {
                            if (!inserted) {
                                throw new ValkeyOperationException(errorPrefix + "Valkey returned an unsuccessful status");
                            }

                            return updatedTokens;
                        });
                } catch (InvalidSessionIdException e) {
                    e.printStackTrace();
                    throw new ValkeyOperationException(errorPrefix + e.getMessage(), e);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    throw new ValkeyOperationException(errorPrefix + e.getMessage(), e);
                }
            });
    }
    
    //#endregion
}
