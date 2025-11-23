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
    public ResponseEntity<PostSpotifyPlaylistResponse> postSpotifyPlaylist(@CookieValue(name = "__Secure-SpotifySessionId") Cookie sessionIdCookie, @RequestBody(required = true) PostSpotifyPlaylistRequest requestBody) throws Exception {

        PlaylistDA da = new PlaylistDA(sessionIdCookie.getValue());

        var response = da.createSpotifyPlaylist(requestBody.getPlaylistName(), requestBody.getPlaylistDescription(), requestBody.getSpotifyURIs(), requestBody.getIsPublic());

        if (!response.getSuccess()) {
            return new ResponseEntity<PostSpotifyPlaylistResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<PostSpotifyPlaylistResponse>(response, HttpStatus.OK);

    }

    //#endregion

    //#endregion
}
