package com.mbmusic.backend.Playlists;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mbmusic.backend.Connections.SpotifyApiConnection;

import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;

//This class resembles the controller for accessing playlist data
@RestController
@RequestMapping("/playlist")
public class PlaylistController {
    //#region " Members "
    
    //The client to the spotify API
    private final SpotifyApiConnection spotifyClient;

    //#endregion

    //#region " Constructor "

    public PlaylistController(SpotifyApiConnection connection) {
        this.spotifyClient = connection;
    }

    //#endregion

    //#region " Methods "

    //#region " GET "

    @RequestMapping("test")
    public String GetTest() {
        
        GetArtistRequest request = this.spotifyClient.getApiClient().getArtist("1GxkXlMwML1oSg5eLPiAz3").build();
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

    //#endregion

    //#endregion
}
