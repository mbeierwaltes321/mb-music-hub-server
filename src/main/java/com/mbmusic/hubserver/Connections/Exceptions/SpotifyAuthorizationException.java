package com.mbmusic.hubserver.Connections.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error occurred while obtaining authorization from Spotify")
public class SpotifyAuthorizationException extends RuntimeException {
    /**
     * Creates a SpotifyAuthorizationException exception only knowing the message
     * @param message The error message associated with the exception
     */
    public SpotifyAuthorizationException(String message) {
        super(message);
    }

    /**
     * Creates a SpotifyAuthorizationException from another exception
     * @param cause The exception for which to base this one
     */
    public SpotifyAuthorizationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a SpotifyClientBuildException with a provided message and base exception
     * @param message The error message associated with the exception
     * @param cause The exception for which to base this one
     */
    public SpotifyAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
