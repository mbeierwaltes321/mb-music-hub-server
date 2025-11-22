package com.mbmusic.hubserver.Playlists;

import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mbmusic.hubserver.Common.Models.ApiResponse;
import com.mbmusic.hubserver.Connections.SpotifyApiConnection;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyItemRequest;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistRequest;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistResponse;

import jakarta.servlet.http.Cookie;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

//This class resembles the controller for accessing playlist data
@RestController
@RequestMapping("playlists")
public class PlaylistController {

    //#region Members
    
    //The client to the spotify API
    private final SpotifyApiConnection spotifyConnection;

    //#endregion

    //#region Constructor
    public PlaylistController(SpotifyApiConnection connection) {
        this.spotifyConnection = connection;
    }

    //#endregion

    //#region Methods

    //#region GET

    //This method gets all of the Spotify playlists created by the current user
    //TODO - Make the cookie name "__Secure-SpotifySessionId" a static string 
    @GetMapping("/spotify-playlists")
    public ResponseEntity<Paging<PlaylistSimplified>> getUserSpotifyPlaylists(@CookieValue(name = "__Secure-SpotifySessionId") Cookie sessionIdCookie, @RequestParam(required = false) Integer offset) throws Exception {

        PlaylistDA da = new PlaylistDA(sessionIdCookie.getValue());
        Paging<PlaylistSimplified> playlists = da.retrieveUserPlaylists(offset);

        //return the response
        return new ResponseEntity<Paging<PlaylistSimplified>>(playlists, HttpStatus.OK);
    }

    //#endregion

    //#region POST

    //This method adds the provided spotify items to the selected playlist
    @PostMapping("/spotify-items")
    public ResponseEntity<Boolean> postSpotifyItems( @CookieValue(name = "__Secure-SpotifySessionId") Cookie sessionIdCookie, @RequestBody(required = true) PostSpotifyItemRequest requestBody) throws Exception {

        PlaylistDA da = new PlaylistDA(sessionIdCookie.getValue());
        
        if (!da.addItemsToPlaylist(requestBody.getPlaylistId(), requestBody.getSpotifyItems())) {
            return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Boolean>(true, HttpStatus.OK);

    }

    @PostMapping("/spotify-playlists")
    public ResponseEntity<ApiResponse<PostSpotifyPlaylistResponse>> postSpotifyPlaylist(
        @RequestBody(required = true) PostSpotifyPlaylistRequest requestBody
    ) throws Exception {

        //First declare the return object and status
        ApiResponse<PostSpotifyPlaylistResponse> response = new ApiResponse<PostSpotifyPlaylistResponse>();
        HttpStatus statusCode;

        //Grab the fields from the request body
        SpotifyTokenInfo authTokens = requestBody.getAuthTokens();
        String playlistName = requestBody.getPlaylistName();
        String playlistDescription = requestBody.getPlaylistDescription();
        List<String> spotifyURIs = requestBody.getSpotifyURIs();
        boolean isPublic = requestBody.getIsPublic();
            
        //Now obtain the spotify client from the connection
        SpotifyApi spotifyApi = this.spotifyConnection.createApiClient(authTokens);

        //Get the current user's profile
        User currentUser = spotifyApi.getCurrentUsersProfile()
        .build()
        .execute();

        //Now get the user's Spotify ID
        String userId = currentUser.getId();

        //Make sure the userId is valid
        if (userId != null && !userId.isBlank()) {

            //Now create a playlist request
            CreatePlaylistRequest createPlaylist = spotifyApi.createPlaylist(userId, playlistName)
            .description(playlistDescription)
            .public_(isPublic)  //NOTE: the Spotify API is outdated, and you cannot create a private playlist at the moment :(
            .build();

            //Execute the playlist request
            Playlist newPlaylist = createPlaylist.execute();

            //Check that the playlist was created
            if(newPlaylist != null) {

                //Playlist created. Get the id
                final String newPlaylistId = newPlaylist.getId();

                //Now check if there were any spotify items to add to the playlist
                if (spotifyURIs != null && !spotifyURIs.isEmpty()) {

                    //Now create a playlist insert request
                    final AddItemsToPlaylistRequest addItems = spotifyApi.addItemsToPlaylist(newPlaylistId, spotifyURIs.toArray(new String[0]))
                    .build();
                    
                    //Insert the items
                    addItems.execute();
                }

                //Create the return content
                PostSpotifyPlaylistResponse content = new PostSpotifyPlaylistResponse(true, newPlaylistId);

                //Populate the return object
                response.setResponseContent(content);
                response.setSpotifyTokenInfo(authTokens);
                statusCode = HttpStatus.CREATED;

            } else {
                //The playlist wasn't created, return an error
                PostSpotifyPlaylistResponse errorResponse = new PostSpotifyPlaylistResponse(false, "", "Unable to create a Spotify Playlist");
                
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                response.setResponseContent(errorResponse);
            }

        } else {
            //The user is not valid. Return error
            PostSpotifyPlaylistResponse errorResponse = new PostSpotifyPlaylistResponse(false, "", "User ID was invalid when getting user's profile");
            
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            response.setResponseContent(errorResponse);
        }

        return new ResponseEntity<ApiResponse<PostSpotifyPlaylistResponse>>(response, statusCode);

    }

    //#endregion

    //#endregion
}
