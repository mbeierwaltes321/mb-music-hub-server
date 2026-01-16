package com.mbmusic.hubserver.Configuration;

import java.util.concurrent.ExecutionException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import glide.api.GlideClient;
import glide.api.models.configuration.GlideClientConfiguration;
import glide.api.models.configuration.NodeAddress;

/**
 * This class contains the necessary information to connect to Valkey
 */
@Configuration
public class ValkeyConfig {

    //#region Members

    private final String valkeyHost = System.getenv("ValkeyHost");
    
    private final String valkeyPort = System.getenv("ValkeyPort");

    private final boolean useSsl = Boolean.parseBoolean(System.getenv("ValkeySsl"));

    /**
     * The base valkey client object to be used for queries
     */
    @Bean
    public GlideClient valkeyGlideClient() throws ExecutionException, InterruptedException {

        NodeAddress address = NodeAddress.builder()
            .host(valkeyHost)
            .port(Integer.parseInt(valkeyPort))
            .build();

        GlideClientConfiguration config = GlideClientConfiguration.builder()
            .address(address)
            .useTLS(useSsl)
            .requestTimeout(500)
            .build();

        return GlideClient.createClient(config).get();
    }

    //#endregion

}
