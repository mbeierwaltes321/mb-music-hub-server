package com.mbmusic.hubserver;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

//TODO - Change the context so that it doesn't depend on Valkey
@SpringBootTest
public class AppServerTests {

	//Sanity test; make sure application loads
	@Test
	public void contextLoads() {
	}

}
