package com.mbmusic.hubserver.SpotifyUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Common.DataAccess;
import com.mbmusic.hubserver.Connections.ConnectionUtils;
import com.mbmusic.hubserver.Connections.SpotifyApiGateway;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.SpotifyUser.Models.GetSpotifyUserInfoResponse;

import jakarta.servlet.http.Cookie;
import se.michaelthelin.spotify.enums.ProductType;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.User;

@SpringBootTest
@AutoConfigureMockMvc
public class SpotifyUserControllerTests extends BaseTest {

    //#region Members
    private MockMvc mvc;

    private SpotifyUserController controller;

    @MockitoBean
    private DataAccess mockDa;

    private SpotifyApiGateway mockGateway;

    //#endregion

    //#region Constructor

    @Autowired
    public SpotifyUserControllerTests(MockMvc mvc, SpotifyUserController controller, DataAccess mockDa) {
        this.mvc = mvc;
        this.controller = controller;
        this.mockDa = mockDa;
    }

    //#endregion

    //#region Methods

    @BeforeEach
    public void mockSpotifyApiGateway() {
        try {
            mockGateway = mock(SpotifyApiGateway.class);
            when(mockDa.getSpotifyApiGatewayAsync(any())).thenReturn(CompletableFuture.completedFuture(mockGateway));
        } catch (Exception e) {
            fail(e);
        }
    }

    //Sanity check
	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

    /**
     * This method tets a successful run of getSpotifyUser
     * @throws Exception
     */
    @Test
    public void getSpotifyUser_Succeeds() throws Exception {

        File imagesFile = Path.of("src/test/java/com/mbmusic/hubserver/SpotifyUser/Fixtures/images.json").toFile();
        if (!imagesFile.canRead()) {
            fail();
        }

        TypeReference<Image[]> imageArray = new TypeReference<Image[]>() {};
        Image[] images = mapper.readValue(imagesFile, imageArray);

        User mockSpotifyUser = new User.Builder()
            .setDisplayName("Test Testington")
            .setProduct(ProductType.PREMIUM)
            .setImages(images)
            .build();

        when(mockGateway.getCurrentSpotifyUserProfile()).thenReturn(CompletableFuture.completedFuture(mockSpotifyUser));

        GetSpotifyUserInfoResponse expectedResponse = new GetSpotifyUserInfoResponse();
        expectedResponse.setDisplayName("Test Testington");
        expectedResponse.setSubscriptionLevel("premium");
        expectedResponse.setImageHeight(300);
        expectedResponse.setImageWidth(300);
        expectedResponse.setImageUrl(images[0].getUrl());
        
        MvcResult asyncResult = mvc.perform(get("/spotify-users/info")
            .cookie(mockSessionCookie))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();
        
        mvc.perform(asyncDispatch(asyncResult))
            .andExpect(status().isOk())
            .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    /**
     * This method tests the scenario where the session id is invalid
     * @throws Exception
     */
    @Test
    public void getSpotifyUser_FailsSessionCheck() throws Exception {
        Cookie invalidCookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, "");
        mvc.perform(get("/spotify-users/info")
            .cookie(invalidCookie))
            .andExpect(status().isUnauthorized())
            .andExpect(status().reason("Provided Session ID Invalid"));
    }

    @Test
    public void getSpotifyUser_FailsSessionCheck_InServiceLayer() throws Exception {
        when(mockDa.getSpotifyApiGatewayAsync(anyString())).thenThrow(InvalidSessionIdException.class);

        mvc.perform(get("/spotify-users/info")
            .cookie(mockSessionCookie))
            .andExpect(status().isUnauthorized())
            .andExpect(status().reason("Provided Session ID Invalid"));
    }

    

    //#endregion

}
