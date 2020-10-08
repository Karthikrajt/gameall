package com.siragu.CMex.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.siragu.CMex.R;
import com.siragu.CMex.network.response.UserResponse;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.SharedPreferenceUtil;

import java.util.ArrayList;

/**
 * Created by laxmikant on 3/29/2018.
 */

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.GroceryViewHolder> {
    private ArrayList<UserResponse> dataList;
    private Context context;
    private StoryClickListener storyClickListener;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private String userMeImage;

    public StoriesAdapter(ArrayList<UserResponse> dataList, Context context, StoryClickListener storyClickListener) {
        this.dataList = dataList;
        this.context = context;
        this.storyClickListener = storyClickListener;
        sharedPreferenceUtil = new SharedPreferenceUtil(context);
        UserResponse userResponse = Helper.getLoggedInUser(sharedPreferenceUtil);
        userMeImage = userResponse != null ? userResponse.getImage() : "";
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroceryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_stories, parent, false));
    }

    @Override
    public void onBindViewHolder(GroceryViewHolder holder, int position) {
        holder.setData(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class GroceryViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView, addIcon;
        private TextView txtview;
        private ProgressBar storyProgress;

        public GroceryViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.idProductImage);
            txtview = view.findViewById(R.id.idProductName);
            addIcon = view.findViewById(R.id.addIcon);
            storyProgress = view.findViewById(R.id.storyProgress);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        if (dataList.get(pos).getId() == -1) {
                            storyClickListener.postStory();
                        } else {
                            storyClickListener.showStory(pos);
                        }
                    }
                }
            });
        }

        public void setData(UserResponse userResponse) {
            Glide.with(context)
                    .load(userResponse.getId() != -1 ? userResponse.getImage() : userMeImage)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(context, 8))).placeholder(R.drawable.ic_person_gray_24dp))
                    .into(imageView);
            txtview.setText(userResponse.getId() != -1 ? userResponse.getName() : context.getString(R.string.store_add));
            txtview.setSelected(true);
            addIcon.setVisibility(userResponse.getId() != -1 ? View.GONE : View.VISIBLE);
            storyProgress.setVisibility(userResponse.getId() != -1 ? View.GONE : userResponse.isStoryUpdateProgress() ? View.VISIBLE : View.GONE);

//            if (userResponse.getId() != -1) {
//                imageView.setBackgroundResource(0);
//            } else {
//                imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.trans_black_light));
//            }
        }
    }

    public interface StoryClickListener {
        void showStory(int pos);

        void postStory();
    }

}
