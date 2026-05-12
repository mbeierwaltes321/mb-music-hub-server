package com.mbmusic.hubserver.Connections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import glide.api.models.GlideString;
import glide.api.models.commands.SetOptions;

@SpringBootTest
@AutoConfigureMockMvc
public class ValkeyClientTests extends BaseTest {

    @Autowired
    ValkeyClient valkeyClient;

    final private UUID successfulUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    final private GlideString successfulSessionKey = GlideString.gs("sessionID:" + successfulUuid.toString());

    final private GlideString serializedTestToken = GlideString.gs("{\"accessToken\":\"BQB7_EXAMPLE_TOKEN_STRING_12345\",\"refreshToken\":\"0A1B2C3D4E5F6G7H8I9J0K\",\"expiresIn\":3600,\"tokenGeneratedAt\":\"2026-04-21T20:28:06\"}");

    private SpotifyTokenInfo getExpectedTokenInfo() throws Exception {
        return mapper.readValue(serializedTestToken.getString(), SpotifyTokenInfo.class);
    }

    private void mockGlideTokenRetrieval() {
        when(mockGlideClient.get(successfulSessionKey))
            .thenReturn(CompletableFuture.completedFuture(serializedTestToken));
    }

    private void mockGlideUpsert(String response) {
        when(mockGlideClient.set(eq(successfulSessionKey), eq(serializedTestToken), any(SetOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(response));
    }

    /**
     * This method tests a successful retrieval of Spotify API tokens
     * @throws Exception
     */
    @Test
    public void shouldGetSpotifyApiToken() throws Exception {

        SpotifyTokenInfo expectedTokenInfo = getExpectedTokenInfo();
        mockGlideTokenRetrieval();
        
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

    /**
     * This method tests that an invalid mapping throws the proper exception
     * @throws Exception
     */
    @Test
    public void shouldThrowJsonMappingExceptionWhenRetrievingToken() throws Exception {

        //Replace the date with an integer to create an invalid mapping
        GlideString invalidJson = GlideString.gs(serializedTestToken.toString().replace("\"2026-04-21T20:28:06\"", "11"));

        when(mockGlideClient.get(successfulSessionKey))
            .thenReturn(CompletableFuture.completedFuture(invalidJson));

        var completionException = assertThrows(CompletionException.class, () -> {
            valkeyClient.getSpotifyAPITokenAsync(successfulUuid).join();
        });

        assertInstanceOf(JsonMappingException.class, completionException.getCause());
    }

    /**
     * This method tests that invalid json throws a proper exception
     * @throws Exception
     */
    @Test
    public void shouldThrowJsonParsingExceptionWhenRetrievingToken() throws Exception {

        GlideString invalidJson = GlideString.gs("sdjflha38*@H# HHDFSKLJD HFLDSJbfwe7h2uh3");

        when(mockGlideClient.get(successfulSessionKey))
            .thenReturn(CompletableFuture.completedFuture(invalidJson));

        var completionException = assertThrows(CompletionException.class, () -> {
            valkeyClient.getSpotifyAPITokenAsync(successfulUuid).join();
        });

        assertInstanceOf(JsonParseException.class, completionException.getCause());
    }

    /**
     * This method mocks a successful upsert of the spotify api token
     * @throws Exception
     */
    @Test
    public void shouldUpsertSpotifyAPIToken() throws Exception {

        mockGlideUpsert("OK");

        assertEquals(valkeyClient.upsertSpotifyAPITokenAsync(successfulUuid, getExpectedTokenInfo()).join(), true);
    }

    /**
     * This method tests the scenario where the upsert returned a null response
     * @throws Exception
     */
    @Test
    public void upsertTokenShouldReturnFalseOnNullResponse() throws Exception {
        
        mockGlideUpsert(null);
        
        assertFalse(valkeyClient.upsertSpotifyAPITokenAsync(successfulUuid, getExpectedTokenInfo()).join());
    }

    /**
     * This method tests the scenario where the token upsert returned a non-OK value
     * @throws Exception
     */
    @Test
    public void upsertTokenShouldReturnFalseInvalidResponse() throws Exception {
        
        mockGlideUpsert("Oops!");
        
        assertFalse(valkeyClient.upsertSpotifyAPITokenAsync(successfulUuid, getExpectedTokenInfo()).join());
    }

    /**
     * This method tests that an invalid session exceptions is thrown when attempting to upsert an invalid session id
     * @throws Exception
     */
    @Test
    public void shouldGetInvalidSessionWhenUpsertingSpotifyAPIToken() throws Exception {
        UUID nullInvalidUuid = null;
        UUID nilInvalidUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        var exceptionFromNull = assertThrows(InvalidSessionIdException.class, () -> {
            valkeyClient.upsertSpotifyAPITokenAsync(nullInvalidUuid, getExpectedTokenInfo()).join();
        });

        assertTrue(exceptionFromNull.getMessage().contains("Provided Session ID Invalid"));

        var exceptionFromNil = assertThrows(InvalidSessionIdException.class, () -> {
            valkeyClient.upsertSpotifyAPITokenAsync(nilInvalidUuid, getExpectedTokenInfo()).join();
        });

        assertTrue(exceptionFromNil.getMessage().contains("Provided Session ID Invalid"));
    }

    /**
     * This method tests for a successful deletion of an existing spotify session id
     */
    @Test
    public void shouldDeleteSpotifyApiToken() throws InvalidSessionIdException {
        when(mockGlideClient.del((GlideString[])any())).thenReturn(CompletableFuture.completedFuture(1L));
        assertTrue(valkeyClient.removeTokenAsync(successfulUuid).join());
    }

    /**
     * This method tests that an invalid session id exception is thrown when passing an invalid session id to 
     */
    @Test
    public void shouldHandleInvalidSessionIdWhenDeletingTokenInfo() {
        var exception = assertThrows(InvalidSessionIdException.class, () -> {
            UUID nilUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
            valkeyClient.removeTokenAsync(nilUuid);
        });

        assertTrue(exception.getMessage().contains("Invalid Session ID"));
    }

    /**
     * This method ensures that removeTokenAsync returns false when no tokens are deleted
     * @throws InvalidSessionIdException
     */
    @Test
    public void shouldReturnFalseWhenZeroTokenRecordsAreDeleted() throws InvalidSessionIdException {
        when(mockGlideClient.del((GlideString[])any())).thenReturn(CompletableFuture.completedFuture(0L));
        assertFalse(valkeyClient.removeTokenAsync(successfulUuid).join());
    }

}
