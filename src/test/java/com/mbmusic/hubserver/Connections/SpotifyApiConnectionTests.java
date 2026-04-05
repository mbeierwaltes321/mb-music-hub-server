package com.mbmusic.hubserver.Connections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

@SpringBootTest
@AutoConfigureMockMvc
public class SpotifyApiConnectionTests extends BaseTest {

    //#region Members

    @MockitoBean
    SpotifyApiConnection mockSpotifyApiConnection;

    @MockitoBean
    ValkeyClient mockValkeyClient;

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

    @Test
    public void shouldNotCreateApiClientFromInvalidInput() throws Exception {
        String emptySessionId = "";
        String nullSessionId = null;

        when(mockSpotifyApiConnection.createApiClient(any())).thenCallRealMethod();

        InvalidSessionIdException exception = assertThrows(InvalidSessionIdException.class, () -> {
            mockSpotifyApiConnection.createApiClient(emptySessionId);
        });

        assertTrue(exception.getMessage().contains("Invalid Session Id"));

        exception = assertThrows(InvalidSessionIdException.class, () -> {
            mockSpotifyApiConnection.createApiClient(nullSessionId);
        });

        assertTrue(exception.getMessage().contains("Invalid Session Id"));

    }

    //#endregion

}
