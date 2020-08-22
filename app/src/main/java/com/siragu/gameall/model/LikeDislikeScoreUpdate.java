package com.siragu.gameall.model;

/**
 * Created by a_man on 11-12-2017.
 */

public class LikeDislikeScoreUpdate {
    private int like, dislike;
    private boolean inProgress;

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
}
