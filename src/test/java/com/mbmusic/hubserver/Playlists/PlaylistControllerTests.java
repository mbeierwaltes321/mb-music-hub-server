package com.mbmusic.hubserver.Playlists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Common.DataAccess;
import com.mbmusic.hubserver.Connections.SpotifyApiGateway;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyItemRequest;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistRequest;
import com.mbmusic.hubserver.Playlists.Models.PostSpotifyPlaylistResponse;

import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;

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

    @MockitoBean
    private DataAccess mockDa;

    private SpotifyApiGateway mockGateway;

    //#endregion

    //#region Tests

    @BeforeEach
    public void preparePlaylistTests() {
        try {
            mockGateway = mock(SpotifyApiGateway.class);
            when(mockDa.getSpotifyApiGatewayAsync(anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockGateway));
        } catch (Exception e) {
            fail(e);
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

        when(mockGateway.retrieveUserPlaylists((Integer)isNull()))
            .thenReturn(CompletableFuture.completedFuture(playlists));
        when(mockGateway.retrieveUserPlaylists(anyInt()))
            .thenReturn(CompletableFuture.completedFuture(playlists));

        var mvcResult = this.mvc.perform(get("/playlists/spotify-playlists")
            .cookie(mockSessionCookie))
            .andExpect(request().asyncStarted())
            .andReturn();

        this.mvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(content().json(playlistsJson));
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

        when(mockGateway.addItemsToPlaylist(anyString(), anyList())).thenReturn(CompletableFuture.completedFuture(true));

        PostSpotifyItemRequest request = new PostSpotifyItemRequest();
        request.setPlaylistId(mockPlaylistId);
        request.setSpotifyItems(List.of(spotifyItems));

        var mvcRequest = this.mvc.perform(post("/playlists/spotify-items")
            .cookie(mockSessionCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
            .andExpect(request().asyncStarted())
            .andReturn();

        this.mvc.perform(asyncDispatch(mvcRequest))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

    }

    /**
     * This test ensures that invlaid input returns a 400 with the proper message. This tests
     * the scenarios where the playlist id and spotify items do not contain valid input
     * @throws Exception 
     */
    @Test
    public void shouldFailToInsertTracksIntoPlaylistFromInvalidInput() throws Exception {
        var invalidRequestBody = new PostSpotifyItemRequest();
        invalidRequestBody.setPlaylistId(null);
        invalidRequestBody.setSpotifyItems(List.of("test"));

        mvc.perform(post("/playlists/spotify-items")
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(mockSessionCookie)
            .content(mapper.writeValueAsString(invalidRequestBody)))
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("playlistId: must not be null"));

        invalidRequestBody.setSpotifyItems(null);

        mvc.perform(post("/playlists/spotify-items")
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(mockSessionCookie)
            .content(mapper.writeValueAsString(invalidRequestBody)))
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(StringContains.containsString("spotifyItems: must not be null")))
            .andExpect(content().string(StringContains.containsString("playlistId: must not be null")));

        invalidRequestBody.setPlaylistId("test");
        invalidRequestBody.setSpotifyItems(List.of());

        mvc.perform(post("/playlists/spotify-items")
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(mockSessionCookie)
            .content(mapper.writeValueAsString(invalidRequestBody)))
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("spotifyItems: size must be between 1 and 2147483647"));

    }

    /**
     * This method tests the the POST spotify-playlist endpoint without spotify items
     * @throws Exception
     */
    @Test
    public void shouldCreatePlaylistWithoutItems() throws Exception {
  
        PostSpotifyPlaylistRequest requestBody = new PostSpotifyPlaylistRequest();
        requestBody.setPlaylistName("NewPlaylist");
        requestBody.setPlaylistDescription("NewPlaylistDescription");
        requestBody.setSpotifyURIs(List.of(new String[0]));
        requestBody.setIsPublic(true);

        PostSpotifyPlaylistResponse expectedResponse = new PostSpotifyPlaylistResponse(true, null);

        var mockUser = mock(User.class);
        var mockPlaylist = mock(Playlist.class);
        
        when(mockGateway.getCurrentSpotifyUserProfile()).thenReturn(CompletableFuture.completedFuture(mockUser));
        when(mockUser.getId()).thenReturn("NotNull");   //Pass null check
        when(mockGateway.createNewSpotifyPlaylist(anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(CompletableFuture.completedFuture(mockPlaylist));
            

        var mvcRequest = mvc.perform(post("/playlists/spotify-playlist")
            .cookie(mockSessionCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestBody)))
            .andExpect(request().asyncStarted())
            .andReturn();
            
        mvc.perform(asyncDispatch(mvcRequest))
            .andExpect(status().isOk())
            .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));

        verify(mockGateway, times(0)).addItemsToPlaylist(anyString(), anyList());

    }

    /**
     * This method tests the the POST spotify-playlist endpoint with spotify items
     * @throws Exception
     */
    @Test
    public void shouldCreatePlaylistWithItems() throws Exception {
        PostSpotifyPlaylistRequest requestBody = new PostSpotifyPlaylistRequest();
        requestBody.setPlaylistName("NewPlaylist");
        requestBody.setPlaylistDescription("NewPlaylistDescription");
        requestBody.setSpotifyURIs(List.of("SpotifyTrack"));
        requestBody.setIsPublic(true);

        PostSpotifyPlaylistResponse expectedResponse = new PostSpotifyPlaylistResponse(true, "NotNull");

        var mockUser = mock(User.class);
        var mockPlaylist = mock(Playlist.class);
        
        when(mockGateway.getCurrentSpotifyUserProfile()).thenReturn(CompletableFuture.completedFuture(mockUser));
        when(mockUser.getId()).thenReturn("NotNull");   //Pass null check
        when(mockGateway.createNewSpotifyPlaylist(anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(CompletableFuture.completedFuture(mockPlaylist));
        when(mockPlaylist.getId()).thenReturn("NotNull");
        when(mockGateway.addItemsToPlaylist(anyString(), anyList())).thenReturn(CompletableFuture.completedFuture(true));
            

        var mvcRequest = mvc.perform(post("/playlists/spotify-playlist")
            .cookie(mockSessionCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestBody)))
            .andExpect(request().asyncStarted())
            .andReturn();
            
        mvc.perform(asyncDispatch(mvcRequest))
            .andExpect(status().isOk())
            .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));

        verify(mockGateway, times(1)).addItemsToPlaylist(anyString(), anyList());
    }

    /**
     * This method should test the scenario where POST spotify-playlist fails because of a failiure
     * to retrieve a user's information
     */
    @Test
    public void shouldFailToCreatePlaylistBecauseOfFaultyUserId() throws Exception {
        PostSpotifyPlaylistRequest requestBody = new PostSpotifyPlaylistRequest();
        requestBody.setPlaylistName("NewPlaylist");
        requestBody.setPlaylistDescription("NewPlaylistDescription");
        requestBody.setSpotifyURIs(List.of("SpotifyTrack"));
        requestBody.setIsPublic(true);
        var mockUser = mock(User.class);
        
        when(mockGateway.getCurrentSpotifyUserProfile()).thenReturn(CompletableFuture.completedFuture(mockUser));
        when(mockUser.getId()).thenReturn(null);

        var asyncCall = mvc.perform(post("/playlists/spotify-playlist")
            .cookie(mockSessionCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestBody)))
            .andExpect((result) -> {
                Object exception = result.getAsyncResult();
                assertInstanceOf(RuntimeException.class, exception);
                RuntimeException typedException = ((RuntimeException)exception);
                assertTrue(typedException.getMessage().contains("Invalid User ID when obtaining user's profile"));
               })
            .andReturn();

        mvc.perform(asyncDispatch(asyncCall))
            .andExpect(status().isInternalServerError());

        verify(mockGateway, times(0))
            .createNewSpotifyPlaylist(anyString(), anyString(), anyString(), anyBoolean());
    }

    /**
     * This method tests the scenario where the POST spotify-endpoint endpoint fails becuase of a failure
     * to create the playlist
     * @throws Exception
     */
    @Test
    public void shouldFailToCreatePlaylistBecauseOfNullPlaylist() throws Exception {
        PostSpotifyPlaylistRequest requestBody = new PostSpotifyPlaylistRequest();
        requestBody.setPlaylistName("NewPlaylist");
        requestBody.setPlaylistDescription("NewPlaylistDescription");
        requestBody.setSpotifyURIs(List.of("SpotifyTrack"));
        requestBody.setIsPublic(true);
        var mockUser = mock(User.class);

        when(mockGateway.getCurrentSpotifyUserProfile()).thenReturn(CompletableFuture.completedFuture(mockUser));
        when(mockUser.getId()).thenReturn("UserId");
        when(mockGateway.createNewSpotifyPlaylist(anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        var mvcRequest = mvc.perform(post("/playlists/spotify-playlist")
            .cookie(mockSessionCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestBody)))
            .andExpect(request().asyncStarted())
            .andExpect(result -> {
                var responseVal = result.getAsyncResult();
                assertInstanceOf(RuntimeException.class, responseVal);
                var responseException = (RuntimeException)responseVal;
                assertTrue(responseException.getMessage().contains("Error creating the spotify playlist"));
            })
            .andReturn();
        
        mvc.perform(asyncDispatch(mvcRequest))
            .andExpect(status().isInternalServerError());
    }

    /**
     * This test ensures that invlaid input (empty playlist name) returns a 400 with the proper message. This tests
     * the scenario where the playlist name is both null and the empty string
     * @throws Exception 
     */
    @Test
    public void shouldFailToCreatePlaylistBecauseOfInvalidInput() throws Exception {
        PostSpotifyPlaylistRequest invalidRequestBody = new PostSpotifyPlaylistRequest();
        invalidRequestBody.setPlaylistName(null);
        invalidRequestBody.setSpotifyURIs(null);

        mvc.perform(post("/playlists/spotify-playlist")
            .cookie(mockSessionCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(invalidRequestBody)))
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("playlistName: must not be null"));

        invalidRequestBody.setPlaylistName("");

        mvc.perform(post("/playlists/spotify-playlist")
            .cookie(mockSessionCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(invalidRequestBody)))
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("playlistName: size must be between 1 and 2147483647"));
    }

    //#endregion

}
