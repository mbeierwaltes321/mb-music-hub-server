package com.mbmusic.hubserver.Connections;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.hc.core5.http.ParseException;

import com.mbmusic.hubserver.Connections.Exceptions.SpotifyAuthorizationException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

/**
 * This class is meant to encapsulate logic used by the Spotify API client using the Gateway pattern defined by
 * Martin Fowler: https://martinfowler.com/articles/gateway-pattern.html
 * Ideally, this will inject the spotify connection and perform the actual API calls, while the different
 * data access classes will inject this class
 */
public class SpotifyApiGateway {

    //#region Members

    private SpotifyApi spotifyClient;

    //#endregion

    //#region Constructor

    public SpotifyApiGateway(SpotifyApi spotifyClient) {
        this.spotifyClient = spotifyClient;
    }

    //#endregion

    //#region Methods

    /**
     * This method performs the authorization request to Spotify and retruns the authorization
     * URI for the user to authorize the app
     * @return The Authorization URI
     */
    public URI createAuthorizationURI(String state) {
        return spotifyClient.authorizationCodeUri()
            .state(state)
            .response_type("code")
            .scope("user-library-read playlist-read-private playlist-modify-public playlist-modify-private")
            .build()
            .execute();
    }

    /**
     * This method takes a code returned from the Spotify API and returns the authorization
     * code credentails needed for a Spotify Token
     * @param code The authorization code given from the Spotify API
     * @return The authorization code credentials
     * @throws ParseException
     * @throws SpotifyWebApiException
     * @throws IOException
     */
    public AuthorizationCodeCredentials getAuthorizationCodeCredentials(String code) 
        throws ParseException, SpotifyWebApiException, IOException {
            try {
                return spotifyClient.authorizationCode(code)
                    .build()
                    .execute();
            } catch (Exception e) {
                throw new SpotifyAuthorizationException( 
                    "There was an error retrieving the Spotify Authorization Code Credentials"
                );
            }
    }

    /**
     * This method retrieves all the Spotify playlists created by the current user 
     * @param offset Represents the offset indicating which page of playlists to return
     * @return A CompletableFuture containing the specified page of playlists 
     */
    public CompletableFuture<Paging<PlaylistSimplified>> retrieveUserPlaylists(Integer offset) {
        GetListOfCurrentUsersPlaylistsRequest.Builder requestBuilder = spotifyClient.getListOfCurrentUsersPlaylists();

        //Now determine if there is an offset applied
        if (offset != null) {
            //Offset isn't null. Add it
            requestBuilder.offset(offset);
        }
        
        GetListOfCurrentUsersPlaylistsRequest request = requestBuilder.build();

        return request.executeAsync();
    }

    /**
     * This method adds spotify items (tracks, podcast episodes, etc.) to a specified playlist
     * @param playlistId The ID of the playlist for which to insert the items
     * @param spotifyItems The items to insert
     * @return
     */
    public CompletableFuture<Boolean> addItemsToPlaylist(String playlistId, List<String> spotifyItems) {

        AddItemsToPlaylistRequest addItemsToPlaylistRequest = 
            spotifyClient.addItemsToPlaylist(playlistId,spotifyItems.toArray(new String[0]))
            .build();

        return addItemsToPlaylistRequest.executeAsync()
            .thenApply(snapshot -> snapshot != null);
    }

    /**
     * This method retrieve's the current user's Spotify profile
     * @return The current user's User object
     */
    public CompletableFuture<User> getCurrentSpotifyUserProfile() {
        return spotifyClient.getCurrentUsersProfile()
            .build()
            .executeAsync();
    }

    /**
     * This method creates a new playlist on Spotify for the provided user
     * @param userId The ID of the Spotify User who authors the playlist
     * @param playlistName The name of the playlist
     * @param playlistDescription The description of the playlist
     * @param isPublic Whether or not the newly created playlist is public or not
     * @return The newly created Playlist
     */
    public CompletableFuture<Playlist> createNewSpotifyPlaylist(String userId, String playlistName,
        String playlistDescription, boolean isPublic) {
        //Validate the userId
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("Invalid User ID when obtaining user's profile");
        }

        //Now create a playlist request
        return spotifyClient.createPlaylist(userId, playlistName)
            .description(playlistDescription)
            .public_(isPublic)  //NOTE: the Spotify API is outdated, and you cannot create a private playlist at the moment :(
            .build()
            .executeAsync();
    }

    //#endregion

}
