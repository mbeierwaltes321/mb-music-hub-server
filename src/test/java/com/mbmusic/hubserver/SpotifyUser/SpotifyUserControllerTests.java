package com.mbmusic.hubserver.SpotifyUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.concurrent.CompletableFuture;

import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Common.DataAccess;
import com.mbmusic.hubserver.Connections.ConnectionUtils;
import com.mbmusic.hubserver.Connections.SpotifyApiGateway;
import jakarta.servlet.http.Cookie;

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

    //#endregion

}
