package com.mbmusic.hubserver.SpotifyUser;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mbmusic.hubserver.Connections.ConnectionUtils;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.SpotifyUser.Models.GetSpotifyUserInfoResponse;

import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("spotify-users")
public class SpotifyUserController {

    //#region Members

    private SpotifyUserService spotifyUserService;

    //#endregion

    //#region Constructor

    public SpotifyUserController(SpotifyUserService spotifyUserService) {
        this.spotifyUserService = spotifyUserService;
    }

    //#endregion

    //#region Methods

    public CompletableFuture<GetSpotifyUserInfoResponse> getSpotifyUserInfo(
        @CookieValue(name = ConnectionUtils.SPOTIFY_COOKIE_NAME) Cookie sessionIdCookie
    ) throws InvalidSessionIdException {

        if (sessionIdCookie.getValue() == null || sessionIdCookie.getValue() == "")
            throw new InvalidSessionIdException("Invalid Session ID");

        return spotifyUserService.getSpotifyInfo(sessionIdCookie.getValue());

    }

    //#endregion

}
