package com.mbmusic.hubserver.Connections;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import com.mbmusic.hubserver.Common.Utilities.TimeUtils;
import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Connections.Exceptions.SpotifyAuthorizationException;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

//This controller is responsible for handling any requests related to API connections
@RestController
@RequestMapping("conn")
public class ConnectionController {

    //#region Members
    private SpotifyApiConnection spotifyConnection;

    @Autowired 
    private ValkeyClient valkeyClient;

    //#endregion

    //#region Constructor

    public ConnectionController(SpotifyApiConnection spotifyConnection) {
        this.spotifyConnection = spotifyConnection;
    }

    //#endregion

    //#region Methods

    /**
     * This method builds and retrieves a Spotify Gateway object responsible for performing authentication and authorization
     */
    private SpotifyApiGateway getSpotifyApiGateway() {
        //Retrieve the token and prepare the Spotify API Client
        return new SpotifyApiGateway(spotifyConnection.createEmptyApiClient());
    }

    /**
     * This method generates the login URI for authenticating the user into Spotify
     * @return A {@link RedirectView} that redirects to the authentication window for the user on successful login
     * @throws Exception when something goes wrong with the initial authentication
     */
    @PostMapping("/spotifylogin")
    public RedirectView postSpotifyLogin() {
     
        // Generate a state
        // choose a Character random from this String 
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        + "0123456789"
        + "abcdefghijklmnopqrstuvxyz"; 

        // Create StringBuffer size of AlphaNumericString 
        StringBuilder stateSb = new StringBuilder(16); 

        for (int i = 0; i < 16; i++) { 

            // generate a random number between 
            // 0 to AlphaNumericString variable length 
            int index 
            = (int)(AlphaNumericString.length() 
            * Math.random()); 

            // add Character one by one in end of sb 
            stateSb.append(AlphaNumericString 
            .charAt(index)); 
        }  

        SpotifyApiGateway spotifyGateway = getSpotifyApiGateway();
        final URI authUri = spotifyGateway.createAuthorizationURI(stateSb.toString());

        String responseQuery = authUri.getQuery();

        //Create hash map for organizing the query parms
        HashMap<String, String> parmInfo = new HashMap<String, String>();
        for (String queryParm : responseQuery.split("&")) {
            String[] parmValues = queryParm.split("=");
            parmInfo.put(parmValues[0], parmValues[1]);
        }

        if (parmInfo.get("error") != null) {
            throw new SpotifyAuthorizationException("Access denied. Details: " + parmInfo.get("error"));
        }

        //Validate that the state is the same
        String returnedState = parmInfo.get("state");
        if (returnedState == null || !returnedState.equals(stateSb.toString())) {
            throw new SpotifyAuthorizationException("Access denied: State did not match");
        }

        return new RedirectView(authUri.toString());
    }

    /**
     * This method is called by the Spotify API. It is used to generate an authorization and referesh token
     * @note When the state is returned to the client, you must verify that the state in the browser matches the state passed here
     * @param code The code returned from the Spotify API used to authenticate the user
     * @param state The state-specific code used to identify the user and session
     * @throws IOException IO Exceptions performed while obtaining the authorization code
     * @throws SpotifyWebApiException Exceptions specific to the Spotify API while retrieving the authorization code
     * @throws ParseException Parsing exception when retrieving the Spotify API codes
     * @throws InvalidSessionIdException Invalid session id when upserting the new session id to Valkey
     */
    @GetMapping("/redirect")
    public CompletableFuture<Void> generateSpotifyAuthToken(@RequestParam(name="code") String code, 
            @RequestParam(name="state") String state, HttpServletResponse response ) 
                throws ParseException, SpotifyWebApiException, IOException, InvalidSessionIdException {

        SpotifyApiGateway spotifyGateway = getSpotifyApiGateway();
                
        final AuthorizationCodeCredentials authorizationCodeCredentials = 
            spotifyGateway.getAuthorizationCodeCredentials(code);
        
        //Get the current time in UTC
        ZoneId UTC = ZoneId.of("UTC");
        LocalDateTime generatedTimeUTC = LocalDateTime.now(UTC);

        SpotifyTokenInfo newTokenInfo = new SpotifyTokenInfo();
        newTokenInfo.setAccessToken(authorizationCodeCredentials.getAccessToken());
        newTokenInfo.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
        newTokenInfo.setTokenGeneratedAt(generatedTimeUTC);
        newTokenInfo.setExpiresIn(authorizationCodeCredentials.getExpiresIn());

        //Create the session id for the user, and add the session cookie
        //So it looks like the cookie requirements are as follows during development:
        //1. For Safari: setSecure should be false; cookie cannot have __Secure if setSecure is false. Otherwise it will not show
        //2. For Edge: setSecure will work, but it doesn't like that it's being combined with SameSite: none
        //3. We need to find the best way to handle this
        UUID newSessionId = UUID.randomUUID();
        Cookie cookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, newSessionId.toString());
        cookie.setSecure(false);
        cookie.setDomain("127.0.0.1");  //NOTE: For testing, you must use 127.0.0.1 instead of "localhost" to match what Spotify requests
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge(TimeUtils.WEEK_SECONDS); //TODO - Configure this with "Remember Me" at one point

        response.addCookie(cookie);

        //Declare the URL object used to redirect to the frontend application
        //TODO - Make this an environment variable?
        URL redirectUrl = UriComponentsBuilder.fromUriString("http://127.0.0.1:5173/")
            .build()
            .toUri()
            .toURL();

        return valkeyClient.upsertSpotifyAPITokenAsync(newSessionId, newTokenInfo)
            .thenAccept(inserted -> {
                try {
                    if (!inserted){
                        response.sendRedirect(redirectUrl.toString() + "/error");
                    }
                    response.sendRedirect(redirectUrl.toString());
                } catch (Exception e) {
                    throw new RuntimeException("Error redirecting to the front end");
                }
            });
    }

    //#endregion
    
}
