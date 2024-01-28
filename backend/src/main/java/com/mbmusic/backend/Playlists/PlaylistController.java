package com.mbmusic.backend.Playlists;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mbmusic.backend.Common.Models.ApiResponse;
import com.mbmusic.backend.Connections.SpotifyApiConnection;
import com.mbmusic.backend.Connections.Models.SpotifyTokenInfo;
import com.mbmusic.backend.Playlists.Models.PostSpotifyItemRequest;
import com.mbmusic.backend.Playlists.Models.PostSpotifyPlaylistRequest;
import com.mbmusic.backend.Playlists.Models.PostSpotifyPlaylistResponse;

import se.michaelthelin.spotify.SpotifyApi;
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
@RequestMapping("playlist")
public class PlaylistController {

    //#region " Members "
    
    //The client to the spotify API
    private final SpotifyApiConnection spotifyConnection;

    //#endregion

    //#region " Constructor "
    public PlaylistController(SpotifyApiConnection connection) {
        this.spotifyConnection = connection;
    }

    //#endregion

    //#region " Methods "

    //#region " GET "

    //This method gets all of the Spotify playlists created by the current user
    @GetMapping("/getuserspotifyplaylists")
    public ResponseEntity<ApiResponse<Paging<PlaylistSimplified>>> GetUserSpotifyPlaylists(SpotifyTokenInfo authTokens, @RequestParam(required = false)Integer offset) 
        throws Exception {

        //First obtain the spotify client from the connection
        SpotifyApi spotifyApi = this.spotifyConnection.getApiClient(authTokens);

        //Now create a requet builder to get the playlists for the current user
        GetListOfCurrentUsersPlaylistsRequest.Builder requestBuilder = spotifyApi.getListOfCurrentUsersPlaylists();

        //Now determine if there is an offset applied
        if (offset != null) {
            //Offset isn't null. Add it
            requestBuilder.offset(offset);
        }

        //Finally build the request
        final GetListOfCurrentUsersPlaylistsRequest request = requestBuilder.build();

        //Execute the request to obtain the playlists
        Paging<PlaylistSimplified> playlists = request.execute();

        ApiResponse<Paging<PlaylistSimplified>> response = new ApiResponse<Paging<PlaylistSimplified>>();

        //Set the response content
        response.setResponseContent(playlists);

        //Set the token information
        response.setSpotifyTokenInfo(authTokens);

        //return the response
        return new ResponseEntity<ApiResponse<Paging<PlaylistSimplified>>>(response, HttpStatus.OK);
    }

    //This method adds the provided spotify items to the selected playlist
    @PostMapping("/spotify-items")
    public ResponseEntity<ApiResponse<Boolean>> postSpotifyItems(
        @RequestBody(required = true) PostSpotifyItemRequest requestBody
    ) throws Exception {

        //First create the return variable
        Boolean itemsAdded = false;

        //Grab the fields from the request body
        SpotifyTokenInfo authTokens = requestBody.getAuthTokens();
        String playlistId = requestBody.getPlaylistId();
        String[] spotifyItems = requestBody.getSpotifyItems();
            
        //First obtain the spotify client from the connection
        SpotifyApi spotifyApi = this.spotifyConnection.getApiClient(authTokens);

        //Create the request object
        final AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi
        .addItemsToPlaylist(playlistId, spotifyItems)
        .build();

        //Run the request
        SnapshotResult snapshot = addItemsToPlaylistRequest.execute();

        //Check if the reques succeeded
        if (snapshot != null) {
            //Success! Set return object to true
            itemsAdded = true;
        }

        //Finally create the result object
        ApiResponse<Boolean> resultObject = new ApiResponse<Boolean>();
        ResponseEntity<ApiResponse<Boolean>> response;

        //Determine what is returned depending on the success
        if (!itemsAdded) {
            //Failure, return false
            resultObject.setResponseContent(false);
            response = new ResponseEntity<ApiResponse<Boolean>>(resultObject, HttpStatus.INTERNAL_SERVER_ERROR);

        } else {
            //Success
            resultObject.setResponseContent(itemsAdded);
            resultObject.setSpotifyTokenInfo(authTokens);
            response = new ResponseEntity<ApiResponse<Boolean>>(resultObject, HttpStatus.OK);
        }

        return response;
    }

    @PostMapping("/spotify-playlist")
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
        SpotifyApi spotifyApi = this.spotifyConnection.getApiClient(authTokens);

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
                    final AddItemsToPlaylistRequest addItems = spotifyApi.addItemsToPlaylist(newPlaylistId, spotifyURIs.toArray(new String[spotifyURIs.size()]))
                    .build();
                    
                    //Insert the items
                    addItems.execute();
                }

                //Create the return content
                PostSpotifyPlaylistResponse content = new PostSpotifyPlaylistResponse(true, newPlaylistId, HttpStatus.CREATED.value());

                //Populate the return object
                response.setResponseContent(content);
                response.setSpotifyTokenInfo(authTokens);
                statusCode = HttpStatus.CREATED;

            } else {
                //The playlist wasn't created, return an error
                PostSpotifyPlaylistResponse errorResponse = new PostSpotifyPlaylistResponse(false, "", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to create a Spotify Playlist");
                
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                response.setResponseContent(errorResponse);
            }

        } else {
            //The user is not valid. Return error
            PostSpotifyPlaylistResponse errorResponse = new PostSpotifyPlaylistResponse(false, "", HttpStatus.INTERNAL_SERVER_ERROR.value(), "User ID was invalid when getting user's profile");
            
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            response.setResponseContent(errorResponse);
        }

        return new ResponseEntity<ApiResponse<PostSpotifyPlaylistResponse>>(response, statusCode);

    }

    //#endregion

    //#endregion
}
