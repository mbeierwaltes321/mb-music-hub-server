package com.mbmusic.hubserver.Playlists;

import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mbmusic.hubserver.Common.DataAccess;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistResponse;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
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
    public Paging<PlaylistSimplified> retrieveUserPlaylists(String sessionId, Integer offset) throws ParseException, SpotifyWebApiException, IOException {

        //Now create a requet builder to get the playlists for the current user
        GetListOfCurrentUsersPlaylistsRequest.Builder requestBuilder = da.getSpotifyClient(sessionId).getListOfCurrentUsersPlaylists();

        //Now determine if there is an offset applied
        if (offset != null) {
            //Offset isn't null. Add it
            requestBuilder.offset(offset);
        }

        //Finally build the request
        final GetListOfCurrentUsersPlaylistsRequest request = requestBuilder.build();

        //Execute the request to obtain the playlists
        Paging<PlaylistSimplified> playlists = request.execute();

        return playlists;

    }


    /**
     * This method adds spotify items (tracks, podcast episodes, etc.) to a specified playlist
     * @param playlistId The ID of the spotify playlist for which to add the spotify items
     * @param spotifyItems The spotify items (tracks, podcast episodes, etc.) to add.
     * @return True on success. False otherwise
     * @throws ParseException
     * @throws SpotifyWebApiException
     * @throws IOException
     */
    public boolean addItemsToPlaylist(String sessionId, String playlistId, List<String> spotifyItems) throws ParseException, SpotifyWebApiException, IOException {

        final AddItemsToPlaylistRequest addItemsToPlaylistRequest = da.getSpotifyClient(sessionId).addItemsToPlaylist(playlistId, spotifyItems.toArray(new String[0]))
        .build();

        //Check if the items were added
        SnapshotResult snapshot = addItemsToPlaylistRequest.execute();
        if (snapshot == null)
            return false;

        return true;
    }

    /**
     * This method attempts to create a playlist in Spotify and subsequently add items in there, if provided
     * @param playlistName The name of the playlist to create
     * @param playlistDescription The description in the playlist to create
     * @param spotifyURIs URIs containing the spotify items to include in the playlist
     * @param isPublic Flag indicating whether the sptoify playlist should be public or not
     * @return A {@link PostSpotifyPlaylistResponse} object indiciating success or failure. If there was a failure, then the error message is populated with the reason
     * @throws Exception
     */
    public PostSpotifyPlaylistResponse createSpotifyPlaylist(String sessionId, String playlistName, String playlistDescription, List<String> spotifyURIs, boolean isPublic) throws Exception {

        User currentUser = da.getSpotifyClient(sessionId).getCurrentUsersProfile()
            .build()
            .execute();

        String userId = currentUser.getId();

        //Validate the userId
        if (userId == null || userId.isBlank()) {
            return new PostSpotifyPlaylistResponse(false, "", "User ID was invalid when getting user's profile");
        }

        //Now create a playlist request
        CreatePlaylistRequest createPlaylist = da.getSpotifyClient(sessionId).createPlaylist(userId, playlistName)
            .description(playlistDescription)
            .public_(isPublic)  //NOTE: the Spotify API is outdated, and you cannot create a private playlist at the moment :(
            .build();

        Playlist newPlaylist = createPlaylist.execute();

        if (newPlaylist == null) {
            return new PostSpotifyPlaylistResponse(false, "", "Unable to create a Spotify Playlist");
        }

        String newPlaylistId = newPlaylist.getId();

        //Add spotify items to the newly created playlist
        if (spotifyURIs != null && !spotifyURIs.isEmpty()) {
            //Now create a playlist insert request
            final AddItemsToPlaylistRequest addItems = da.getSpotifyClient(sessionId).addItemsToPlaylist(newPlaylistId, spotifyURIs.toArray(new String[0]))
            .build();
            
            //Insert the items
            addItems.execute();
        }

        return new PostSpotifyPlaylistResponse(true, newPlaylistId);

    }

    //#endregion

}
