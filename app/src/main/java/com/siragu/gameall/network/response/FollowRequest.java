package com.siragu.gameall.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FollowRequest {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("requested_by_profile_id")
    @Expose
    private int requested_by_profile_id;
    @SerializedName("user_profile_id")
    @Expose
    private int user_profile_id;
    @SerializedName("created_at")
    @Expose
    private String created_at;
    @SerializedName("profile")
    @Expose
    private UserResponse profile;
    @SerializedName("requested_by")
    @Expose
    private UserResponse requested_by;

    public int getId() {
        return id;
    }

    public int getRequested_by_profile_id() {
        return requested_by_profile_id;
    }

    public int getUser_profile_id() {
        return user_profile_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public UserResponse getProfile() {
        return profile;
    }

    public UserResponse getRequested_by() {
        return requested_by;
    }
}
