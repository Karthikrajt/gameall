package com.siragu.gameall.adapter;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.siragu.gameall.activity.UserProfileDetailActivity;
import com.siragu.gameall.model.Comment;
import com.siragu.gameall.R;
import com.siragu.gameall.model.UserMeta;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.EasyRecyclerViewAdapter;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.siragu.gameall.util.SpringAnimationHelper;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.response.LikeDislikeResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsRecyclerAdapter extends EasyRecyclerViewAdapter<Comment> {
    private Context context;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private DrService foxyService;

    public CommentsRecyclerAdapter(Context context) {
        this.context = context;
        sharedPreferenceUtil = new SharedPreferenceUtil(context);
        foxyService = ApiUtils.getClient().create(DrService.class);
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemView(ViewGroup parent, int viewType) {
        return new CommentViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_comment, parent, false));
    }

    @Override
    public void onBindItemView(RecyclerView.ViewHolder commonHolder, Comment currComment, int position) {
        CommentViewHolder holder = (CommentViewHolder) commonHolder;
        String dateOfPost = currComment.getCreated_at();
        holder.commentText.setText(currComment.getText());

        if (currComment.getUserMeta() != null) {
            Glide.with(context).load(currComment.getUserMeta().getImage())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(context, 5))).override(Helper.dp2px(context, 45), Helper.dp2px(context, 45)).placeholder(R.drawable.ic_person))
                    .into(holder.foxyImage);
            holder.userName.setText(currComment.getUserMeta().getName());
        }

        holder.postedTime.setText(Helper.timeDiff(dateOfPost));
        holder.like.setVisibility(View.GONE);
        holder.like.setImageDrawable(ContextCompat.getDrawable(context, currComment.getLiked() == 1 ? R.drawable.ic_thumb_up_accent_12dp : R.drawable.ic_thumb_up_gray_12dp));
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        //        TextView dislike;
        ImageView like;
        TextView commentText;
        ImageView foxyImage;
        TextView postedTime;

        CommentViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.list_item_name_view);
            //dislike=itemView.findViewById(R.id.list_item_comment_dislike);
            like = itemView.findViewById(R.id.list_item_comment_like);
            commentText = itemView.findViewById(R.id.list_item_comment_view);
            foxyImage = itemView.findViewById(R.id.list_item_comment_foxy_logo);
            postedTime = itemView.findViewById(R.id.list_item_comment_posting_time);
            itemView.findViewById(R.id.list_item_comment_like).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLikeClick(v);
                }
            });
            userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openProfile();
                }
            });
            postedTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openProfile();
                }
            });
            foxyImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openProfile();
                }
            });
//            itemView.findViewById(R.id.list_item_comment_dislike).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onDislikeClick(v);
//                }
//            });
        }

        private void openProfile() {
            int pos = getLayoutPosition();
            if (pos != -1) {
                UserMeta userMeta = getItem(pos).getUserMeta();
                if (userMeta != null) {
                    context.startActivity(UserProfileDetailActivity.newInstance(context, userMeta.getId().toString(), userMeta.getName(), userMeta.getImage()));
                }
            }
        }


        /**
         * Updates the data when dislike is clicked.<br/>
         * <b>-1</b> denotes dislike<br/>
         * <b>0</b> denotes neutral<br/>
         * <b>1</b> denotes like<br/>
         */
        void onDislikeClick(View view) {
            SpringAnimationHelper.performAnimation(view);
            final int position = getLayoutPosition();
            final Comment currComment = getItem(position);

            boolean alreadyDisliked = currComment.getDisliked() == 1;
            currComment.setDisliked(alreadyDisliked ? 0 : 1);
            currComment.setDislikeCount(alreadyDisliked ? (currComment.getDislikeCount() - 1) : (currComment.getDislikeCount() + 1));
            //setLikeDislikeData(currComment);
            //executeDislike(currComment.getId());
        }


        /**
         * Updates the data when like is clicked.<br/>
         * <b>-1</b> denotes dislike<br/>
         * <b>0</b> denotes neutral<br/>
         * <b>1</b> denotes like<br/>
         */
        void onLikeClick(View view) {
            SpringAnimationHelper.performAnimation(view);
            final int position = getLayoutPosition();
            final Comment currComment = getItem(position);

            boolean alreadyLiked = currComment.getLiked() == 1;
            currComment.setLiked(alreadyLiked ? 0 : 1);
            currComment.setLikeCount(alreadyLiked ? (currComment.getLikeCount() - 1) : (currComment.getLikeCount() + 1));
            notifyItemChanged(position);
            executeLike(currComment.getId());
        }

        void setDislikedView(boolean disliked) {
            //dislike.setTypeface(null, disliked ? Typeface.BOLD : Typeface.NORMAL);
            //dislike.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, disliked ? R.drawable.ic_dislike_red_18dp : R.drawable.ic_dislike_gray_18dp);
            if (disliked) {
                setLikedView(false);
            }
        }

        void setLikedView(boolean liked) {
            //like.setTypeface(null, liked ? Typeface.BOLD : Typeface.NORMAL);

            if (liked) {
                setDislikedView(false);
            }
        }

    }

    private void executeLike(String id) {
        foxyService.updateCommentLike(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), id).enqueue(new Callback<LikeDislikeResponse>() {
            @Override
            public void onResponse(Call<LikeDislikeResponse> call, Response<LikeDislikeResponse> response) {
            }

            @Override
            public void onFailure(Call<LikeDislikeResponse> call, Throwable t) {

            }
        });
    }

    private void executeDislike(String id) {
        foxyService.updateCommentDislike(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), id).enqueue(new Callback<LikeDislikeResponse>() {
            @Override
            public void onResponse(Call<LikeDislikeResponse> call, Response<LikeDislikeResponse> response) {
                if (response.isSuccessful()) {
                }
            }

            @Override
            public void onFailure(Call<LikeDislikeResponse> call, Throwable t) {

            }
        });
    }
}
