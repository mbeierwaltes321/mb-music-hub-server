package com.mbmusic.hubserver;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mbmusic.hubserver.Connections.ConnectionUtils;

import glide.api.GlideClient;
import jakarta.servlet.http.Cookie;
import se.michaelthelin.spotify.SpotifyApi;

@SpringBootTest
public abstract class BaseTest {

    //For all tests, we want to have a mock Valkey Client so we don't have to rely on Valkey
    @MockitoBean
	private GlideClient mockGlideClient;

    @MockitoBean
    private SpotifyApi.Builder mockSpotifyApiBuilder;

    final private String SUCCESSFUL_SESSION_COOKIE_VALUE = "Success";

    protected Cookie mockSessionCookie = new Cookie(ConnectionUtils.SPOTIFY_COOKIE_NAME, SUCCESSFUL_SESSION_COOKIE_VALUE);
}
