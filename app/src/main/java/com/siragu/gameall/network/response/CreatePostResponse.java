package com.siragu.gameall.network.response;

import com.siragu.gameall.model.UserMeta;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a_man on 05-12-2017.
 */

public class CreatePostResponse {
    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("media_url")
    @Expose
    private String media_url;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("user_profile_id")
    @Expose
    private UserMeta user_profile_id;

    @SerializedName("id")
    @Expose
    private String id;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getMedia_url() {
        return media_url;
    }

    public String getType() {
        return type;
    }

    public UserMeta getUser_profile_id() {
        return user_profile_id;
    }
}
