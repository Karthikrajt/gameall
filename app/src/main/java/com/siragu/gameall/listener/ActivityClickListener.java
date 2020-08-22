package com.siragu.gameall.listener;

import com.siragu.gameall.model.Activity;
import com.siragu.gameall.network.request.FollowRequestReview;

public interface ActivityClickListener {
    void onActivityClick(Activity activity, int pos);

    void onActivityFollowRequestClick(Activity activity, FollowRequestReview followRequestReview, int pos);
}
