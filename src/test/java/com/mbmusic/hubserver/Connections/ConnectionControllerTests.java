package com.mbmusic.hubserver.Connections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Common.Utilities.TimeUtils;
import com.mbmusic.hubserver.Connections.Clients.ValkeyClient;
import com.mbmusic.hubserver.Connections.Exceptions.SpotifyAuthorizationException;
import com.mbmusic.hubserver.Connections.Models.SpotifyTokenInfo;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

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

    @MockitoBean
    private ValkeyClient mockValkeyClient;

    private static SpotifyApiGateway mockApiGateway;

    //#endregion

    //#region Methods

    @BeforeEach
    private void prepareMockGateway() {
        mockApiGateway = mock(SpotifyApiGateway.class);
        when(mockApiConnection.createEmptySpotifyApiGateway())
            .thenReturn(mockApiGateway);
    }

    /**
     * This method builds and retrieves an authorization code credentials stub
     * @return
     */
    private AuthorizationCodeCredentials getMockAuthorizationCreds() {
        return new AuthorizationCodeCredentials.Builder()
            .setAccessToken("Successfull Access")
            .setRefreshToken("Time to refresh!")
            .setExpiresIn(11)
            .build();
    }

    //#endregion

    //#region Tests

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
     *  2.3. Failure - State returned from URI does not match, and it throws a SpotifyAuthorizationException --DONE--
     * 3. GET redirect tests
     *  3.1. Success - We get a successful redirect to the front end --DONE--
     *  3.2. Failure - Exceptions properly handled from SpotifyApiGateway.getAuthorizationCodeCredentials() --DONE--
     *  3.3. Failure - The SpotifyApi token was not inserted (upsertSpotifyApiTokenAsync returned false) --DONE--
     *  3.4. Failure - Redirect threw an exception, and it was caught within the catch statement --DONE--
     */


    /**
     * This method tests for successful authorization request to the Spotify API
     * @throws Exception
     */
    @Test
    public void postSpotifyLoginSuccess() throws Exception {
        final String URI_WITHOUT_STATE = "http://testuri.com/";

        final StringBuilder finalUri = new StringBuilder(URI_WITHOUT_STATE);

        when(mockApiGateway.createAuthorizationURI(anyString()))
            .thenAnswer((InvocationOnMock invoation) -> {
                String state = invoation.getArgument(0);

                finalUri.append("?state=" + state);
                return new URI(finalUri.toString());
            });
        
        mvc.perform(get("/conn/spotifylogin"))
            .andExpect(status().isFound())
            .andExpect(header().exists("Location"))
            .andExpect(header().string("Location", finalUri.toString()));
    }

    /**
     * This method tests the scenario where the authorization request returns an error
     * @throws Exception
     */
    @Test
    public void postSpotifyLogin_authorizationShouldReturnError() throws Exception {
        URI URI_WITH_ERROR = new URI("http://testuri.com/?state=Hawaii&error=failure");

        when(mockApiGateway.createAuthorizationURI(anyString()))
            .thenReturn(URI_WITH_ERROR);

        var exception = mvc.perform(get("/conn/spotifylogin"))
            .andExpect(status().isInternalServerError())
            .andReturn()
            .getResolvedException();

        assertInstanceOf(SpotifyAuthorizationException.class, exception);
        assertTrue(exception.getMessage().contains("Access denied"));
        assertTrue(exception.getMessage().contains("failure"));

    }

    /**
     * This method handles the case where the authorization request returns a different state than was provided
     * @throws Exception
     */
    @Test
    public void postSpotifyLogin_stateShouldNotMatch() throws Exception {
        URI URI_WITH_INVALID_STATE = new URI("http://testuri.com/?state=Invalid!!!");

        when(mockApiGateway.createAuthorizationURI(anyString()))
            .thenReturn(URI_WITH_INVALID_STATE);
        
        var exception = mvc.perform(get("/conn/spotifylogin"))
            .andExpect(status().isInternalServerError())
            .andReturn()
            .getResolvedException();

        assertInstanceOf(SpotifyAuthorizationException.class, exception);
        assertTrue(exception.getMessage().contains("Access denied"));
        assertTrue(exception.getMessage().contains("State did not match"));

    }

    /**
     * This method should successfully test the POST redirect request called by Spotify to deliver the 
     * code used to request authentication tokens
     * @throws Exception
     */
    @Test
    public void generateSpotifyAuthToken() throws Exception {
        
        final String SUCCESSFUL_CODE = "success";
        AuthorizationCodeCredentials creds = getMockAuthorizationCreds();

        when(mockApiGateway.getAuthorizationCodeCredentials(SUCCESSFUL_CODE))
            .thenReturn(creds);   
        when(mockValkeyClient.upsertSpotifyAPITokenAsync(any(UUID.class), any(SpotifyTokenInfo.class)))
            .thenReturn(CompletableFuture.completedFuture(true));
        
        //TODO - When we change the redirect URI then update accordingly
        mvc.perform(get("/conn/redirect")
            .accept(MediaType.APPLICATION_JSON)
            .queryParam("code", SUCCESSFUL_CODE)
            .queryParam("state", "Hawaii"))
            .andExpect(status().isFound())
            .andExpect((MvcResult result) -> {
                Cookie returnedCookie = result.getResponse().getCookies()[0];
                assertTrue(() -> returnedCookie.getSecure() == false &&
                        returnedCookie.getDomain().contentEquals("127.0.0.1") &&
                        returnedCookie.isHttpOnly() == true &&
                        returnedCookie.getPath().contentEquals("/") &&
                        returnedCookie.getAttribute("SameSite").contentEquals("Lax") &&
                        returnedCookie.getMaxAge() == TimeUtils.WEEK_SECONDS
                );

                String locationHeader = result.getResponse().getHeader("Location");
                assertNotNull(locationHeader);
                assertTrue(locationHeader.contentEquals("http://127.0.0.1:5173/"));
            });

    }

    /**
     * This method tests the scenario where there was a failure to retrieve the authorization code credentials
     * @throws Exception 
     */
    @Test
    public void generateSpotifyAuthToken_shouldFailToGetAuthorizationCodeCreds() throws Exception {
        final String UNSUCCESSFUL_CODE = "failure";

        when(mockApiGateway.getAuthorizationCodeCredentials(UNSUCCESSFUL_CODE))
            .thenThrow(new SpotifyAuthorizationException("There was an error retrieving the Spotify Authorization Code Credentials"));

        mvc.perform(get("/conn/redirect")
            .accept(MediaType.APPLICATION_JSON)
            .queryParam("code", UNSUCCESSFUL_CODE)
            .queryParam("state", "Hawaii"))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> {
                var exception = result.getResolvedException();
                assertInstanceOf(SpotifyAuthorizationException.class, exception);
                assertTrue(exception.getMessage().contains("There was an error retrieving the Spotify Authorization Code Credentials"));
            });

    }

    /**
     * This method tests the scenario where the spotify api token does not successfully insert
     */
    @Test
    public void generateSpotifyAuthToken_shouldFailToInsertAuthTokens() throws Exception {

        final String SUCCESSFUL_CODE = "success";

        AuthorizationCodeCredentials mockCreds = getMockAuthorizationCreds();

        when(mockApiGateway.getAuthorizationCodeCredentials(SUCCESSFUL_CODE))
            .thenReturn(mockCreds);

        when(mockValkeyClient.upsertSpotifyAPITokenAsync(any(UUID.class), any(SpotifyTokenInfo.class)))
            .thenReturn(CompletableFuture.completedFuture(false));

        var mvcRequest = mvc.perform(get("/conn/redirect")
            .accept(MediaType.APPLICATION_JSON)
            .queryParam("code", SUCCESSFUL_CODE)
            .queryParam("state", "Hawaii"))
            .andExpect(request().asyncStarted())
            .andExpect(result -> {
                var exception = result.getAsyncResult();
                assertInstanceOf(SpotifyAuthorizationException.class, exception);
                assertTrue(((SpotifyAuthorizationException)exception).getMessage().contains("Failed to insert session information into Valkey"));
            })
            .andReturn();
        
        mvc.perform(asyncDispatch(mvcRequest))
            .andExpect(status().isInternalServerError());
    }

    /**
     * This method tests the scenario where the spotify api token does not successfully insert
     */
    @Test
    public void generateSpotifyAuthToken_shouldFailToRedirectToFrontEnd() throws Exception {
        final String SUCCESSFUL_CODE = "success";
        final String STATE = "state";
        final HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        when(mockApiGateway.getAuthorizationCodeCredentials(SUCCESSFUL_CODE))
            .thenReturn(getMockAuthorizationCreds());

        when(mockValkeyClient.upsertSpotifyAPITokenAsync(any(UUID.class), any(SpotifyTokenInfo.class)))
            .thenReturn(CompletableFuture.completedFuture(true));
            
        doThrow(IOException.class).when(mockResponse).sendRedirect(anyString());

        ConnectionController mockController = new ConnectionController(mockApiConnection, mockValkeyClient);

        var exception = assertThrows(CompletionException.class,
            () -> mockController.generateSpotifyAuthToken(SUCCESSFUL_CODE, STATE, mockResponse).join());

        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertTrue(exception.getMessage().contains("Error redirecting to the front end"));
    }

    //#endregion

}
