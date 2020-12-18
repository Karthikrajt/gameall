
package com.siragu.CMex.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Post implements Parcelable {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("user_profile_id")
    @Expose
    private UserMeta user_profile_id;
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
    @SerializedName("like_count")
    @Expose
    private Long likeCount;
    @SerializedName("dislike_count")
    @Expose
    private Long dislikeCount;
    @SerializedName("comment_count")
    @Expose
    private Long commentCount;
    @SerializedName("share_count")
    @Expose
    private Long shareCount;
    @SerializedName("liked")
    @Expose
    private Integer liked;
    @SerializedName("disliked")
    @Expose
    private Integer disliked;
    @SerializedName("commented")
    @Expose
    private Integer commented;
    @SerializedName("deleted_at")
    @Expose
    private String deletedAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("text_location_on_video")
    @Expose
    private Integer videoTextLocation;
    @SerializedName("video_thumbnail_url")
    @Expose
    private String videoThumbnailUrl;

    public Post(String id) {
        this.id = id;
    }

    public Post() {

    }

    protected Post(Parcel in) {
        id = in.readString();
        user_profile_id = in.readParcelable(UserMeta.class.getClassLoader());
        text = in.readString();
        title = in.readString();
        media_url = in.readString();
        type = in.readString();
        if (in.readByte() == 0) {
            likeCount = null;
        } else {
            likeCount = in.readLong();
        }
        if (in.readByte() == 0) {
            dislikeCount = null;
        } else {
            dislikeCount = in.readLong();
        }
        if (in.readByte() == 0) {
            commentCount = null;
        } else {
            commentCount = in.readLong();
        }
        if (in.readByte() == 0) {
            shareCount = null;
        } else {
            shareCount = in.readLong();
        }
        if (in.readByte() == 0) {
            liked = null;
        } else {
            liked = in.readInt();
        }
        if (in.readByte() == 0) {
            disliked = null;
        } else {
            disliked = in.readInt();
        }
        if (in.readByte() == 0) {
            commented = null;
        } else {
            commented = in.readInt();
        }
        deletedAt = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
        if (in.readByte() == 0) {
            videoTextLocation = null;
        } else {
            videoTextLocation = in.readInt();
        }
        videoThumbnailUrl = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Post(String postId, String title, String text, String attachment, String type, Integer videoTextLocation, String videoThumbnailUrl, UserMeta userMeta) {
        this.id = postId;
        this.text = text;
        this.title = title;
        this.media_url = attachment;
        if (this.media_url == null)
            this.media_url = "";
        this.type = type;
        this.likeCount = 0L;
        this.dislikeCount = 0L;
        this.commentCount = 0L;
        this.liked = 0;
        this.disliked = 0;
        this.commented = 0;
        this.user_profile_id = userMeta;
        //this.postedBy = new PostedBy(12, "52463626", "m");
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date(Calendar.getInstance().getTimeInMillis()));
        this.videoTextLocation = videoTextLocation;
        this.videoThumbnailUrl = videoThumbnailUrl;
        if (this.videoThumbnailUrl == null)
            this.videoThumbnailUrl = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserMeta getUserMetaData() {
        return user_profile_id;
    }

    public String getMedia_url() {
        return media_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(Long dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getShareCount() {
        return shareCount;
    }

    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text The text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }

    public Integer getLiked() {
        return liked;
    }

    public void setLiked(Integer liked) {
        this.liked = liked;
    }

    public Integer getDisliked() {
        return disliked;
    }

    public void setDisliked(Integer disliked) {
        this.disliked = disliked;
    }

    public Integer getCommented() {
        return commented;
    }

    public void setCommented(Integer commented) {
        this.commented = commented;
    }

    public Integer getVideoTextLocation() {
        return videoTextLocation;
    }

    public void setVideoTextLocation(Integer videoTextLocation) {
        this.videoTextLocation = videoTextLocation;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public void setVideoThumbnailUrl(String videoThumbnailUrl) {
        this.videoThumbnailUrl = videoThumbnailUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        } else {
            Post getPost = (Post) obj;
            return getId().equals(getPost.id);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeParcelable(user_profile_id, i);
        parcel.writeString(text);
        parcel.writeString(title);
        parcel.writeString(media_url);
        parcel.writeString(type);
        if (likeCount == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(likeCount);
        }
        if (dislikeCount == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(dislikeCount);
        }
        if (commentCount == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(commentCount);
        }
        if (shareCount == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(shareCount);
        }
        if (liked == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(liked);
        }
        if (disliked == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(disliked);
        }
        if (commented == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(commented);
        }
        parcel.writeString(deletedAt);
        parcel.writeString(createdAt);
        parcel.writeString(updatedAt);
        if (videoTextLocation == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(videoTextLocation);
        }
        parcel.writeString(videoThumbnailUrl);
    }
}
