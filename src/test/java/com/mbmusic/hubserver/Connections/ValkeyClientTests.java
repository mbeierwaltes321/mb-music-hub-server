package com.mbmusic.hubserver.Connections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import glide.api.models.GlideString;

@SpringBootTest
@AutoConfigureMockMvc
public class ValkeyClientTests extends BaseTest {

    @Autowired
    ValkeyClient valkeyClient;

    final private UUID successfulUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    final private GlideString successfulSessionKey = GlideString.gs("sessionID:" + successfulUuid.toString());

    final private GlideString serializedTestToken = GlideString.gs("{\"accessToken\":\"BQB7_EXAMPLE_TOKEN_STRING_12345\",\"refreshToken\":\"0A1B2C3D4E5F6G7H8I9J0K\",\"expiresIn\":3600,\"tokenGeneratedAt\":\"2026-04-21T20:28:06\"}");

    /**
     * Test plan
     * 1. Create happy and sad path tests for getSpotifyAPITokenAsync
     * 2. Create happy and sad path tests for upsertSpotifyAPITokenAsync
     * 3. Create happy and sad path tests for removeTokenAsync
     */

    /**
     * This method tests a successful retrieval of Spotify API tokens
     * @throws Exception
     */
    @Test
    public void shouldGetSpotifyApiToken() throws Exception {

        SpotifyTokenInfo expectedTokenInfo = mapper.readValue(serializedTestToken.getString(), SpotifyTokenInfo.class);
        when(mockGlideClient.get(successfulSessionKey))
            .thenReturn(CompletableFuture.completedFuture(serializedTestToken));
        
        var tokenPair = valkeyClient.getSpotifyAPITokenAsync(successfulUuid).join();

        assertEquals(tokenPair.getFirst(), successfulUuid);
        assertTrue(() -> {
            var returnedToken = tokenPair.getSecond();
            return expectedTokenInfo.getAccessToken().equals(returnedToken.getAccessToken()) &&
                expectedTokenInfo.getExpiresIn() == returnedToken.getExpiresIn() &&
                expectedTokenInfo.getRefreshToken().equals(returnedToken.getRefreshToken()) &&
                expectedTokenInfo.getTokenGeneratedAt().isEqual(returnedToken.getTokenGeneratedAt());
        });
    }

    /**
     * This method tests both scenarios in getSpotifyAPITokenAsync involving an invalid session id
     */
    @Test
    public void shouldGetInvalidSessionIdWhenRetrievingToken() {
        UUID nullInvalidUuid = null;
        UUID nilInvalidUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        var exceptionFromNull = assertThrows(InvalidSessionIdException.class, () -> {
            valkeyClient.getSpotifyAPITokenAsync(nullInvalidUuid);
        });

        var exceptionFromNil = assertThrows(InvalidSessionIdException.class, () -> {
            valkeyClient.getSpotifyAPITokenAsync(nilInvalidUuid);
        });

        assertTrue(() -> {
            return exceptionFromNull.getMessage().contains("Provided Session ID Invalid") &&
                exceptionFromNil.getMessage().contains("Provided Session ID Invalid");
        });
    }

}
