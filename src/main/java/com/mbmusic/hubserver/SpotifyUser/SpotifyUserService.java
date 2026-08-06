package com.mbmusic.hubserver.SpotifyUser;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.mbmusic.hubserver.Common.DataAccess;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.SpotifyUser.Models.GetSpotifyUserInfoResponse;

@Service
public class SpotifyUserService {

    //#region Members

    private DataAccess da;

    //#endregion

    //#region Constructor

    public SpotifyUserService(DataAccess da) {
        this.da = da;
    }

    //#endregion

    /**
     * This method retrieves essential information on the current Spotify user
     * @return A {@link GetSpotifyUserInfoResponse} object that contains the information on the current user
     */
    public CompletableFuture<GetSpotifyUserInfoResponse> getSpotifyInfo(String sessionId)
        throws InvalidSessionIdException {

        return da.getSpotifyApiGatewayAsync(sessionId)
            .thenCompose(spotifyApiGateway -> spotifyApiGateway.getCurrentSpotifyUserProfile())
            .thenApply(spotifyUser -> {
                GetSpotifyUserInfoResponse userInfo = new GetSpotifyUserInfoResponse();

                userInfo.setDisplayName(spotifyUser.getDisplayName());
                userInfo.setSubscriptionLevel(spotifyUser.getProduct().getType());

                var images = spotifyUser.getImages();
                if (images != null && images.length > 0) {
                    userInfo.setImageUrl(images[0].getUrl());
                    userInfo.setImageHeight(images[0].getHeight());
                    userInfo.setImageWidth(images[0].getWidth());
                }

                return userInfo;
            });
    }
}
