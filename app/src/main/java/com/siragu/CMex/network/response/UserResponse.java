package com.siragu.CMex.network.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a_man on 05-12-2017.
 */

public class UserResponse implements Parcelable {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("gender")
    @Expose
    private String gender;

    @SerializedName("updated_at")
    @Expose
    private String updated_at;
    @SerializedName("created_at")
    @Expose
    private String created_at;

    @SerializedName("notification_on_like")
    @Expose
    private Boolean notification_on_like;
    @SerializedName("notification_on_dislike")
    @Expose
    private Boolean notification_on_dislike;
    @SerializedName("notification_on_comment")
    @Expose
    private Boolean notification_on_comment;

    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("is_admin")
    @Expose
    private Integer is_admin;
    @SerializedName("is_paid")
    @Expose
    private Integer is_paid;
    @SerializedName("is_private")
    @Expose
    private Integer is_private;
    @SerializedName("is_following")
    @Expose
    private Integer is_following;
    @SerializedName("following_count")
    @Expose
    private Integer following_count;
    @SerializedName("followers_count")
    @Expose
    private Integer followers_count;
    @SerializedName("is_follow_requested")
    @Expose
    private Integer is_follow_requested;

    @Exclude
    private boolean storyUpdateProgress;

    public UserResponse() {
    }

    public UserResponse(Integer id, String imageUrl, String name) {
        this.id = id;
        this.image = imageUrl;
        this.name = name;
    }

    protected UserResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        userId = in.readString();
        name = in.readString();
        gender = in.readString();
        updated_at = in.readString();
        created_at = in.readString();
        byte tmpNotification_on_like = in.readByte();
        notification_on_like = tmpNotification_on_like == 0 ? null : tmpNotification_on_like == 1;
        byte tmpNotification_on_dislike = in.readByte();
        notification_on_dislike = tmpNotification_on_dislike == 0 ? null : tmpNotification_on_dislike == 1;
        byte tmpNotification_on_comment = in.readByte();
        notification_on_comment = tmpNotification_on_comment == 0 ? null : tmpNotification_on_comment == 1;
        image = in.readString();
        if (in.readByte() == 0) {
            is_admin = null;
        } else {
            is_admin = in.readInt();
        }
        if (in.readByte() == 0) {
            is_paid = null;
        } else {
            is_paid = in.readInt();
        }
        if (in.readByte() == 0) {
            is_private = null;
        } else {
            is_private = in.readInt();
        }
        if (in.readByte() == 0) {
            is_following = null;
        } else {
            is_following = in.readInt();
        }
        if (in.readByte() == 0) {
            following_count = null;
        } else {
            following_count = in.readInt();
        }
        if (in.readByte() == 0) {
            followers_count = null;
        } else {
            followers_count = in.readInt();
        }
        if (in.readByte() == 0) {
            is_follow_requested = null;
        } else {
            is_follow_requested = in.readInt();
        }

        storyUpdateProgress = in.readByte() != 0;
    }

    public Integer getIs_private() {
        return is_private != null ? is_private : 0;
    }

    public void setIs_private(Integer is_private) {
        this.is_private = is_private;
    }

    public static final Creator<UserResponse> CREATOR = new Creator<UserResponse>() {
        @Override
        public UserResponse createFromParcel(Parcel in) {
            return new UserResponse(in);
        }

        @Override
        public UserResponse[] newArray(int size) {
            return new UserResponse[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserResponse that = (UserResponse) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    public boolean isStoryUpdateProgress() {
        return storyUpdateProgress;
    }

    public void setStoryUpdateProgress(boolean storyUpdateProgress) {
        this.storyUpdateProgress = storyUpdateProgress;
    }

    public Integer getIs_follow_requested() {
        return is_follow_requested == null ? 0 : is_follow_requested;
    }

    public void setIs_follow_requested(Integer is_follow_requested) {
        this.is_follow_requested = is_follow_requested;
    }

    public String getImage() {
        return image;
    }

    public Integer getIs_admin() {
        return is_admin;
    }

    public Integer getIs_following() {
        return is_following;
    }

    public void setIs_following(Integer is_following) {
        this.is_following = is_following;
    }

    public Integer getFollowing_count() {
        return following_count == null ? 0 : following_count;
    }

    public Integer getFollowers_count() {
        return followers_count == null ? 0 : followers_count;
    }

    public Boolean getNotification_on_like() {
        if (notification_on_like == null) notification_on_like = false;
        return notification_on_like;
    }

    public Boolean getNotification_on_dislike() {
        if (notification_on_dislike == null) notification_on_dislike = false;
        return notification_on_dislike;
    }

    public Boolean getNotification_on_comment() {
        if (notification_on_comment == null) notification_on_comment = false;
        return notification_on_comment;
    }

    public String getName() {
        if (name == null)
            return "";
        else
            return name;
    }

    public String getGender() {
        if (gender == null) gender = "m";
        return gender;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public Integer getId() {
        if (id == null)
            id = -1;
        return id;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setNotification_on_like(Boolean notification_on_like) {
        this.notification_on_like = notification_on_like;
    }

    public void setNotification_on_dislike(Boolean notification_on_dislike) {
        this.notification_on_dislike = notification_on_dislike;
    }

    public void setNotification_on_comment(Boolean notification_on_comment) {
        this.notification_on_comment = notification_on_comment;
    }

    public void setIs_admin(Integer is_admin) {
        this.is_admin = is_admin;
    }

    public void setIs_paid(Integer is_paid) {
        this.is_paid = is_paid;
    }

    public void setFollowing_count(Integer following_count) {
        this.following_count = following_count;
    }

    public void setFollowers_count(Integer followers_count) {
        this.followers_count = followers_count;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isMale() {
        return gender.equals("m");
    }

    public boolean isFeMale() {
        return gender.equals("f");
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(userId);
        dest.writeString(name);
        dest.writeString(gender);
        dest.writeString(updated_at);
        dest.writeString(created_at);
        dest.writeByte((byte) (notification_on_like == null ? 0 : notification_on_like ? 1 : 2));
        dest.writeByte((byte) (notification_on_dislike == null ? 0 : notification_on_dislike ? 1 : 2));
        dest.writeByte((byte) (notification_on_comment == null ? 0 : notification_on_comment ? 1 : 2));
        dest.writeString(image);
        if (is_admin == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(is_admin);
        }
        if (is_paid == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(is_paid);
        }
        if (is_private == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(is_private);
        }
        if (is_following == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(is_following);
        }
        if (following_count == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(following_count);
        }
        if (followers_count == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(followers_count);
        }
        if (is_follow_requested == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(is_follow_requested);
        }
        dest.writeByte((byte) (storyUpdateProgress ? 1 : 0));
    }
}
