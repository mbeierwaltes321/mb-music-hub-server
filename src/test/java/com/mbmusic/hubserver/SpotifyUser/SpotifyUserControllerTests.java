package com.mbmusic.hubserver.SpotifyUser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mbmusic.hubserver.BaseTest;
import com.mbmusic.hubserver.Common.DataAccess;

@SpringBootTest
@AutoConfigureMockMvc
public class SpotifyUserControllerTests extends BaseTest {

    private MockMvc mvc;

    private SpotifyUserController controller;

    @MockitoBean
    private DataAccess mockDa;

    @Autowired
    public SpotifyUserControllerTests(MockMvc mvc, SpotifyUserController controller, DataAccess mockDa) {
        this.mvc = mvc;
        this.controller = controller;
        this.mockDa = mockDa;
    }

    //Sanity check
	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}


}
