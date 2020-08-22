package com.siragu.gameall.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.siragu.gameall.listener.ActivityClickListener;
import com.siragu.gameall.model.Activity;
import com.siragu.gameall.R;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.network.request.FollowRequestReview;
import com.siragu.gameall.util.EasyRecyclerViewAdapter;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;

/**
 * Created by mayank on 9/7/16.
 */
public class NotificationRecyclerAdapter extends EasyRecyclerViewAdapter<Activity> {
    private Context context;
    private DrService foxyService;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private ActivityClickListener activityClickListener;

    public NotificationRecyclerAdapter(Context context, ActivityClickListener activityClickListener) {
        this.context = context;
        this.foxyService = ApiUtils.getClient().create(DrService.class);
        this.sharedPreferenceUtil = new SharedPreferenceUtil(context);
        this.activityClickListener = activityClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemView(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notification, parent, false));
    }

    @Override
    public void onBindItemView(RecyclerView.ViewHolder commonHolder, Activity currActivity, int position) {
        ViewHolder holder = (ViewHolder) commonHolder;
        holder.followAction.setVisibility(currActivity.getFollowRequest() == null ? View.GONE : View.VISIBLE);
        if (currActivity.getFollowRequest() == null) {
            String action = "";
            switch (currActivity.getType()) {
                case "like":
                    action = context.getString(R.string.someone_liked);
                    break;
                case "comment":
                    action = context.getString(R.string.someone_commented);
                    break;
            }

            String statusText = currActivity.getUser_profile_id().getName() + " " + action;

            holder.status.setText(statusText);
            holder.time.setText(Helper.timeDiff(currActivity.getCreatedAt()));
//            Glide.with(context)
//                    .load(currActivity.getUser_profile_id().getImage())
//                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(context, 8))).placeholder(R.drawable.ic_person_gray_24dp))
//                    .into(holder.descImg);

            Glide.with(context).load(currActivity.getUser_profile_id().getImage())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(context, 5))).override(Helper.dp2px(context, 38), Helper.dp2px(context, 38)).placeholder(R.drawable.ic_person_gray_24dp))
                    .into(holder.descImg);
        } else {
            holder.status.setText(currActivity.getFollowRequest().getRequested_by().getName());
            holder.time.setText(context.getString(R.string.follow_request) + ": " + Helper.timeDiff(currActivity.getFollowRequest().getCreated_at()));
//            Glide.with(context)
//                    .load(currActivity.getFollowRequest().getRequested_by().getImage())
//                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(context, 8))).placeholder(R.drawable.ic_person_gray_24dp))
//                    .into(holder.descImg);
            Glide.with(context).load(currActivity.getFollowRequest().getRequested_by().getImage())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(context, 5))).override(Helper.dp2px(context, 38), Helper.dp2px(context, 38)).placeholder(R.drawable.ic_person_gray_24dp))
                    .into(holder.descImg);
        }
        //holder.progressNotification.setVisibility(currActivity.isInProgress() ? View.VISIBLE : View.INVISIBLE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView descImg;
        TextView status;
        TextView time;
        LinearLayout followAction;
        //ProgressBar progressNotification;

        public ViewHolder(View itemView) {
            super(itemView);
            descImg = itemView.findViewById(R.id.list_item_notification_desc_img);
            status = itemView.findViewById(R.id.list_item_notification_status);
            time = itemView.findViewById(R.id.list_item_notification_time);
            followAction = itemView.findViewById(R.id.followAction);
            //progressNotification = itemView.findViewById(R.id.progressNotification);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDetailHomeItemFragment();
                }
            });
            itemView.findViewById(R.id.followActionAccept).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reviewFollowRequest(new FollowRequestReview(true));
                }
            });
            itemView.findViewById(R.id.followActionReject).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reviewFollowRequest(new FollowRequestReview(false));
                }
            });
        }

        void reviewFollowRequest(FollowRequestReview followRequestReview) {
            int pos = getAdapterPosition();
            if (pos != -1) {
                activityClickListener.onActivityFollowRequestClick(getItem(pos), followRequestReview, pos);
            }
        }

        void openDetailHomeItemFragment() {
            int pos = getAdapterPosition();
            if (pos != -1) {
                activityClickListener.onActivityClick(getItem(pos), pos);
            }
        }

    }
}
