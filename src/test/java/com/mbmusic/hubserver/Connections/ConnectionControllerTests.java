package com.mbmusic.hubserver.Connections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mbmusic.hubserver.BaseTest;

@SpringBootTest
@AutoConfigureMockMvc
public class ConnectionControllerTests extends BaseTest {

    //#region Members

    //Mock MVC object for calling endpoints
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ConnectionController controller;

    @MockitoBean
    private SpotifyApiConnection mockApiConnection;

    private static SpotifyApiGateway mockApiGateway;

    //#endregion

    //#region Tests

    @BeforeAll
    private static void prepareMockGateway() {
        mockApiGateway = mock(SpotifyApiGateway.class);
    }

    //Sanity check
	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

    /**
     * This is the test plan. We need to write tests that do the following
     * 1. Ensure the context loads --DONE--
     * 2. POST spotifylogin tests --DONE--
     *  2.1. Success - Returns a successful redirect
     *  2.2. Failure - Authorization URI returns a failure and it throws a SpotifyAuthorizationException
     *  2.3. Failure - State returned from URI does not match, and it throws a SpotifyAuthorizationException
     * 3. GET redirect tests
     *  3.1. Success - We get a successful redirect to the front end
     *  3.2. Failure - Exceptions properly handled from SpotifyApiGateway.getAuthorizationCodeCredentials()
     *  3.3. Failure - The SpotifyApi token was not inserted (upsertSpotifyApiTokenAsync returned false)
     *  3.4. Failure - Redirect threw an exception, and it was caught within the catch statement
     */


    /**
     * This method tests for successful authorization request to the Spotify API
     * @throws Exception
     */
    @Test
    public void postSpotifyLoginSuccess() throws Exception {
        String URI_WITHOUT_STATE = "http://testuri.com/";

        when(mockApiConnection.createEmptySpotifyApiGateway())
            .thenReturn(mockApiGateway);
        when(mockApiGateway.createAuthorizationURI(anyString()))
            .thenAnswer((InvocationOnMock invoation) -> {
                String state = invoation.getArgument(0);

                return new URI(URI_WITHOUT_STATE + "?state=" + state);
            });
        
        mvc.perform(post("/conn/spotifylogin"))
            .andExpect(status().isFound());
    }

    //#endregion

}
