package com.siragu.CMex.network.request;

/**
 * Created by a_man on 05-12-2017.
 */

public class CreatePostRequest {
    private String title, text, type, media_url, video_thumbnail_url;
    private boolean is_story;

    public CreatePostRequest(String title, String text, String type, String media_url) {
        this.title = title;
        this.text = text;
        this.type = type;
        this.media_url = media_url;
    }

    public CreatePostRequest(String title, String text, String type) {
        this.title = title;
        this.text = text;
        this.type = type;
    }

    public CreatePostRequest(String title, String text, String type, String media_url, String videoThumUrl) {
        this.title = title;
        this.text = text;
        this.type = type;
        this.media_url = media_url;
        this.video_thumbnail_url = videoThumUrl;
    }

    public void setIs_story(boolean is_story) {
        this.is_story = is_story;
    }
}
