package com.mbmusic.backend.Playlists;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mbmusic.backend.Common.Models.ApiResponse;
import com.mbmusic.backend.Connections.SpotifyApiConnection;
import com.mbmusic.backend.Connections.Models.SpotifyTokenInfo;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
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

    //Test endpoint for testing MVC
    @GetMapping("/test")
    public String getTest() {
        
        GetArtistRequest request = this.spotifyConnection.getApiClient().getArtist("1GxkXlMwML1oSg5eLPiAz3").build();
        Artist artist;

        try {
            //Attempt to grab the artist
            artist = request.execute();
        } catch (Exception e) {            
            System.out.println(e.getMessage());
            return "Oops!";
        }
        return "I LOVE " + artist.getName();
    }

    //This method gets all of the Spotify playlists created by the current user
    @GetMapping("/getuserspotifyplaylists")
    public ResponseEntity<ApiResponse<Paging<PlaylistSimplified>>> GetUserSpotifyPlaylists(SpotifyTokenInfo authTokens) 
        throws Exception {

        //First obtain the spotify client from the connection
        SpotifyApi spotifyApi = this.spotifyConnection.getApiClient(authTokens);

        //Now create a requet to get the playlists for the current user
        final GetListOfCurrentUsersPlaylistsRequest request = spotifyApi.getListOfCurrentUsersPlaylists().build();

        //Execute the request to obtain the playlists
        Paging<PlaylistSimplified> playlists = request.execute();

        ApiResponse<Paging<PlaylistSimplified>> response = new ApiResponse<Paging<PlaylistSimplified>>();

        //Set the response content
        response.setResponseContent(playlists);

        //Set the token information
        response.setSpotifyTokenInfo(authTokens);

        // //Now create the response
        // ResponseEntity<Paging<PlaylistSimplified>> response = new ResponseEntity<Paging<PlaylistSimplified>>(response, HttpStatus.OK);

        //return the response
        return new ResponseEntity<ApiResponse<Paging<PlaylistSimplified>>>(response, HttpStatus.OK);
    }

    //#endregion

    //#endregion
}
