package com.mbmusic.hubserver.Connections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Connections.Exceptions.SpotifyAuthorizationException;

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

    @BeforeEach
    private void prepareMockGateway() {
        mockApiGateway = mock(SpotifyApiGateway.class);
        when(mockApiConnection.createEmptySpotifyApiGateway())
            .thenReturn(mockApiGateway);
    }

    //Sanity check
	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

    /**
     * This is the test plan. We need to write tests that do the following
     * 1. Ensure the context loads --DONE--
     * 2. POST spotifylogin tests
     *  2.1. Success - Returns a successful redirect --DONE--
     *  2.2. Failure - Authorization URI returns a failure and it throws a SpotifyAuthorizationException --DONE--
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

        when(mockApiGateway.createAuthorizationURI(anyString()))
            .thenAnswer((InvocationOnMock invoation) -> {
                String state = invoation.getArgument(0);

                return new URI(URI_WITHOUT_STATE + "?state=" + state);
            });
        
        mvc.perform(post("/conn/spotifylogin"))
            .andExpect(status().isFound());
    }

    /**
     * This method tests the scenario where the authorization request returns an error
     * @throws Exception
     */
    @Test
    public void postSpotifyLogin_shouldReturnError() throws Exception {
        URI URI_WITH_ERROR = new URI("http://testuri.com/?state=Hawaii&error=failure");

        when(mockApiGateway.createAuthorizationURI(anyString()))
            .thenReturn(URI_WITH_ERROR);

        var exception = mvc.perform(post("/conn/spotifylogin"))
            .andExpect(status().isInternalServerError())
            .andReturn()
            .getResolvedException();

        assertInstanceOf(SpotifyAuthorizationException.class, exception);
        assertTrue(exception.getMessage().contains("Access denied"));
        assertTrue(exception.getMessage().contains("failure"));

    }

    //#endregion

}
