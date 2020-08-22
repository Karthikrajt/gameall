package com.siragu.gameall.model;

public class ChatUser {
    private String userPlayerId;
    private boolean online;

    public ChatUser() {
    }

    public String getUserPlayerId() {
        return userPlayerId;
    }

    public void setUserPlayerId(String userPlayerId) {
        this.userPlayerId = userPlayerId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
