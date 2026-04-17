package com.mbmusic.hubserver.Connections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
     * This method tests a successful build of the Spotify Api Client from tokens with a refresh
     * @throws Exception
     */
    @Test
    public void shouldBuildSpotifyClientFromTokensWithRefresh() throws Exception {

        //TODO - Make this test better; add verify statements and such
        SpotifyTokenInfo oldTokenInfo = createValidTokenInfo(true);
        SpotifyTokenInfo newTokenInfo = createValidTokenInfo(false);
        newTokenInfo.setAccessToken("New Access Token");
        newTokenInfo.setRefreshToken("New Refresh Token");

        var sessionTokenPair = Pair.of(successfulSessionId, oldTokenInfo);
        when(mockValkeyClient.getSpotifyAPITokenAsync(successfulSessionId))
            .thenReturn(CompletableFuture.completedFuture(sessionTokenPair));

        SpotifyApi api = SpotifyApi.builder().build();
        api.setAccessToken(oldTokenInfo.getAccessToken());
        api.setRefreshToken(oldTokenInfo.getRefreshToken());

        when(mockSpotifyApiBuilder.build()).thenReturn(api);
        
        AuthorizationCodeCredentials creds = new AuthorizationCodeCredentials.Builder().build();
        AuthorizationCodeCredentials credsSpy = spy(creds);

        try (MockedConstruction<SpotifyApiGateway> mockGateway = mockConstruction(SpotifyApiGateway.class,
            (mock, context) -> {
                when(mock.refreshAuthorizationTokensAsync(oldTokenInfo.getRefreshToken()))
                    .thenReturn(CompletableFuture.completedFuture(credsSpy));
            }
        )) {

            doReturn(ELEVEN_DAYS_SECONDS).when(credsSpy).getExpiresIn();
            doReturn(newTokenInfo.getAccessToken()).when(credsSpy).getAccessToken();
            doReturn(newTokenInfo.getRefreshToken()).when(credsSpy).getRefreshToken();
            
            when(mockValkeyClient.upsertSpotifyAPITokenAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(true));

            var returnedSpotifyClient = spotifyApiConnection.createApiClientAsync(successfulSessionId.toString()).join();

            //Finally, verify that the updated tokens are updated
            assertTrue(returnedSpotifyClient.getAccessToken() == newTokenInfo.getAccessToken() &&
                returnedSpotifyClient.getRefreshToken() == newTokenInfo.getRefreshToken());
        }
        
        
    }

    /**
     * This method tests a successful build of the Spotify Client from tokens without a referesh
     * @throws Exception
     */
    @Test
    public void shouldBuildSpotifyClientFromTokensWithoutRefresh() throws Exception {
        //TODO - Try to improve this test with spites instead of mocks if possible
        SpotifyTokenInfo tokenInfo = createValidTokenInfo(false);

        var sessionTokenPair = Pair.of(successfulSessionId, tokenInfo);
        when(mockValkeyClient.getSpotifyAPITokenAsync(successfulSessionId))
            .thenReturn(CompletableFuture.completedFuture(sessionTokenPair));

        SpotifyApi mockApi = mock(SpotifyApi.class);
        when(mockSpotifyApiBuilder.build()).thenReturn(mockApi);
        when(mockApi.getAccessToken()).thenReturn(tokenInfo.getAccessToken());
        when(mockApi.getRefreshToken()).thenReturn(tokenInfo.getRefreshToken());

        SpotifyApi returnedSpotifyClient = 
            spotifyApiConnection.createApiClientAsync(successfulSessionId.toString()).join();

        //Verify that a token refresh did not happen
        verify(mockApi, times(0)).authorizationCodeRefresh();

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
