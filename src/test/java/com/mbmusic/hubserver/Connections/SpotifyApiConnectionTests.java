package com.mbmusic.hubserver.Connections;

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

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
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

    //#endregion

    //#region Tests

    /**
     * Test plan
     * 1. Create happy and sad path tests for createApiClient(String sessionId)
     * 2. Create happy and sad path tests for buildSpotifyClientFromTokens
     * 3. Create happy and sad path tests for refreshSpotifyTokenAsync
     */

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

    //#endregion

}
