package com.siragu.CMex.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("text")
    @Expose
    private String text;

    @Expose
    @SerializedName("audio_url")
    private String audio_url;

    @Expose
    @SerializedName("comment_type")
    private String comment_type;

    @SerializedName("post_id")
    @Expose
    private String post_id;
    @SerializedName("user_profile_id")
    @Expose
    private UserMeta userMeta;
    @SerializedName("like_count")
    @Expose
    private Integer likeCount;
    @SerializedName("dislike_count")
    @Expose
    private Integer dislikeCount;
    @SerializedName("liked")
    @Expose
    private Integer liked;
    @SerializedName("disliked")
    @Expose
    private Integer disliked;
    @SerializedName("updated_at")
    @Expose
    private String updated_at;
    @SerializedName("created_at")
    @Expose
    private String created_at;
    @SerializedName("deleted_at")
    @Expose
    private String deleted_at;

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }
    public String getComment_type() {
        return comment_type;
    }
    public String getAudio_url() {
        return audio_url;
    }
    public String getPost_id() {
        return post_id;
    }

    public UserMeta getUserMeta() {
        return userMeta;
    }

    public Integer getLikeCount() {
        if (likeCount == null)
            likeCount = 0;
        return likeCount;
    }

    public Integer getDislikeCount() {
        if (dislikeCount == null)
            dislikeCount = 0;
        return dislikeCount;
    }

    public Integer getLiked() {
        if (liked == null)
            liked = 0;
        return liked;
    }

    public Integer getDisliked() {
        if (disliked == null)
            disliked = 0;
        return disliked;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public void setDislikeCount(Integer dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public void setLiked(Integer liked) {
        this.liked = liked;
    }

    public void setDisliked(Integer disliked) {
        this.disliked = disliked;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }
}
