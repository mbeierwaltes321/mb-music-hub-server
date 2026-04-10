package com.mbmusic.hubserver.Connections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
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

@SpringBootTest
@AutoConfigureMockMvc
public class SpotifyApiConnectionTests extends BaseTest {

    //#region Members

    @Autowired
    SpotifyApiConnection spotifyApiConnection;

    @MockitoBean
    ValkeyClient mockValkeyClient;

    private UUID successfulSessionId = UUID.fromString("6356C38E-FA4D-4EBC-8BDB-3628250A998A");

    //#endregion

    //#region Tests

    /**
     * Test plan
     * 1. Create happy and sad path tests for createApiClient(String sessionId)
     * 2. Create happy and sad path tests for buildSpotifyClientFromTokens
     * 3. Create happy and sad path tests for refreshSpotifyTokenAsync
     */

    @Test
    public void shouldCreateApiClient() throws Exception {

        // String mockSessionId = "6356C38E-FA4D-4EBC-8BDB-3628250A998A";
        // UUID mockUuid = UUID.fromString(mockSessionId);

        // SpotifyTokenInfo mockSpotifyTokenInfo = mock(SpotifyTokenInfo.class);

        // var mockUuidAndTokenPair = Pair.of(mockUuid, mockSpotifyTokenInfo);

        // when(mockValkeyClient.getSpotifyAPITokenAsync(any(UUID.class)))
        //     .thenReturn(CompletableFuture.completedFuture(mockUuidAndTokenPair));
        
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

    @Test
    public void shouldBuildSpotifyClientFromTokensWithoutRefresh() throws Exception {
        SpotifyTokenInfo tokenInfo = new SpotifyTokenInfo();
        tokenInfo.setAccessToken("Access Token");
        tokenInfo.setRefreshToken("Refresh Token");
        tokenInfo.setTokenGeneratedAt(LocalDateTime.now(ZoneId.of("UTC")).plusDays(11));

        var sessionTokenPair = Pair.of(successfulSessionId, tokenInfo);
        when(mockValkeyClient.getSpotifyAPITokenAsync(successfulSessionId))
            .thenReturn(CompletableFuture.completedFuture(sessionTokenPair));

        SpotifyApi mockApi = mock(SpotifyApi.class);
        when(mockSpotifyApiBuilder.build()).thenReturn(mockApi);
        when(mockApi.getAccessToken()).thenReturn(tokenInfo.getAccessToken());
        when(mockApi.getRefreshToken()).thenReturn(tokenInfo.getRefreshToken());

        SpotifyApi returnedSpotifyClient = 
            spotifyApiConnection.createApiClientAsync(successfulSessionId.toString()).join();

        assertTrue(returnedSpotifyClient.getAccessToken() == tokenInfo.getAccessToken() &&
            returnedSpotifyClient.getRefreshToken() == tokenInfo.getRefreshToken());
    }

    //#endregion

}
