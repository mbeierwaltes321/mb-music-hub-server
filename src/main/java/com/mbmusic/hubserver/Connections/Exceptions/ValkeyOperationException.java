package com.mbmusic.hubserver.Connections.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "")
public class ValkeyOperationException extends RuntimeException {

    /**
     * Creates a ValkeyOperationException exception only knowing the message
     * @param message The error message associated with the exception
     */
    public ValkeyOperationException(String message) {
        super(message);
    }

    /**
     * Creates a ValkeyOperationException from another exception
     * @param cause The exception for which to base this one
     */
    public ValkeyOperationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a ValkeyOperationException with a provided message and base exception
     * @param message The error message associated with the exception
     * @param cause The exception for which to base this one
     */
    public ValkeyOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
