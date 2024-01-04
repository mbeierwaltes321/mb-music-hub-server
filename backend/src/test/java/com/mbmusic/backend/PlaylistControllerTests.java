package com.mbmusic.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.mbmusic.backend.Connections.SpotifyApiConnection;
import com.mbmusic.backend.Connections.Models.SpotifyTokenInfo;
import com.mbmusic.backend.Playlists.PlaylistController;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Playlist;

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
        this.mvc.perform(get("/playlist/getuserspotifyplaylists")
        .queryParams(parms))
        .andExpect(status().isOk());
    }

    //This method gets playlists from the getuserspotifyplaylists endpoint an verifies
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
        final MvcResult result = this.mvc.perform(get("/playlist/getuserspotifyplaylists")
        .queryParams(parms))
        .andExpectAll(status().isOk())
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
    @Test
    public void shouldPostSpotifyItems() throws Exception {
        //Get the token information to make the API Call
        SpotifyTokenInfo authTokens = TestCommon.getSpotifyTokenInfo();

        //Set the PlaylistID
        final String playlistId = "0BWFXwbLStcb99vSbBhJVP";

        //Create a Spotify Client for purposes of calling other API methods
        SpotifyApiConnection spotifyApi = new SpotifyApiConnection();

        //Get a Spotify API
        SpotifyApi api = spotifyApi.getApiClient(authTokens);

        //First obtain a count of the songs in the playlist
        Playlist initialPlaylist = api.getPlaylist(playlistId)
        .build()
        .execute();

        //Get the number of songs originally in the playlist
        final int prevCount = initialPlaylist.getTracks().getTotal();

        //Create the request body object
        com.mbmusic.backend.Playlists.Models.PostSpotifyItemRequest body = new com.mbmusic.backend.Playlists.Models.PostSpotifyItemRequest();

        //Set the body information
        body.setAuthTokens(authTokens);
        body.setPlaylistId(playlistId);

        //Spotify Items to add:
        ArrayList<String> items = new ArrayList<String>(0); 
        
        items.add("spotify:track:1bV7gUR0CfAOnqro4vOS5U");
        items.add("spotify:track:2dR5WkrpwylTuT3jRWNufa");
        items.add("spotify:track:2FlcxnvybdwpaMmMhaRtSN");
        items.add("spotify:track:0IgSgIyhnVmQLDh64PtmFa");

        body.setSpotifyItems(items.toArray(new String[items.size()]));

        //Write the body as a JSON object
        String postBodyJSON = mapper.writeValueAsString(body);

        //Get the number of items for use later
        int numItemsToAdd = items.size();

        //Perform the POST request
        this.mvc.perform(post("/playlist/spotify-items")
        .contentType(MediaType.APPLICATION_JSON)
        .content(postBodyJSON))
        .andExpect(status().isCreated());

        //First obtain a count of the songs in the playlist
        Playlist updatedPlaylist = api.getPlaylist(playlistId)
        .build()
        .execute();

        //Get the number of songs originally in the playlist
        final int postCount = updatedPlaylist.getTracks().getTotal();

        //Make sure the songs were added
        assertTrue((prevCount + numItemsToAdd) == postCount);
        
    }

    //#endregion

    //#endregion

}
