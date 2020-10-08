package com.siragu.CMex.network.request;

/**
 * Created by a_man on 05-12-2017.
 */

public class UserUpdateRequest {
    private String gender, name, fcm_registration_id, image;
    private boolean notification_on_like, notification_on_dislike, notification_on_comment;
    private int is_private = 0;

    public UserUpdateRequest(String gender, String fcm_registration_id, boolean notification_on_like, boolean notification_on_dislike, boolean notification_on_comment, int isPrivate) {
        this.gender = gender;
        this.fcm_registration_id = fcm_registration_id;
        this.notification_on_like = notification_on_like;
        this.notification_on_dislike = notification_on_dislike;
        this.notification_on_comment = notification_on_comment;
        this.is_private = isPrivate;
    }

    public UserUpdateRequest(String gender, String name, String image, String fcm_registration_id, boolean notification_on_like, boolean notification_on_dislike, boolean notification_on_comment, int isPrivate) {
        this.gender = gender;
        this.name = name;
        this.image = image;
        this.fcm_registration_id = fcm_registration_id;
        this.notification_on_like = notification_on_like;
        this.notification_on_dislike = notification_on_dislike;
        this.notification_on_comment = notification_on_comment;
        this.is_private = isPrivate;
    }
}
