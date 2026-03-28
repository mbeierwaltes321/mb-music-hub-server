package com.mbmusic.hubserver.Playlists;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mbmusic.hubserver.Connections.ConnectionUtils;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyItemRequest;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistRequest;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistResponse;

import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

//This class resembles the controller for accessing playlist data
@RestController
@RequestMapping("playlists")
public class PlaylistController {

    //#region Members

    @Autowired
    private PlaylistDA playlistDA;

    //#endregion

    //#region Methods

    //#region GET

    //This method gets all of the Spotify playlists created by the current user
    @GetMapping("/spotify-playlists")
    public CompletableFuture<Paging<PlaylistSimplified>> getUserSpotifyPlaylists(
        @CookieValue(ConnectionUtils.SPOTIFY_COOKIE_NAME) Cookie sessionIdCookie, 
        @RequestParam(required = false) Integer offset) throws Exception {

        if (sessionIdCookie.getValue() == null || sessionIdCookie.getValue() == "")
            throw new InvalidSessionIdException("Invalid Session ID");

        return playlistDA.retrieveUserPlaylists(sessionIdCookie.getValue(), offset);
    }

    //#endregion

    //#region POST

    //This method adds the provided spotify items to the selected playlist
    @PostMapping("/spotify-items")
    public CompletableFuture<Boolean> postSpotifyItems(
        @CookieValue(name = ConnectionUtils.SPOTIFY_COOKIE_NAME) Cookie sessionIdCookie, 
        @Valid @RequestBody(required = true) PostSpotifyItemRequest requestBody) throws InvalidSessionIdException {

        if (sessionIdCookie.getValue() == null || sessionIdCookie.getValue() == "")
            throw new InvalidSessionIdException("Invalid Session ID");

        return playlistDA.addItemsToPlaylist(sessionIdCookie.getValue(), requestBody.getPlaylistId(), requestBody.getSpotifyItems());

    }

    /**
     * This method inserts a new spotify playlist as well as insert tracks if provided
     * @param sessionIdCookie Cookie containing the session id to access the spotify gateway
     * @param requestBody The request containing the playlist and track information
     * @return A {@link PostSpotifyPlaylistResponse} containing information on whether there was a success or not
     * @throws InvalidSessionIdException
     */
    @PostMapping("/spotify-playlist")
    public CompletableFuture<PostSpotifyPlaylistResponse> postSpotifyPlaylist(
        @CookieValue(name = ConnectionUtils.SPOTIFY_COOKIE_NAME) Cookie sessionIdCookie, 
        @Valid @RequestBody(required = true) PostSpotifyPlaylistRequest requestBody) throws InvalidSessionIdException {

        if (sessionIdCookie.getValue() == null || sessionIdCookie.getValue() == "")
            throw new InvalidSessionIdException("Invalid Session ID");

        var response = playlistDA.createSpotifyPlaylist(sessionIdCookie.getValue(), requestBody.getPlaylistName(), 
            requestBody.getPlaylistDescription(), requestBody.getSpotifyURIs(), requestBody.getIsPublic());
        return response;
    }

    //#endregion

    //#endregion
}
