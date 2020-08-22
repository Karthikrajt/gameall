package com.siragu.gameall.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a_man on 11-12-2017.
 */

public class UserMeta implements Parcelable {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("image")
    @Expose
    private String image;

    public UserMeta() {
    }

    public UserMeta(Integer id, String gender, String name) {
        this.id = id;
        this.gender = gender;
        this.name = name;
    }

    protected UserMeta(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        gender = in.readString();
        name = in.readString();
        image = in.readString();
    }

    public static final Creator<UserMeta> CREATOR = new Creator<UserMeta>() {
        @Override
        public UserMeta createFromParcel(Parcel in) {
            return new UserMeta(in);
        }

        @Override
        public UserMeta[] newArray(int size) {
            return new UserMeta[size];
        }
    };

    public Integer getId() {
        if (id == null)
            id = -1;
        return id;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        if (image == null)
            return "";
        else
            return image;
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
        dest.writeString(gender);
        dest.writeString(name);
        dest.writeString(image);
    }
}
