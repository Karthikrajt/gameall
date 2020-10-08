package com.siragu.CMex.network.request;

/**
 * Created by a_man on 11-12-2017.
 */

public class CreateCommentRequest {
    private String text;

    private String audio_url ="";

    private  String comment_type = "1";

    public CreateCommentRequest(String text,String audio_url, String comment_type) {
        this.text = text;
        this.audio_url = audio_url;
        this.comment_type = comment_type;
    }
}
