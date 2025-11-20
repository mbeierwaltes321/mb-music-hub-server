package com.mbmusic.hubserver.Connections;

import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

//This controller is responsible for handling any requests related to API connections
@Controller
@RequestMapping("conn")
public class ConnectionController {

    @Autowired
    SpotifyApiConnection spotifyConnection;

    @Autowired 
    ValkeyClient valkeyClient;

    //#region " Methods "
    /**
     * This method generates the login URI for authenticating the user into Spotify
     * @return A {@link RedirectView} that redirects to the authentication window for the user on successful login
     * @throws Exception when something goes wrong with the initial authentication
     */
    @PostMapping("/spotifylogin")
    public RedirectView postSpotifyLogin() throws Exception {
     
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

        //Build the authorization request
        AuthorizationCodeUriRequest request;
        request = spotifyConnection.createApiClient()
            .authorizationCodeUri()
            .state(stateSb.toString())
            .response_type("code")
            .scope("user-library-read playlist-read-private playlist-modify-public playlist-modify-private")
            .build();

        final URI authUri = request.execute();

        //Check that the state wasn't modified
        String responseQuery = authUri.getQuery();

        //Create hash map for organizing the query parms
        HashMap<String, String> parmInfo = new HashMap<String, String>();
        for (String queryParm : responseQuery.split("&")) {
            String[] parmValues = queryParm.split("=");
            parmInfo.put(parmValues[0], parmValues[1]);
        }

        //Determine if access was denied
        if (parmInfo.get("error") != null) {
            throw new Exception("Access denied. Details: " + parmInfo.get("error"));
        }

        //Validate that the state is the same
        String returnedState = parmInfo.get("state");
        if (returnedState == null || !returnedState.equals(stateSb.toString())) {
            throw new Exception("Access denied: State did not match");
        }

        return new RedirectView(authUri.toString());

    }

    /**
     * This method is called by the Spotify API. It is used to generate an authorization and referesh token
     * @note When the state is returned to the client, you must verify that the state in the browser matches the state passed here
     * @param code The code returned from the Spotify API used to authenticate the user
     * @param state The state-specific code used to identify the user and session
     */
    @GetMapping("/redirect")
    public CompletableFuture<RedirectView> generateSpotifyAuthToken(@RequestParam(name="code") String code, 
                                           @RequestParam(name="state") String state,
                                           HttpServletResponse response) throws Exception {
    
        //Create an authorization code request object for retrieving the access/refresh tokens
        final AuthorizationCodeRequest request = spotifyConnection.createApiClient().authorizationCode(code).build();

        //Grab the credentails
        // Attempt to obtain the credentails
        final AuthorizationCodeCredentials authorizationCodeCredentials = request.execute();
        
        //Create time zone obbject to get current time in UTC
        ZoneId UTC = ZoneId.of("UTC");

        //Get the current time in UTC
        LocalDateTime generatedTimeUTC = LocalDateTime.now(UTC);

        //Build the token information and add it to token store
        SpotifyTokenInfo newTokenInfo = new SpotifyTokenInfo();
        newTokenInfo.setAccessToken(authorizationCodeCredentials.getAccessToken());
        newTokenInfo.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
        newTokenInfo.setTokenGeneratedAt(generatedTimeUTC);
        newTokenInfo.setExpiresIn(authorizationCodeCredentials.getExpiresIn());

        //Create the session id for the user, and add the session cookie
        UUID newSessionId = UUID.randomUUID();
        Cookie cookie = new Cookie("__Secure-SpotifySessionId", newSessionId.toString());
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/");
        cookie.setAttribute("SameSite", "Strict");
        cookie.setMaxAge(-1); //TODO - Configure this with "Remember Me" at one point

        response.addCookie(cookie);

        //Declare the URL object used to redirect to the frontend application
        URL redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/")
            .build()
            .toUri()
            .toURL();

        //Add the token information to the Valkey database
        CompletableFuture<RedirectView> redirect = valkeyClient.insertSpotifyAPITokenAsync(newSessionId, newTokenInfo)
            .thenApply(inserted -> {
                if (!inserted) {
                    return new RedirectView(redirectUrl.toString() + "/error");
                }

                return new RedirectView(redirectUrl.toString()); 
            });

        return redirect;

    }

    //#endregion
    
}
