package com.mbmusic.hubserver.SpotifyUser.Models;

public class GetSpotifyUserInfoResponse {

    private String displayName;

    private String subscriptionLevel;

    private int imageHeight;

    private int imageWidth;

    private String imageUrl;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSubscriptionLevel() {
        return subscriptionLevel;
    }

    public void setSubscriptionLevel(String subscriptionLevel) {
        this.subscriptionLevel = subscriptionLevel;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
