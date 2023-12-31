package com.mbmusic.backend.Connections;

import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.ui.Model;

import com.mbmusic.backend.Connections.Models.SpotifyLoginAuth;

import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

//This controller is responsible for handling any requests related to API connections
@Controller
@RequestMapping("conn")
public class ConnectionController {

    @Autowired
    SpotifyApiConnection spotifyConnection;

    //#region " Methods "
    //This method generates the login URI for authenticating the user into Spotify
    @PostMapping("/spotifylogin")
    public ResponseEntity<SpotifyLoginAuth> postSpotifyLogin() {

        //First create the return object
        SpotifyLoginAuth loginAuth = new SpotifyLoginAuth();
     
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
        request = spotifyConnection.getApiClient().authorizationCodeUri()
                    .state(stateSb.toString())
                    .response_type("code")
                    .scope("user-library-read playlist-read-private")
                    .build();

        final URI authUri = request.execute();

        //Check that the url returned successfully
        if (authUri == null) {
            return ResponseEntity.internalServerError().build();
        }

        //Populate the return object
        loginAuth.setState(stateSb.toString());
        loginAuth.setLoginUrl(authUri.toString());

        return new ResponseEntity<SpotifyLoginAuth>(loginAuth, HttpStatusCode.valueOf(HttpStatus.SC_OK));

    }

    //This method is called by the Spotify API
    //NOTE: When the state is returned to the client, you
    //must verify that the state in the browser matches the state passed here
    @GetMapping("/redirect")
    public String generateSpotifyAuthToken(@RequestParam(name="code") String code, @RequestParam(name="state") String state, @RequestParam(name="error", required= false) String error, Model model) {
        
        //First create boolean variable which determines the success of the token request
        boolean authTokenSuccessful = true;

        //Declare the URL object used to redirect to the frontend application
        URL redirectUrl = null;

        //First check if there isn error
        if (error != null && !error.isBlank()) {
            authTokenSuccessful = false;
            System.out.println(error);
        }
        //Check for if the code returned is either blank or null
        else if (code.isBlank() || code == null) {
            authTokenSuccessful = false;
        } else {
            //Create an authorization code request object for retrieving the access/refresh tokens
            final AuthorizationCodeRequest request = spotifyConnection.getApiClient().authorizationCode(code).build();

            //Grab the credentails
            try {
                // Attempt to obtain the credentails
                final AuthorizationCodeCredentials authorizationCodeCredentials = request.execute();
                
                //Create time zone obbject to get current time in UTC
                ZoneId UTC = ZoneId.of("UTC");

                //Get the current time in UTC
                LocalDateTime generatedTimeUTC = LocalDateTime.now(UTC);

                //Build frontend redirect url
                redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/api/conn/testDisplayToken")
                                    .queryParam("token", authorizationCodeCredentials.getAccessToken())
                                    .queryParam("refresh", authorizationCodeCredentials.getRefreshToken())
                                    .queryParam("state", state)
                                    .queryParam("generatedAt", generatedTimeUTC.toString())
                                    .build()
                                    .toUri()
                                    .toURL();

            } catch (Exception e) {
                // There was an error setting obtaining the credentails, send 500 error
                authTokenSuccessful = false;
            }

        }

        //Get the final url
        String urlString = "";
        if(redirectUrl != null) {
            urlString = redirectUrl.toString();
        } else {
            authTokenSuccessful = false;
        }
    
        //Return the response
        model.addAttribute("authTokenSuccess", authTokenSuccessful);
        model.addAttribute("redirectUrl", urlString);
        return "SpotifyTokenGenerated";
    }

    //This method is used to display the spotify token response data without a need for the frontend application
    @GetMapping("testDisplayToken")
    public String testDisplayTokens(@RequestParam(name="token") String token, @RequestParam(name="refresh") String refresh, @RequestParam(name="state") String state, @RequestParam(name="generatedAt")String generatedAt, Model model) {

        model.addAttribute("token", token);
        model.addAttribute("refresh", refresh);
        model.addAttribute("state", state);
        model.addAttribute("generatedAt", generatedAt);

        return "TokenDisplay";

    }

    //#endregion
    
}
