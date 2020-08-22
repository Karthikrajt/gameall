package com.siragu.gameall.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileFollowRequestReviewResponse {
    @SerializedName("follow")
    @Expose
    private int follow;

    public int getFollow() {
        return follow;
    }
}
