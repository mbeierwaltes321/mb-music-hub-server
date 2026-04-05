package com.mbmusic.hubserver.Connections;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.mbmusic.hubserver.BaseTest;

@SpringBootTest
@AutoConfigureMockMvc
public class ConnectionControllerTests extends BaseTest {

    /**
     * This is the test plan. We need to write tests that do the following
     * 1. Ensure the context loads
     * 2. POST spotifylogin tests
     *  2.1. Success - Returns a successful redirect
     *  2.2. Failure - Authorization URI returns a failure and it throws a SpotifyAuthorizationException
     *  2.3. Failure - State returned from URI does not match, and it throws a SpotifyAuthorizationException
     * 3. GET redirect tests
     *  3.1. Success - We get a successful redirect to the front end
     *  3.2. Failure - Exceptions properly handled from SpotifyApiGateway.getAuthorizationCodeCredentials()
     *  3.3. Failure - The SpotifyApi token was not inserted (upsertSpotifyApiTokenAsync returned false)
     *  3.4. Failure - Redirect threw an exception, and it was caught within the catch statement
     */

}
