package com.mbmusic.hubserver.SpotifyUser;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.mbmusic.hubserver.Common.DataAccess;
import com.mbmusic.hubserver.Connections.Exceptions.InvalidSessionIdException;
import com.mbmusic.hubserver.SpotifyUser.Models.GetSpotifyUserInfoResponse;

import se.michaelthelin.spotify.model_objects.specification.Image;

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

                //Get the smallest profile pic
                var images = spotifyUser.getImages();
                Optional<Image> profilePic = Arrays.stream(images)
                    .min((img1, img2) -> {
                        return (img1.getHeight() * img1.getWidth()) - (img2.getHeight() * img2.getWidth());
                    });

                if (profilePic.isPresent()) {
                    userInfo.setImageUrl(profilePic.get().getUrl());
                    userInfo.setImageHeight(profilePic.get().getHeight());
                    userInfo.setImageWidth(profilePic.get().getWidth());
                }

                return userInfo;
            });
    }
}
