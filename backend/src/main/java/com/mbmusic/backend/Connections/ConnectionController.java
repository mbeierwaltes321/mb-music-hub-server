package com.mbmusic.backend.Connections;

import java.net.URI;
import java.time.LocalDateTime;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mbmusic.backend.Connections.Models.SpotifyLoginAuth;
import com.mbmusic.backend.Connections.Models.SpotifyTokenResponse;

import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

//This controller is responsible for handling any requests related to API connections
@RestController
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
                    .scope("user-library-read")
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
    public ResponseEntity<SpotifyTokenResponse> spotifyAuthToken(String code, String state) {
        
        //First create the response token
        SpotifyTokenResponse tokenResponse = new SpotifyTokenResponse();

        if (code.isBlank() || code == null) {
            //Blank or null code, return 500
            return ResponseEntity.internalServerError().build();
        }

        //Create an authorization code request object for retrieving the access/refresh tokens
        final AuthorizationCodeRequest request = spotifyConnection.getApiClient().authorizationCode(code).build();

        //Grab the credentails
        try {
            // Attempt to obtain the credentails
            final AuthorizationCodeCredentials authorizationCodeCredentials = request.execute();

            //Populate the return varaible
            tokenResponse.setAccessToken(authorizationCodeCredentials.getAccessToken());
            tokenResponse.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            tokenResponse.setExpiresIn(authorizationCodeCredentials.getExpiresIn());
            tokenResponse.setTokenGeneratedAt(LocalDateTime.now());
            tokenResponse.setState(state);

        } catch (Exception e) {
            // There was an error setting obtaining the credentails, send 500 error
            return ResponseEntity.internalServerError().build();
        }

        //TODO - The response should be a confirmation page that will return the tokenResponse to the frontend application

        //Return the response
        return new ResponseEntity<SpotifyTokenResponse>(tokenResponse, HttpStatusCode.valueOf(HttpStatus.SC_OK));

    }


    //#endregion
    
}
