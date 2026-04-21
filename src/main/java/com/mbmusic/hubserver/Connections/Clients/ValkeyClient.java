package com.mbmusic.hubserver.Connections.Clients;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mbmusic.hubserver.Common.Utilities.TimeUtils;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import glide.api.GlideClient;
import glide.api.models.GlideString;
import glide.api.models.commands.SetOptions;
import glide.api.models.commands.SetOptions.Expiry;

@Component
public class ValkeyClient {

    private GlideClient valkeyGlide;

    private final String SESSION_PREFIX = "sessionID:";

    @Autowired
    public ValkeyClient(GlideClient valkeyGlide) {
        this.valkeyGlide = valkeyGlide;
    }

    /**
     * This method attempts to retrieve the Spotify API Token information given the generated session id
     * @param sessionID The ID of the session generated at authentication
     * @return The Spotify Token Information needed for requests.
     * @throws InvalidSessionIdException Thrown if the provided Session ID is invalid
     */
    public CompletableFuture<Pair<UUID, SpotifyTokenInfo>> getSpotifyAPITokenAsync(UUID sessionID)
        throws InvalidSessionIdException {

        //Validate the incoming session id
        if (sessionID == null || sessionID.equals(UUID.fromString("00000000-0000-0000-0000-000000000000")))
            throw new InvalidSessionIdException("Provided Session ID Invalid");

        //Retrieve the Spotify API Token
        CompletableFuture<Pair<UUID, SpotifyTokenInfo>> tokenRetrievalFuture =
            this.valkeyGlide.get(GlideString.gs(SESSION_PREFIX + sessionID.toString()))
        .thenApply((GlideString serializedToken) -> {

            SpotifyTokenInfo tokenInfo;
            try {
                //Build the spotify token information
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                tokenInfo = mapper.readValue(serializedToken.getString(), SpotifyTokenInfo.class);
            } catch(JsonMappingException e) {
                throw new CompletionException(e);
            } catch(JsonProcessingException e) {
                throw new CompletionException(e);
            }

            return Pair.of(sessionID, tokenInfo);
        });

        return tokenRetrievalFuture;
    }


    /**
     * This method inserts a session ID into the Spotiy Token Cache
     * @param sessionID The session ID that will serve as the key for the valkey database
     * @param tokenInfo The token informaiton for Spotify
     * @return True on success. False otherwise.
     * @throws JsonProcessingException
     * @throws InvalidSessionIdException
     */
    public CompletableFuture<Boolean> upsertSpotifyAPITokenAsync(UUID sessionID, SpotifyTokenInfo tokenInfo)
        throws JsonProcessingException, InvalidSessionIdException {

        //Validate the input parameters
        if (sessionID == null || sessionID.equals(UUID.fromString("00000000-0000-0000-0000-000000000000")) || tokenInfo == null) {
            //No session id. Failed
            throw new InvalidSessionIdException("Provided Session ID Invalid");
        }

        //Serialize the token information
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        String spotifyTokenJson = mapper.writeValueAsString(tokenInfo);

        //TODO - This may or may not need to change depending on any "remember me" functionality
        SetOptions setOptions = SetOptions.builder()
            .expiry(Expiry.Seconds((long)TimeUtils.WEEK_SECONDS))
            .build();

        //Add the token to Valkey
        return this.valkeyGlide.set(GlideString.gs(SESSION_PREFIX + sessionID.toString()), GlideString.gs(spotifyTokenJson), setOptions)
            .thenApply((String setResponse) -> {
                if (setResponse == null || setResponse.length() == 0 || setResponse != "OK") {
                    //Error setting. Failed;
                    return false;
                }

                return true;
            });
    }


    /**
     * This method attempts to delete the token information associated with the provided session id
     * @param sessionId The ID of the session to delete
     * @return A {@link CompletableFuture} with a boolean value indicating success
     */
    public CompletableFuture<Boolean> removeTokenAsync(UUID sessionId) {

        if (sessionId == null || sessionId.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
            return CompletableFuture.completedFuture(false);
        }

        GlideString[] keys = new GlideString[1];
        keys[0] = GlideString.gs(SESSION_PREFIX + sessionId.toString());
    
        return this.valkeyGlide.del(keys)
            .thenApply(numDeleted -> numDeleted.longValue() > 0);

    }


}
