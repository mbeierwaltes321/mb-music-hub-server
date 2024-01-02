package com.mbmusic.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import com.mbmusic.backend.Connections.Models.SpotifyTokenInfo;
import com.mbmusic.backend.Playlists.PlaylistController;

//This class handles test cases for the playlist controller
@SpringBootTest
@AutoConfigureMockMvc
public class PlaylistControllerTests {
    
    //Mock MVC object for calling endpoints
    @Autowired
    private MockMvc mvc;

    //Controller object for sanity check
    @Autowired
	private PlaylistController controller;

    //Sanity check
	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

    //This method tests that playlists are returned
    @Test
    public void shouldGetPlaylists() throws Exception {
        //Get the token information to make the API Call
        SpotifyTokenInfo authTokens = TestCommon.getSpotifyTokenInfo();

        LinkedMultiValueMap<String, String> parms = new LinkedMultiValueMap<String, String>();
        parms.add("accessToken", authTokens.getAccessToken());
        parms.add("refreshToken", authTokens.getRefreshToken());
        parms.add("expiresIn", authTokens.getExpiresIn() + "");
        parms.add("tokenGeneratedAt", authTokens.getTokenGeneratedAt().format(DateTimeFormatter.ISO_DATE_TIME));

        //Now call the endpoint
        this.mvc.perform(get("/playlist/getuserspotifyplaylists")
        .queryParams(parms))
        .andExpect(status().isOk());
    }

    //This method gets playlists from the getuserspotifyplaylists endpoint an verifies
    //that the information returned is true
    @Test
    public void playlistContentCorrect() throws Exception {
        //Create the object mapper to map the parameter
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //Get the token information to make the API Call
        SpotifyTokenInfo authTokens = TestCommon.getSpotifyTokenInfo();

        final LinkedMultiValueMap<String, String> parms = new LinkedMultiValueMap<String, String>();
        parms.add("accessToken", authTokens.getAccessToken());
        parms.add("refreshToken", authTokens.getRefreshToken());
        parms.add("expiresIn", authTokens.getExpiresIn() + "");
        parms.add("tokenGeneratedAt", authTokens.getTokenGeneratedAt().format(DateTimeFormatter.ISO_DATE_TIME));

        //Now call the endpoint
        final MvcResult result = this.mvc.perform(get("/playlist/getuserspotifyplaylists")
        .queryParams(parms))
        .andExpectAll(status().isOk())
        .andReturn();

        //Get the resulting JSON
        final String resultJson = result.getResponse().getContentAsString();
        
        //Grab the spotify id of the owner
        final String requestUrl = JsonPath.read(resultJson, "$.responseContent.href");

        //Finally, assert that the response href contains my id
        assertTrue(requestUrl.contains("users/1256937600"));

    }


}
