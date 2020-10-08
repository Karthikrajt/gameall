package com.siragu.CMex.listener;

import com.siragu.CMex.model.Activity;
import com.siragu.CMex.network.request.FollowRequestReview;

public interface ActivityClickListener {
    void onActivityClick(Activity activity, int pos);

    void onActivityFollowRequestClick(Activity activity, FollowRequestReview followRequestReview, int pos);
}
