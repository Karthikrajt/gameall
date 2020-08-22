package com.siragu.gameall.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a_man on 09-02-2018.
 */

public class ProfileFollowResponse {
    @SerializedName("success")
    @Expose
    private Integer success;

    public Integer getSuccess() {
        return success;
    }

    public boolean isFollowed() {
        return success == 2;
    }

    public boolean isUnFollowed() {
        return success == 1;
    }
}
