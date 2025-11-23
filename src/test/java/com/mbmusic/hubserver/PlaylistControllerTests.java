package com.mbmusic.hubserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import com.mbmusic.hubserver.Connections.SpotifyApiConnection;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;
import com.mbmusic.hubserver.Playlists.PlaylistController;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistRequest;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

//This class handles test cases for the playlist controller
@SpringBootTest
@AutoConfigureMockMvc
public class PlaylistControllerTests {

    //#region " Members "
    
    //Mock MVC object for calling endpoints
    @Autowired
    private MockMvc mvc;

    //Controller object for sanity check
    @Autowired
	private PlaylistController controller;

    //Object mapper for interpreting results
    ObjectMapper mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    //#endregion

    //#region " Tests "

    //#region " GET "

    //Sanity check
	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

    //This method tests that playlists are returned
    @Test
    public void shouldGetPlaylists() throws Exception {
        //Get the token information to make the API Call
        SpotifyTokenInfo authTokens = TestCommon.getSpotifyTokenInfo();

        LinkedMultiValueMap<String, String> parms = new LinkedMultiValueMap<String, String>();
        parms.add("accessToken", authTokens.getAccessToken());
        parms.add("refreshToken", authTokens.getRefreshToken());
        parms.add("expiresIn", authTokens.getExpiresIn() + "");
        parms.add("tokenGeneratedAt", authTokens.getTokenGeneratedAt().format(DateTimeFormatter.ISO_DATE_TIME));

        //Now call the endpoint
        this.mvc.perform(get("/playlists/spotify-playlists")
        .queryParams(parms))
        .andExpect(status().isOk());
    }

    //This method gets playlists from the spotify-playlists GET request and verifies
    //that the information returned is true
    @Test
    public void playlistContentCorrect() throws Exception {

        //Get the token information to make the API Call
        SpotifyTokenInfo authTokens = TestCommon.getSpotifyTokenInfo();

        final LinkedMultiValueMap<String, String> parms = new LinkedMultiValueMap<String, String>();
        parms.add("accessToken", authTokens.getAccessToken());
        parms.add("refreshToken", authTokens.getRefreshToken());
        parms.add("expiresIn", authTokens.getExpiresIn() + "");
        parms.add("tokenGeneratedAt", authTokens.getTokenGeneratedAt().format(DateTimeFormatter.ISO_DATE_TIME));

        //Now call the endpoint
        final MvcResult result = this.mvc.perform(get("/playlists/spotify-playlists")
        .queryParams(parms))
        .andExpect(status().isOk())
        .andReturn();

        //Get the resulting JSON
        final String resultJson = result.getResponse().getContentAsString();
        
        //Grab the spotify id of the owner
        final String requestUrl = JsonPath.read(resultJson, "$.responseContent.href");

        //Finally, assert that the response href contains my id
        assertTrue(requestUrl.contains("users/1256937600"));

    }

    //#endregion

    //#region " POST "

    //This method tests the spotify-items POST method by adding the itmes to the API playlist
    //and verifying that the items are indeed added
    @SuppressWarnings("null")
    @Test
    public void shouldPostSpotifyItems() throws Exception {
        //Get the token information to make the API Call
        SpotifyTokenInfo authTokens = TestCommon.getSpotifyTokenInfo();

        //Set the PlaylistID
        final String playlistId = "0BWFXwbLStcb99vSbBhJVP";

        //Create a Spotify Client for purposes of calling other API methods
        SpotifyApiConnection spotifyApi = new SpotifyApiConnection();

        //Get a Spotify API
        SpotifyApi api = spotifyApi.createApiClient(authTokens);

        //First obtain a count of the songs in the playlist
        Playlist initialPlaylist = api.getPlaylist(playlistId)
        .build()
        .execute();

        //Get the number of songs originally in the playlist
        final int prevCount = initialPlaylist.getTracks().getTotal();

        //Create the request body object
        com.mbmusic.hubserver.Playlists.Models.PostSpotifyItemRequest body = new com.mbmusic.hubserver.Playlists.Models.PostSpotifyItemRequest();

        //Set the body information
        body.setAuthTokens(authTokens);
        body.setPlaylistId(playlistId);

        //Spotify Items to add:
        ArrayList<String> items = new ArrayList<String>(0); 
        
        items.add("spotify:track:1bV7gUR0CfAOnqro4vOS5U");
        items.add("spotify:track:2dR5WkrpwylTuT3jRWNufa");
        items.add("spotify:track:2FlcxnvybdwpaMmMhaRtSN");
        items.add("spotify:track:0IgSgIyhnVmQLDh64PtmFa");

        body.setSpotifyItems(items);

        //Write the body as a JSON object
        
        String postBodyJSON = mapper.writeValueAsString(body);

        //Get the number of items for use later
        int numItemsToAdd = items.size();

        //Perform the POST request
        this.mvc.perform(post("/playlists/spotify-items")
        .contentType(MediaType.APPLICATION_JSON)
        .content(postBodyJSON))
        .andExpect(status().isOk());

        //First obtain a count of the songs in the playlist
        Playlist updatedPlaylist = api.getPlaylist(playlistId)
        .build()
        .execute();

        //Get the number of songs originally in the playlist
        final int postCount = updatedPlaylist.getTracks().getTotal();

        //Make sure the songs were added
        assertTrue((prevCount + numItemsToAdd) == postCount);
        
    }

    //This method tests the spotify-playlist POST request, which should create a spotify playlist. The resulting playlist is empty
    @SuppressWarnings("null")
    @Test
    public void shouldCreatePlaylistWithoutSongs() throws Exception{
        //Get the token information to make the API Call
        SpotifyTokenInfo authTokens = TestCommon.getSpotifyTokenInfo();

        //Initialize parameters, set request body
        PostSpotifyPlaylistRequest requestBody = new PostSpotifyPlaylistRequest();
        requestBody.setPlaylistName("Test");
        requestBody.setPlaylistDescription("Test Description");
        requestBody.setIsPublic(true);
        requestBody.setAuthTokens(authTokens);

        final String requestBodyJson = mapper.writeValueAsString(requestBody);

        //Run the endpoint, get the result
        mvc.perform(post("/playlists/spotify-playlist")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBodyJson))
        .andExpect(status().isCreated());
        
    }
    
    //This method tests the spotify-playlist POST request, which should create a spotify playlist. The resulting playlist contains 4 songs
    @SuppressWarnings("null")
    @Test
    public void shouldCreatePlaylistWithSongs() throws Exception{
        //Get the token information to make the API Call
        SpotifyTokenInfo authTokens = TestCommon.getSpotifyTokenInfo();

        //Create a Spotify Client for purposes of calling other API methods
        SpotifyApiConnection spotifyApi = new SpotifyApiConnection();

        //Get a Spotify API
        SpotifyApi api = spotifyApi.createApiClient(authTokens);

        List<String> songs = new ArrayList<String>();
        
        songs.add("spotify:track:1bV7gUR0CfAOnqro4vOS5U");
        songs.add("spotify:track:2dR5WkrpwylTuT3jRWNufa");
        songs.add("spotify:track:2FlcxnvybdwpaMmMhaRtSN");
        songs.add("spotify:track:0IgSgIyhnVmQLDh64PtmFa");

        //Initialize parameters, set request body
        PostSpotifyPlaylistRequest requestBody = new PostSpotifyPlaylistRequest();
        requestBody.setPlaylistName("Test with songs");
        requestBody.setPlaylistDescription("Test Description with songs");
        requestBody.setIsPublic(true);
        requestBody.setSpotifyURIs(songs);
        requestBody.setAuthTokens(authTokens);

        final String requestBodyJson = mapper.writeValueAsString(requestBody);

        //Run the endpoint, get the result
        MvcResult mvcResult = mvc.perform(post("/playlists/spotify-playlist")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBodyJson))
        .andExpect(status().isCreated())
        .andReturn();

        //Grab the response of the endpoint
        String resultJson = mvcResult.getResponse().getContentAsString();

        //Extract the playlist ID
        String newPlaylistId = JsonPath.read(resultJson, "$.responseContent.newPlaylistId");

        //Use the path and the API to get the new playlist
        Playlist newPlaylist = api.getPlaylist(newPlaylistId).build().execute();

        List<PlaylistTrack> items = java.util.Arrays.asList(newPlaylist.getTracks().getItems());

        //Assert that each song was inserted
        for (PlaylistTrack playlistTrack : items) {
            //Check that each item in the returned playlist is in the initial songs list
            assertThat(songs.contains(playlistTrack.getTrack().getUri()));
        }

        //TODO - Unfollow, or "delete" the playlist
    }

    //#endregion

    //#endregion

}
