package com.mbmusic.hubserver.Connections.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when the provided session id is invalid
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Provided Session ID Invalid")
public class InvalidSessionIdException extends Exception {
    
    public InvalidSessionIdException(String message) {
        super(message);
    }

}
