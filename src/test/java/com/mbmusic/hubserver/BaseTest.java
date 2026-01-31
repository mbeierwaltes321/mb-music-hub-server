package com.mbmusic.hubserver;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import glide.api.GlideClient;
import se.michaelthelin.spotify.SpotifyApi;

@SpringBootTest
public abstract class BaseTest {

    //For all tests, we want to have a mock Valkey Client so we don't have to rely on Valkey
    @MockitoBean
	private GlideClient mockGlideClient;

    @MockitoBean
    private SpotifyApi.Builder mockSpotifyApiBuilder;

}
