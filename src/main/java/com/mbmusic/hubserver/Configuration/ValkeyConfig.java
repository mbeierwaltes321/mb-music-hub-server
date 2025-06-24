package com.mbmusic.hubserver.Configuration;

import org.springframework.stereotype.Component;

/**
 * This class contains the necessary information to connect to Valkey
 */
@Component
public class ValkeyConfig {

    private String valkeyHost = System.getenv("ValkeyHost");
    
    private String valkeyPort = System.getenv("ValkeyPort");

    private boolean useSsl = Boolean.parseBoolean("ValkeySsl");

    public String getValkeyHost() {
        return valkeyHost;
    }

    public String getValkeyPort() {
        return valkeyPort;
    }

    public boolean isUseSsl() {
        return useSsl;
    }
    
}
