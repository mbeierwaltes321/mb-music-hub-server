package com.mbmusic.backend.Playlists;

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

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
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
        @RequestBody(required = true) PostSpotifyItemRequest requestBody)
        throws Exception {

        //First create the return variable
        Boolean result = false;

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
            result = true;
        }

        //Finally create the result object
        ApiResponse<Boolean> resultObject = new ApiResponse<Boolean>();
        resultObject.setResponseContent(result);
        resultObject.setSpotifyTokenInfo(authTokens);
        ResponseEntity<ApiResponse<Boolean>> response = new ResponseEntity<ApiResponse<Boolean>>(resultObject, HttpStatus.CREATED);

        return response;
    }

    //#endregion

    //#endregion
}
