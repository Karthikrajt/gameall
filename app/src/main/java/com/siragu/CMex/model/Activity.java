package com.siragu.CMex.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.siragu.CMex.network.response.FollowRequest;

/**
 * Created by a_man on 23-12-2017.
 */

public class Activity {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("user_profile_id")
    @Expose
    private UserMeta user_profile_id;
    @SerializedName("post_id")
    @Expose
    private String post_id;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("deleted_at")
    @Expose
    private String deletedAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    private boolean inProgress;
    private FollowRequest followRequest;

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public Activity(String id) {
        this.id = id;
    }

    public Activity(FollowRequest fr) {
        this.followRequest = fr;
    }

    public Activity(String id, UserMeta user_profile_id, String post_id, String type) {
        this.id = id;
        this.user_profile_id = user_profile_id;
        this.post_id = post_id;
        this.type = type;
    }

    public Activity() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Activity activity = (Activity) o;

        return id.equals(activity.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public FollowRequest getFollowRequest() {
        return followRequest;
    }

    public UserMeta getUser_profile_id() {
        return user_profile_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public String getType() {
        return type;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
