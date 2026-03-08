package com.mbmusic.hubserver.Playlists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonArray;
import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Connections.ConnectionUtils;
import com.mbmusic.hubserver.Connections.SpotifyApiConnection;
import com.mbmusic.hubserver.Connections.SpotifyApiGateway;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyItemRequest;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistRequest;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistResponse;

import jakarta.servlet.http.Cookie;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetUsersProfileRequest;

//This class handles test cases for the playlist controller
@SpringBootTest
@AutoConfigureMockMvc
public class PlaylistControllerTests extends BaseTest {

    //#region Members
    
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

    final private String SUCCESSFUL_SESSION_COOKIE_VALUE = "Success";

    final private String UNSUCCESSFUL_SESSION_COOKIE_VALUE = null;

    //Object mapper for interpreting results
    ObjectMapper mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    //#endregion

    //#region Tests

    @BeforeEach
    public void mockSpotifyConnection() {

        try {
            when(mockedSpotifyApiConnection.createApiClient(SUCCESSFUL_SESSION_COOKIE_VALUE))
            .thenReturn(CompletableFuture.completedFuture(mockApi));
        } catch (Exception e) {
            fail(e.getMessage());
        }
                
    }

    //Sanity check
	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

    //This method tests that spotify-playlists returns playlists
    @Test
    public void shouldGetPlaylists() throws Exception {

        File playlistFile = new File("src/test/java/com/mbmusic/hubserver/Playlists/Fixtures/Playlists.json");
        if (!playlistFile.canRead()){
            fail();
        }

        Paging<PlaylistSimplified> playlists = mapper.readValue(playlistFile,
            new TypeReference<Paging<PlaylistSimplified>>() {});
        String playlistsJson = mapper.writeValueAsString(playlists);

        try (MockedConstruction<SpotifyApiGateway> mockedSpotifyGateway =
                Mockito.mockConstruction(SpotifyApiGateway.class, (mockGateway, context) -> {
                    when(mockGateway.retrieveUserPlaylists((Integer)isNull()))
                        .thenReturn(CompletableFuture.completedFuture(playlists));
                    when(mockGateway.retrieveUserPlaylists(any(Integer.class)))
                        .thenReturn(CompletableFuture.completedFuture(playlists));
                })) {

            //Create the mock cookie
            Cookie mockSessionCookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, SUCCESSFUL_SESSION_COOKIE_VALUE); 

            var mvcResult = this.mvc.perform(get("/playlists/spotify-playlists")
                .cookie(mockSessionCookie))
                .andExpect(request().asyncStarted())
                .andReturn();

            this.mvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().json(playlistsJson));
        } catch(Exception e) {
            fail(e);
        }
    }

    /**
     * This test should successfully insert tracks into the playlist
     * @throws Exception 
     */
    @Test
    public void shouldInsertTracksIntoPlaylist() throws Exception {

        File spotifyItemsFile = new File("src/test/java/com/mbmusic/hubserver/Playlists/Fixtures/SpotifyItems.json");
        if (!spotifyItemsFile.canRead()) {
            fail();
        }

        String mockPlaylistId = "11";
        String[] spotifyItems = mapper.readValue(spotifyItemsFile, new TypeReference<String[]>(){});

        try (MockedConstruction<SpotifyApiGateway> mockedSpotifyGateway =
            Mockito.mockConstruction(SpotifyApiGateway.class, (mockedGateway, context) -> {
                when(mockedGateway.addItemsToPlaylist(anyString(), anyList()))
                    .thenReturn(CompletableFuture.completedFuture(true));   
            })) {

            PostSpotifyItemRequest request = new PostSpotifyItemRequest();
            request.setPlaylistId(mockPlaylistId);
            request.setSpotifyItems(List.of(spotifyItems));

            Cookie mockSessionCookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, SUCCESSFUL_SESSION_COOKIE_VALUE);

            var mvcRequest = this.mvc.perform(post("/playlists/spotify-items")
                .cookie(mockSessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

            this.mvc.perform(asyncDispatch(mvcRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
            
        } catch (Exception e) {
            fail(e);
        }
    }

    //TODO - This should be put inside a test class for authentication and validation
    /**
     * This method tests an attempt to cal GET spotify-playlists with an improper cookie
     * @throws Exception
     */
    @Test
    public void shouldFailFromInvalidSessionId() throws Exception {

        //Create the mock cookie
        Cookie mockSessionCookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, UNSUCCESSFUL_SESSION_COOKIE_VALUE); 

        //Now call the endpoint
        this.mvc.perform(get("/playlists/spotify-playlists")
            .cookie(mockSessionCookie))
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidSessionIdException));
    }

    /**
     * TODO - Implement tests for the following createSpotifyPlaylist scenarios:
     * 1. Success without spotify items --DONE--
     * 3. Success with spotify items
     * 4. Failed to get user id's profile
     * 5. Failed - new playlist is null (should never happen)
     */

    @Test
    public void shouldCreatePlaylistWithoutItems() throws Exception {
        
        preparePostSpotifyPlaylist();

        Cookie mockSessionCookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, SUCCESSFUL_SESSION_COOKIE_VALUE); 

        PostSpotifyPlaylistRequest requestBody = new PostSpotifyPlaylistRequest();
        requestBody.setPlaylistName("NewPlaylist");
        requestBody.setPlaylistDescription("NewPlaylistDescription");
        requestBody.setSpotifyURIs(List.of(new String[0]));
        requestBody.setIsPublic(true);

        PostSpotifyPlaylistResponse expectedResponse = new PostSpotifyPlaylistResponse(true, null);

        var mvcRequest = mvc.perform(post("/playlists/spotify-playlist")
            .cookie(mockSessionCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestBody)))
            .andExpect(request().asyncStarted())
            .andReturn();
            
        mvc.perform(asyncDispatch(mvcRequest))
            .andExpect(status().isOk())
            .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));

    }

    //#endregion

    //#region Private Methods

    /**
     * This method performs the mocking necessary for all PostSpotifyPlaylist tests
     */
    public void preparePostSpotifyPlaylist() {
        GetCurrentUsersProfileRequest.Builder mockUserRequestBuilder = mock(GetCurrentUsersProfileRequest.Builder.class);
        GetCurrentUsersProfileRequest mockUserRequest = mock(GetCurrentUsersProfileRequest.class);
        CreatePlaylistRequest.Builder mockCreatePlaylistRequestBuilder = mock(CreatePlaylistRequest.Builder.class);
        CreatePlaylistRequest mockCreatePlaylistRequest = mock(CreatePlaylistRequest.class);
        AddItemsToPlaylistRequest.Builder mockAddToPlaylistBuilder = mock(AddItemsToPlaylistRequest.Builder.class);
        AddItemsToPlaylistRequest mockAddToPlaylistRequest = mock(AddItemsToPlaylistRequest.class);

        User mockUser = mock(User.class);
        Playlist mockPlaylist = mock(Playlist.class);

        when(mockApi.getCurrentUsersProfile()).thenReturn(mockUserRequestBuilder);
        when(mockUserRequestBuilder.build()).thenReturn(mockUserRequest);
        when(mockUserRequest.executeAsync()).thenReturn(CompletableFuture.completedFuture(mockUser));
        when(mockUser.getId()).thenReturn("11");

        when(mockApi.createPlaylist(any(), any())).thenReturn(mockCreatePlaylistRequestBuilder);
        when(mockCreatePlaylistRequestBuilder.description(any())).thenReturn(mockCreatePlaylistRequestBuilder);
        when(mockCreatePlaylistRequestBuilder.public_(any())).thenReturn(mockCreatePlaylistRequestBuilder);
        when(mockCreatePlaylistRequestBuilder.build()).thenReturn(mockCreatePlaylistRequest);
        when(mockCreatePlaylistRequest.executeAsync()).thenReturn(CompletableFuture.completedFuture(mockPlaylist));

        when(mockApi.addItemsToPlaylist(any(), (String[])isNull())).thenReturn(mockAddToPlaylistBuilder);
        when(mockApi.addItemsToPlaylist(any(), (JsonArray)isNull())).thenReturn(mockAddToPlaylistBuilder);
        when(mockAddToPlaylistBuilder.build()).thenReturn(mockAddToPlaylistRequest);

    }

    //#endregion
}
