package com.siragu.gameall.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.siragu.gameall.network.response.ProfileResponse;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Helper;

/**
 * Created by a_man on 1/10/2017.
 */

public class Chat implements Parcelable {
    private String chatId, myId, dateTimeStamp, timeDiff, lastMessage, chatName, chatImage, chatStatus, lastMessageId;
    private boolean isGroup, isRead;

    public Chat(UserResponse userMe, ProfileResponse userProfile) {
        this.chatId = userProfile.getId().toString();
        this.chatImage = userProfile.getImage();
        this.chatName = userProfile.getName();
        this.chatStatus = "user email";
        this.myId = userMe.getId().toString();
    }

    public Chat(Message msg, boolean isMeSender) {
        this.chatId = isMeSender ? msg.getRecipientId() : msg.getSenderId();
        this.myId = isMeSender ? msg.getSenderId() : msg.getRecipientId();
        this.chatName = isMeSender ? msg.getRecipientName() : msg.getSenderName();
        this.chatImage = isMeSender ? msg.getRecipientImage() : msg.getSenderImage();
        this.chatStatus = isMeSender ? msg.getRecipientStatus() : msg.getSenderStatus();
        this.dateTimeStamp = msg.getDateTimeStamp();
        this.lastMessage = msg.getBody();
        this.lastMessageId = msg.getId();
    }

    protected Chat(Parcel in) {
        chatId = in.readString();
        myId = in.readString();
        dateTimeStamp = in.readString();
        timeDiff = in.readString();
        lastMessage = in.readString();
        chatName = in.readString();
        chatImage = in.readString();
        chatStatus = in.readString();
        lastMessageId = in.readString();
        isGroup = in.readByte() != 0;
        isRead = in.readByte() != 0;
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public String getLastMessageId() {
        return lastMessageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public String getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(String dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    public String getTimeDiff() {
        this.timeDiff = Helper.timeDiff(Long.valueOf(this.dateTimeStamp)).toString();
        return timeDiff;
    }

    public void setTimeDiff(String timeDiff) {
        this.timeDiff = timeDiff;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getChatImage() {
        return chatImage;
    }

    public void setChatImage(String chatImage) {
        this.chatImage = chatImage;
    }

    public String getChatStatus() {
        return chatStatus;
    }

    public void setChatStatus(String chatStatus) {
        this.chatStatus = chatStatus;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chatId);
        dest.writeString(myId);
        dest.writeString(dateTimeStamp);
        dest.writeString(timeDiff);
        dest.writeString(lastMessage);
        dest.writeString(chatName);
        dest.writeString(chatImage);
        dest.writeString(chatStatus);
        dest.writeString(lastMessageId);
        dest.writeByte((byte) (isGroup ? 1 : 0));
        dest.writeByte((byte) (isRead ? 1 : 0));
    }
}