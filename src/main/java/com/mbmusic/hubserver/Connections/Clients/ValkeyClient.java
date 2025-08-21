package com.mbmusic.hubserver.Connections.Clients;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import glide.api.GlideClient;
import glide.api.models.GlideString;
import glide.api.models.commands.SetOptions;
import glide.api.models.commands.SetOptions.Expiry;

@Component
public class ValkeyClient {

    private GlideClient valkeyGlide;

    private final String SESSION_PREFIX = "sessionID:";

    private final long WEEK_SECONDS = 604800;

    @Autowired
    public ValkeyClient(GlideClient valkeyGlide) {
        this.valkeyGlide = valkeyGlide;
    }

    /**
     * This method attempts to retrieve the Spotify API Token information given
     * the generated session id
     * @param sessionID The ID of the session generated at authentication
     * @return The Spotify Token Information needed for requests. 
     * Null if there is no token information, or the record doesn't exist
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    public SpotifyTokenInfo getSpotifyAPIToken(UUID sessionID) throws InterruptedException, ExecutionException, JsonMappingException, JsonProcessingException {

        //Validate the incoming session id
        if (sessionID == null || sessionID.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
            //No session id. Return null
            return null;
        }

        //Retrieve the Spotify API Token
        GlideString serializedToken = this.valkeyGlide.get(GlideString.gs(SESSION_PREFIX + sessionID.toString())).get();

        if (serializedToken == null) {
            //No token retruend. Return null
            return null;
        }

        //Build the spotify token information
        ObjectMapper mapper = new ObjectMapper();
        SpotifyTokenInfo tokenInfo = mapper.readValue(serializedToken.getString(), SpotifyTokenInfo.class);

        return tokenInfo;
    }


    /**
     * This method inserts a session ID into the Spotiy Token Cache
     * @param sessionID The session ID that will serve as the key for the valkey database
     * @param tokenInfo The token informaiton for Spotify
     * @return True on success. False otherwise.
     * @throws JsonProcessingException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public boolean insertSpotifyAPIToken(UUID sessionID, SpotifyTokenInfo tokenInfo) throws JsonProcessingException, InterruptedException, ExecutionException {

        //Validate the input parameters
        if (sessionID == null || sessionID.equals(UUID.fromString("00000000-0000-0000-0000-000000000000")) || tokenInfo == null) {
            //No session id. Failed
            return false;
        }

        //Serialize the token information
        ObjectMapper mapper = new ObjectMapper();
        String spotifyTokenJson = mapper.writeValueAsString(tokenInfo);

        SetOptions setOptions = SetOptions.builder()
                                    .expiry(Expiry.Seconds(WEEK_SECONDS))
                                    .build();

        //Add the token to Valkey
        String setResponse = this.valkeyGlide.set(GlideString.gs(SESSION_PREFIX + sessionID.toString()), GlideString.gs(spotifyTokenJson), setOptions).get();

        if (setResponse == null || setResponse.length() == 0 || setResponse != "OK") {
            //Error setting. Failed;
            return false;
        }

        return true;
    }


}
