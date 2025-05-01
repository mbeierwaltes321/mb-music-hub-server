package com.mbmusic.backend;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.*;

//This class is responsible for handling errors from controllers and returning appropriate responses to the user
@ControllerAdvice
public class SpotifyResponseEntityExceptionHandler
    extends ResponseEntityExceptionHandler {

    //This method should handle all exceptions rooting from the SpotifyWebApiException class
    @ExceptionHandler(value = {SpotifyWebApiException.class})
    protected ResponseEntity<Object> handleConflict(SpotifyWebApiException ex, @NonNull WebRequest request) {

        //Declare message body variable
        String responseMessage = "";

        //Declare http status variable
        HttpStatus status;

        //Determine the type of Spotify Exception
        if (ex.getClass().isAssignableFrom(BadGatewayException.class)) {
            
            responseMessage = "The server was acting as a gateway or proxy and received an invalid response from the upstream server";
            status = HttpStatus.BAD_GATEWAY;

        } else if (ex.getClass().isAssignableFrom(BadRequestException.class)) {
            
            responseMessage = "The request could not be understood by the server due to malformed syntax.";
            status = HttpStatus.BAD_REQUEST;
            
        } else if (ex.getClass().isAssignableFrom(ForbiddenException.class)) {
            
            responseMessage = "The server understood the request, but is refusing to fulfill it.";
            status = HttpStatus.FORBIDDEN;

        } else if (ex.getClass().isAssignableFrom(InternalServerErrorException.class)) {
            
            responseMessage = "You should never receive this error because our clever coders catch them all ... but if you are unlucky enough to get one, please report it to us.";
            status = HttpStatus.INTERNAL_SERVER_ERROR;

        } else if (ex.getClass().isAssignableFrom(NotFoundException.class)) {
            
            responseMessage = "The requested resource could not be found. This error can be due to a temporary or permanent condition.";
            status = HttpStatus.NOT_FOUND;

        } else if (ex.getClass().isAssignableFrom(ServiceUnavailableException.class)) {
            
            responseMessage = "The server is currently unable to handle the request due to a temporary condition which will be alleviated after some delay. You can choose to resend the request again.";
            status = HttpStatus.SERVICE_UNAVAILABLE;

        } else if (ex.getClass().isAssignableFrom(TooManyRequestsException.class)) {
            
            responseMessage = "Rate limiting has been applied.";
            status = HttpStatus.TOO_MANY_REQUESTS;

        } else if (ex.getClass().isAssignableFrom(UnauthorizedException.class)) {
            
            responseMessage = "The request requires user authorization or, if the request included authorization credentials, authorization has been refused for those credentials.";
            status = HttpStatus.UNAUTHORIZED;

        } else {
            //Return a Spotify API exception not found in this method which should not be possible
            responseMessage = "An unknown Spotify API exception has occured";
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        //Return the exception
        return handleExceptionInternal(ex, responseMessage, new HttpHeaders(), status, request);

    }
}
