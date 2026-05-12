package com.mbmusic.hubserver.Connections;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Connections.Exceptions.SpotifyClientBuildException;
import com.mbmusic.hubserver.Connections.Exceptions.ValkeyOperationException;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

@SpringBootTest
@AutoConfigureMockMvc
public class SpotifyApiConnectionTests extends BaseTest {

    //#region Members

    @Autowired
    SpotifyApiConnection spotifyApiConnection;

    @MockitoBean
    ValkeyClient mockValkeyClient;

    private UUID successfulSessionId = UUID.fromString("6356C38E-FA4D-4EBC-8BDB-3628250A998A");

    final static int ELEVEN_DAYS_SECONDS = 950400;

    //#endregion

    //#region Static Methods

    private static SpotifyTokenInfo createValidTokenInfo(boolean expired) {
        
        SpotifyTokenInfo tokenInfo = new SpotifyTokenInfo();
        tokenInfo.setAccessToken("Access Token");
        tokenInfo.setRefreshToken("Refresh Token");
        tokenInfo.setTokenGeneratedAt(LocalDateTime.now(ZoneId.of("UTC")));
        tokenInfo.setExpiresIn(!expired ? ELEVEN_DAYS_SECONDS : 0);

        return tokenInfo;
    }

    private void mockSpotifyApiAndValkey(SpotifyTokenInfo tokenInfo) throws Exception {
        SpotifyApi mockApi = mock(SpotifyApi.class);

        when(mockValkeyClient.getSpotifyAPITokenAsync(successfulSessionId))
            .thenReturn(CompletableFuture.completedFuture(Pair.of(successfulSessionId, tokenInfo)));

        when(mockSpotifyApiBuilder.build()).thenReturn(mockApi);
        
    }

    //#endregion

    //#region Tests

    /**
     * Test plan
     * 1. Create happy and sad path tests for createApiClient(String sessionId)
     * 2. Create happy and sad path tests for buildSpotifyClientFromTokens
     * 3. Create happy and sad path tests for refreshSpotifyTokenAsync
     */

    /**
     * This method tests that invalid input is handled when attempting to create the SpotifyApiClient.
     * @throws Exception
     */
    @Test
    public void shouldNotCreateApiClientFromInvalidInput() throws Exception {
        String emptySessionId = "";
        String nullSessionId = null;

        InvalidSessionIdException exception = assertThrows(InvalidSessionIdException.class, () -> {
            spotifyApiConnection.createApiClientAsync(emptySessionId);
        });

        assertTrue(exception.getMessage().contains("Invalid Session Id"));

        exception = assertThrows(InvalidSessionIdException.class, () -> {
            spotifyApiConnection.createApiClientAsync(nullSessionId);
        });

        assertTrue(exception.getMessage().contains("Invalid Session Id"));
    }

    /**
     * This method tests the scenario when invalid token info is passed to build the spotify client
     * @throws Exception
     */
    @Test
    public void shouldNotBuildSpotifyClientBecauseOfInvalidTokens() throws Exception {
        when(mockValkeyClient.getSpotifyAPITokenAsync(successfulSessionId))
            .thenReturn(CompletableFuture.completedFuture(null));

        CompletionException exception = assertThrows(CompletionException.class, () -> {
            spotifyApiConnection.createApiClientAsync(successfulSessionId.toString()).join();
        });

        assertInstanceOf(SpotifyClientBuildException.class, exception.getCause());
        assertTrue(exception.getMessage().contains("Invalid token information for Spotify Client"));
    }

    /**
     * This method tests a successful build of the Spotify Client from tokens without a referesh
     * @throws Exception
     */
    @Test
    public void shouldBuildSpotifyClientFromTokensWithoutRefresh() throws Exception {        
        SpotifyTokenInfo tokenInfo = createValidTokenInfo(false);

        var sessionTokenPair = Pair.of(successfulSessionId, tokenInfo);
        when(mockValkeyClient.getSpotifyAPITokenAsync(successfulSessionId))
            .thenReturn(CompletableFuture.completedFuture(sessionTokenPair));

        SpotifyApi api = SpotifyApi
            .builder()
            .setAccessToken(tokenInfo.getAccessToken())
            .setRefreshToken(tokenInfo.getRefreshToken())
            .build();
        SpotifyApi spyApi = spy(api);
        when(mockSpotifyApiBuilder.build()).thenReturn(spyApi);

        SpotifyApi returnedSpotifyClient = 
            spotifyApiConnection.createApiClientAsync(successfulSessionId.toString()).join();

        //Verify that a token refresh did not happen
        verify(mockValkeyClient, times(0))
            .upsertSpotifyAPITokenAsync(any(UUID.class), any(SpotifyTokenInfo.class));

        assertTrue(returnedSpotifyClient.getAccessToken() == tokenInfo.getAccessToken() &&
            returnedSpotifyClient.getRefreshToken() == tokenInfo.getRefreshToken());

    }

    /**
     * This method tests a successful creation of a Spotify Client with a token refresh
     * @throws Exception
     */
    @Test
    public void shouldBuildSpotifyClientFromTokensWithRefresh() throws Exception {

        SpotifyTokenInfo oldTokenInfo = createValidTokenInfo(true);
        SpotifyTokenInfo newTokenInfo = createValidTokenInfo(false);
        newTokenInfo.setAccessToken("New Access Token");

        var sessionTokenPair = Pair.of(successfulSessionId, oldTokenInfo);
        when(mockValkeyClient.getSpotifyAPITokenAsync(successfulSessionId))
            .thenReturn(CompletableFuture.completedFuture(sessionTokenPair));

        SpotifyApi api = SpotifyApi
            .builder()
            .setAccessToken(oldTokenInfo.getAccessToken())
            .setRefreshToken(oldTokenInfo.getRefreshToken())
            .build();

        when(mockSpotifyApiBuilder.build()).thenReturn(api);
        
        AuthorizationCodeCredentials creds = mock(AuthorizationCodeCredentials.class);

        try (MockedConstruction<SpotifyApiGateway> mockGateway = mockConstruction(SpotifyApiGateway.class,
            (mock, context) -> {
                when(mock.refreshAuthorizationTokensAsync(oldTokenInfo.getRefreshToken()))
                    .thenReturn(CompletableFuture.completedFuture(creds));
            }
        )) {

            when(creds.getExpiresIn()).thenReturn(ELEVEN_DAYS_SECONDS);
            when(creds.getAccessToken()).thenReturn(newTokenInfo.getAccessToken());
            
            when(mockValkeyClient.upsertSpotifyAPITokenAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(true));

            var returnedSpotifyClient = spotifyApiConnection.createApiClientAsync(successfulSessionId.toString()).join();

            //Finally, verify that the updated tokens are updated
            assertTrue(returnedSpotifyClient.getAccessToken() == newTokenInfo.getAccessToken());
        }
    }

    /**
     * This method ensures that a JsonProcessingException is handled correctly when refreshing the tokens
     * @throws Exception
     */
    @Test
    public void shouldHandleJsonProcessingExceptionWhenRefreshingToken() throws Exception {
        var tokenInfo = createValidTokenInfo(true);
        mockSpotifyApiAndValkey(tokenInfo);

        var mockCreds = mock(AuthorizationCodeCredentials.class);

        try (MockedConstruction<SpotifyApiGateway> mockGateway = mockConstruction(SpotifyApiGateway.class,
            (mock, context) -> {
                when(mock.refreshAuthorizationTokensAsync(any()))
                    .thenReturn(CompletableFuture.completedFuture(mockCreds));
            }
        )) {

            var exception = new JsonGenerationException("Serialization failed", mock(JsonGenerator.class));
            when(mockValkeyClient.upsertSpotifyAPITokenAsync(successfulSessionId, tokenInfo))
                .thenThrow(exception);

            var completionException = assertThrows(CompletionException.class, () -> {
                spotifyApiConnection.createApiClientAsync(successfulSessionId.toString()).join();
            });

            assertTrue(completionException.getCause().getMessage().contains("Serialization failed"));
            assertTrue(completionException.getCause().getMessage().contains(successfulSessionId.toString()));
            assertInstanceOf(ValkeyOperationException.class, completionException.getCause());
            assertInstanceOf(JsonProcessingException.class, completionException.getCause().getCause());
        }
    }

    /**
     * This test handles the scenario where there upsertSpotifyAPITokenAsync returns false
     * @throws Exception
     */
    @Test
    public void shouldHandleFailureToUpdateRefreshToken() throws Exception {
        var tokenInfo = createValidTokenInfo(true);
        mockSpotifyApiAndValkey(tokenInfo);

        var mockCreds = mock(AuthorizationCodeCredentials.class);

        try (MockedConstruction<SpotifyApiGateway> mockGateway = mockConstruction(SpotifyApiGateway.class,
            (mock, context) -> {
                when(mock.refreshAuthorizationTokensAsync(any()))
                    .thenReturn(CompletableFuture.completedFuture(mockCreds));
            }
        )) {

            when(mockValkeyClient.upsertSpotifyAPITokenAsync(successfulSessionId, tokenInfo))
                .thenReturn(CompletableFuture.completedFuture(false));

            var completionException = assertThrows(CompletionException.class, () -> {
                spotifyApiConnection.createApiClientAsync(successfulSessionId.toString()).join();
            });

            assertTrue(completionException.getCause().getMessage().contains("Valkey returned an unsuccessful status"));
            assertTrue(completionException.getCause().getMessage().contains(successfulSessionId.toString()));
            assertTrue(completionException.getCause().getMessage().contains(successfulSessionId.toString()));
            assertInstanceOf(ValkeyOperationException.class, completionException.getCause());
        }

    }

    //#endregion

}
