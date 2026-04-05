package com.mbmusic.hubserver.Connections;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.mbmusic.hubserver.BaseTest;

@SpringBootTest
@AutoConfigureMockMvc
public class ValkeyClientTests extends BaseTest {
    /**
     * Test plan
     * 1. Create happy and sad path tests for getSpotifyAPITokenAsync
     * 2. Create happy and sad path tests for upsertSpotifyAPITokenAsync
     * 3. Create happy and sad path tests for removeTokenAsync
     */
}
