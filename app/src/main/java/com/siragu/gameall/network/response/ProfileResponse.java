package com.siragu.gameall.network.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a_man on 12-12-2017.
 */
public class ProfileResponse extends UserResponse implements Parcelable {
    @SerializedName("posts_count")
    @Expose
    private Integer posts_count;
    @SerializedName("like_count")
    @Expose
    private Integer like_count;
    @SerializedName("dislike_count")
    @Expose
    private Integer dislike_count;
    @SerializedName("comment_count")
    @Expose
    private Integer comment_count;

    public ProfileResponse() {
    }

    protected ProfileResponse(Parcel in) {
        super(in);
        if (in.readByte() == 0) {
            posts_count = null;
        } else {
            posts_count = in.readInt();
        }
        if (in.readByte() == 0) {
            like_count = null;
        } else {
            like_count = in.readInt();
        }
        if (in.readByte() == 0) {
            dislike_count = null;
        } else {
            dislike_count = in.readInt();
        }
        if (in.readByte() == 0) {
            comment_count = null;
        } else {
            comment_count = in.readInt();
        }
    }

    public static final Creator<ProfileResponse> CREATOR = new Creator<ProfileResponse>() {
        @Override
        public ProfileResponse createFromParcel(Parcel in) {
            return new ProfileResponse(in);
        }

        @Override
        public ProfileResponse[] newArray(int size) {
            return new ProfileResponse[size];
        }
    };

    public Integer getPosts_count() {
        return posts_count == null ? 0 : posts_count;
    }

    public void setPosts_count(Integer posts_count) {
        this.posts_count = posts_count;
    }

    public Integer getLike_count() {
        return like_count;
    }

    public void setLike_count(Integer like_count) {
        this.like_count = like_count;
    }

    public Integer getDislike_count() {
        return dislike_count;
    }

    public void setDislike_count(Integer dislike_count) {
        this.dislike_count = dislike_count;
    }

    public Integer getComment_count() {
        return comment_count;
    }

    public void setComment_count(Integer comment_count) {
        this.comment_count = comment_count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (posts_count == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(posts_count);
        }
        if (like_count == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(like_count);
        }
        if (dislike_count == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(dislike_count);
        }
        if (comment_count == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(comment_count);
        }
    }
}
