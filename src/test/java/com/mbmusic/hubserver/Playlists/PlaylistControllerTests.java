package com.mbmusic.hubserver.Playlists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
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
import com.mbmusic.hubserver.Connections.ConnectionUtils;
import com.mbmusic.hubserver.Connections.SpotifyApiConnection;

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

    //Mock for the spotify api connection
    @MockitoBean
    private SpotifyApiConnection mockedSpotifyApiConnection;

    private SpotifyApi mockApi = mock(SpotifyApi.class);

    private String SUCCESSFUL_SESSION_COOKIE_VALUE = "Success";

    private String UNSUCCESSFUL_SESSION_COOKIE_VALUE = "Failure";

    //Object mapper for interpreting results
    ObjectMapper mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    //#endregion

    //#region " Tests "

    @BeforeEach
    public void mockSpotifyConnection() {

        try {
            when(mockedSpotifyApiConnection.createApiClient(SUCCESSFUL_SESSION_COOKIE_VALUE))
            .thenReturn(CompletableFuture.completedFuture(mockApi));
        } catch (Exception e) {
            fail(e.getMessage());
        }
                
    }

    //#region " GET "

    //Sanity check
	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

    //This method tests that spotify-playlists returns playlists
    @Test
    public void shouldGetPlaylists() throws Exception {

        //Mock the Sptoify API
        GetListOfCurrentUsersPlaylistsRequest.Builder mockBuilder = mock(GetListOfCurrentUsersPlaylistsRequest.Builder.class);
        GetListOfCurrentUsersPlaylistsRequest mockApiCall = mock(GetListOfCurrentUsersPlaylistsRequest.class);

        
        File playlistFile = new File("src/test/java/com/mbmusic/hubserver/Playlists/Fixtures/Playlists.json");
        if (!playlistFile.canRead())
            fail();

        Paging<PlaylistSimplified> playlists = mapper.readValue(playlistFile, Paging.class);

        when(mockApi.getListOfCurrentUsersPlaylists()).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockApiCall);
        when(mockApiCall.execute()).thenReturn(playlists);

        //Create the mock cookie
        Cookie mockSessionCookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, SUCCESSFUL_SESSION_COOKIE_VALUE); 

        //Now call the endpoint
        this.mvc.perform(get("/playlists/spotify-playlists")
            .cookie(mockSessionCookie))
            .andExpect(status().isOk())
            .andExpect(content().json(mapper.writeValueAsString(playlists)));
    }

    /**
     * This method tests an attempt to cal GET spotify-playlists with an improper cookie
     * @throws Exception
     */
    @Test
    public void shouldFailToGetPlaylists() throws Exception {

        //Create the mock cookie
        Cookie mockSessionCookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, UNSUCCESSFUL_SESSION_COOKIE_VALUE); 

        //Now call the endpoint
        this.mvc.perform(get("/playlists/spotify-playlists")
            .cookie(mockSessionCookie))
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof Exception));
    }

    //#endregion

    //#region " POST "

    //#endregion

    //#endregion

}
