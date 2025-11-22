package com.mbmusic.hubserver.Playlists;

import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.ParseException;

import com.mbmusic.hubserver.Common.DataAccessBase;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

public class PlaylistDA extends DataAccessBase {

    //#region Constructor

    protected PlaylistDA(String sessionIdString) throws Exception {
        super(sessionIdString);
    }

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
    public Paging<PlaylistSimplified> retrieveUserPlaylists(Integer offset) throws ParseException, SpotifyWebApiException, IOException {

        //Now create a requet builder to get the playlists for the current user
        GetListOfCurrentUsersPlaylistsRequest.Builder requestBuilder = spotifyClient.getListOfCurrentUsersPlaylists();

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
    public boolean addItemsToPlaylist(String playlistId, List<String> spotifyItems) throws ParseException, SpotifyWebApiException, IOException {

        final AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyClient.addItemsToPlaylist(playlistId, spotifyItems.toArray(new String[0]))
        .build();

        //Check if the items were added
        SnapshotResult snapshot = addItemsToPlaylistRequest.execute();
        if (snapshot == null)
            return false;

        return true;
    }

    //#endregion

}
