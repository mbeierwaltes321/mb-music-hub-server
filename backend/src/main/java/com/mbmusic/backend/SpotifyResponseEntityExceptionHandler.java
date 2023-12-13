package com.mbmusic.backend;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    protected ResponseEntity<Object> handleConflict(SpotifyWebApiException ex, WebRequest request) {

        //Declare message body variable
        String bodyOfResponse = "";

        //Determine the type of Spotify Exception
        if (ex.getClass().isAssignableFrom(BadGatewayException.class)) {
            bodyOfResponse = "The server was acting as a gateway or proxy and received an invalid response from the upstream server";

            return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_GATEWAY, request);
        } else if (ex.getClass().isAssignableFrom(BadRequestException.class)) {
            bodyOfResponse = "The request could not be understood by the server due to malformed syntax.";

            return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        } else if (ex.getClass().isAssignableFrom(ForbiddenException.class)) {
            bodyOfResponse = "The server understood the request, but is refusing to fulfill it.";

            return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
        } else if (ex.getClass().isAssignableFrom(InternalServerErrorException.class)) {
            bodyOfResponse = "You should never receive this error because our clever coders catch them all ... but if you are unlucky enough to get one, please report it to us.";

            return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
        } else if (ex.getClass().isAssignableFrom(NotFoundException.class)) {
            bodyOfResponse = "The requested resource could not be found. This error can be due to a temporary or permanent condition.";

            return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
        } else if (ex.getClass().isAssignableFrom(ServiceUnavailableException.class)) {
            bodyOfResponse = "The server is currently unable to handle the request due to a temporary condition which will be alleviated after some delay. You can choose to resend the request again.";

            return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE, request);
        } else if (ex.getClass().isAssignableFrom(ServiceUnavailableException.class)) {
            bodyOfResponse = "The server is currently unable to handle the request due to a temporary condition which will be alleviated after some delay. You can choose to resend the request again.";

            return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE, request);
        } else if (ex.getClass().isAssignableFrom(TooManyRequestsException.class)) {
            bodyOfResponse = "Rate limiting has been applied.";

            return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.TOO_MANY_REQUESTS, request);
        } else if (ex.getClass().isAssignableFrom(UnauthorizedException.class)) {
            bodyOfResponse = "The request requires user authorization or, if the request included authorization credentials, authorization has been refused for those credentials.";

            return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
        }

        //Return a Spotify API exception not found in this method which should not be possible
        bodyOfResponse = "An unknown Spotify API exception has occured";
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);

    }
}
