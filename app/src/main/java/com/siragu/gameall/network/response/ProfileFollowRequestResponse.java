package com.siragu.gameall.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileFollowRequestResponse {
    @SerializedName("follow_request")
    @Expose
    private Boolean follow_request;

    public Boolean getFollow_request() {
        return follow_request != null ? follow_request : false;
    }
}
