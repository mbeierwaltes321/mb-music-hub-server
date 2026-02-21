package com.mbmusic.hubserver.Playlists;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import com.mbmusic.hubserver.Common.DataAccess;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistResponse;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

@Component
public class PlaylistDA {

    //#region Members

    @Autowired
    private DataAccess da;

    //#endregion

    //#region Methods

    /**
     * This method retrieves all the Spotify playlists created by the current user
     * @param offset Represents the index of the first playlist to return
     * @return 
     * @throws IOException 
     * @throws SpotifyWebApiException 
     * @throws ParseException 
     */
    public CompletableFuture<Paging<PlaylistSimplified>> retrieveUserPlaylists(String sessionId, Integer offset)
        throws ParseException, SpotifyWebApiException, IOException, InvalidSessionIdException {

        return da.getSpotifyClient(sessionId)
            .thenCompose(spotifyApi -> {
                GetListOfCurrentUsersPlaylistsRequest.Builder requestBuilder = spotifyApi.getListOfCurrentUsersPlaylists();

                //Now determine if there is an offset applied
                if (offset != null) {
                    //Offset isn't null. Add it
                    requestBuilder.offset(offset);
                }
                
                GetListOfCurrentUsersPlaylistsRequest request = requestBuilder.build();

                return request.executeAsync();
            });
    }


    /**
     * This method adds spotify items (tracks, podcast episodes, etc.) to a specified playlist
     * @param playlistId The ID of the spotify playlist for which to add the spotify items
     * @param spotifyItems The spotify items (tracks, podcast episodes, etc.) to add.
     * @return True on success. False otherwise
     * @throws InvalidSessionIdException 
     */
    public CompletableFuture<Boolean> addItemsToPlaylist(String sessionId, String playlistId, List<String> spotifyItems)
        throws InvalidSessionIdException {

        // AddItemsToPlaylistRequest addItemsToPlaylistRequest = da.getSpotifyClient(sessionId).addItemsToPlaylist(playlistId, spotifyItems.toArray(new String[0]))
        // .build();

        return da.getSpotifyClient(sessionId)
            .thenCompose(spotifyApi -> {
                AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistId,spotifyItems.toArray(new String[0]))
                    .build();

                return addItemsToPlaylistRequest.executeAsync()
                    .thenApply(snapshot -> snapshot != null);
            });
    }

    /**
     * This method attempts to create a playlist in Spotify and subsequently add items in there, if provided
     * @param sessionId The ID of the session associated with the Spotify token information
     * @param playlistName The name of the playlist to create
     * @param playlistDescription The description to associate with the newly created playlist
     * @param spotifyURIs URIs of the spotify items ()
     * @param isPublic
     * @return A {@link PostSpotifyPlaylistResponse} object indiciating success or failure. If there was a failure, 
     * then the error message is populated with the reason
     * @throws InvalidSessionIdException
     */
    public CompletableFuture<PostSpotifyPlaylistResponse> createSpotifyPlaylist(String sessionId, String playlistName, 
        String playlistDescription, List<String> spotifyURIs, boolean isPublic) throws InvalidSessionIdException {

        return da.getSpotifyClient(sessionId).thenCompose(spotifyApi -> {
            return spotifyApi.getCurrentUsersProfile()
                .build()
                .executeAsync()
                    .thenApply(currentUser -> Pair.of(spotifyApi, currentUser));
        }).thenCompose(apiAndUser -> {
            SpotifyApi spotifyApi = apiAndUser.getFirst();
            User currentUser = apiAndUser.getSecond();
            String userId = currentUser.getId();

            //Validate the userId
            if (userId == null || userId.isBlank()) {
                //return new PostSpotifyPlaylistResponse(false, "", "User ID was invalid when getting user's profile");
                throw new RuntimeException("Invalid User ID when obtaining user's profile");
            }

            //Now create a playlist request
            return spotifyApi.createPlaylist(userId, playlistName)
                .description(playlistDescription)
                .public_(isPublic)  //NOTE: the Spotify API is outdated, and you cannot create a private playlist at the moment :(
                .build()
                .executeAsync()
                    .thenApply(newPlaylist -> Pair.of(spotifyApi, newPlaylist));
        }).thenCompose(apiAndNewPlaylist -> {
            SpotifyApi spotifyApi = apiAndNewPlaylist.getFirst();
            Playlist newPlaylist = apiAndNewPlaylist.getSecond();

            if (newPlaylist == null) {
                //return new PostSpotifyPlaylistResponse(false, "", "Unable to create a Spotify Playlist");
                throw new RuntimeException("Error creating the spotify playlist");
            }

            String newPlaylistId = newPlaylist.getId();

            //Add spotify items to the newly created playlist
            if (spotifyURIs != null && !spotifyURIs.isEmpty()) {
                //Now create a playlist insert request
                final AddItemsToPlaylistRequest addItems = spotifyApi.addItemsToPlaylist(newPlaylistId, 
                    spotifyURIs.toArray(new String[0]))
                    .build();
                
                //Insert the items
                return addItems.executeAsync()
                    .thenApply(snapshotResult -> new PostSpotifyPlaylistResponse(true, newPlaylistId));
            }

            return CompletableFuture.completedFuture(new PostSpotifyPlaylistResponse(true, newPlaylistId));
        });
    }

    //#endregion

}
