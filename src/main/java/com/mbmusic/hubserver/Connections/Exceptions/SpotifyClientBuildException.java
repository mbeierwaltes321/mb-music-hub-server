package com.mbmusic.hubserver.Connections.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception describes an issue that arises when building an individual Spotify client
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error occured when building Spotify API Client")
public class SpotifyClientBuildException extends RuntimeException {
    
    /**
     * Creates a SpotifyClientBuild exception only knowing the message
     * @param message The error message associated with the exception
     */
    public SpotifyClientBuildException(String message) {
        super(message);
    }

    /**
     * Creates a SpotifyClientBuildException from another exception
     * @param cause The exception for which to base this one
     */
    public SpotifyClientBuildException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a SpotifyClientBuildException with a provided message and base exception
     * @param message The error message associated with the exception
     * @param cause The exception for which to base this one
     */
    public SpotifyClientBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
