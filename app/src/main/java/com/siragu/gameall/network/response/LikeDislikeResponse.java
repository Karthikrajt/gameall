package com.siragu.gameall.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a_man on 11-12-2017.
 */

public class LikeDislikeResponse {
    @SerializedName("status")
    @Expose
    private Integer status;

    @SerializedName("id")
    @Expose
    private String id;

    public String getId() {
        return id;
    }

    public Integer getStatus() {
        return status;
    }
}
