package com.mbmusic.hubserver.Playlists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Common.DataAccess;
import com.mbmusic.hubserver.Connections.ConnectionUtils;
import jakarta.servlet.http.Cookie;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

//This class handles test cases for the playlist controller
@SpringBootTest
@AutoConfigureMockMvc
public class PlaylistControllerTests extends BaseTest {

    //#region " Members "
    
    //Mock MVC object for calling endpoints
    @Autowired
    private MockMvc mvc;

    //Controller object for sanity check
    @Autowired
	private PlaylistController controller;

    @MockitoBean
    private DataAccess mockDA;

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

        //Mock the Sptoify API
        SpotifyApi mockApi = mock(SpotifyApi.class);
        GetListOfCurrentUsersPlaylistsRequest.Builder mockBuilder = mock(GetListOfCurrentUsersPlaylistsRequest.Builder.class);
        GetListOfCurrentUsersPlaylistsRequest mockApiCall = mock(GetListOfCurrentUsersPlaylistsRequest.class);

        
        File playlistFile = new File("src/test/java/com/mbmusic/hubserver/Playlists/Fixtures/Playlists.json");
        if (!playlistFile.canRead())
            fail();

        Paging<PlaylistSimplified> playlists = mapper.readValue(playlistFile, Paging.class);

        when(mockDA.getSpotifyClient("test")).thenReturn(mockApi);
        when(mockApi.getListOfCurrentUsersPlaylists()).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockApiCall);
        when(mockApiCall.execute()).thenReturn(playlists);

        //Create the mock cookie
        Cookie mockSessionCookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, "test"); 

        //Now call the endpoint
        this.mvc.perform(get("/playlists/spotify-playlists")
            .cookie(mockSessionCookie))
            .andExpect(status().isOk())
            .andExpect(content().json(mapper.writeValueAsString(playlists)));
    }

    // //This method gets playlists from the spotify-playlists GET request and verifies
    // //that the information returned is true
    // @Test
    // public void playlistContentCorrect() throws Exception {

    //     //Get the token information to make the API Call
    //     SpotifyTokenInfo authTokens = TestBase.getSpotifyTokenInfo();

    //     final LinkedMultiValueMap<String, String> parms = new LinkedMultiValueMap<String, String>();
    //     parms.add("accessToken", authTokens.getAccessToken());
    //     parms.add("refreshToken", authTokens.getRefreshToken());
    //     parms.add("expiresIn", authTokens.getExpiresIn() + "");
    //     parms.add("tokenGeneratedAt", authTokens.getTokenGeneratedAt().format(DateTimeFormatter.ISO_DATE_TIME));

    //     //Now call the endpoint
    //     final MvcResult result = this.mvc.perform(get("/playlists/spotify-playlists")
    //     .queryParams(parms))
    //     .andExpect(status().isOk())
    //     .andReturn();

    //     //Get the resulting JSON
    //     final String resultJson = result.getResponse().getContentAsString();
        
    //     //Grab the spotify id of the owner
    //     final String requestUrl = JsonPath.read(resultJson, "$.responseContent.href");

    //     //Finally, assert that the response href contains my id
    //     assertTrue(requestUrl.contains("users/1256937600"));

    // } 

    //#endregion

    //#region " POST "

    //#endregion

    //#endregion

}
