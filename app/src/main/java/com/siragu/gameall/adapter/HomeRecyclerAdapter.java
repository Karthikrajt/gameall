package com.siragu.gameall.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.siragu.gameall.activity.CommentActivity;
import com.siragu.gameall.activity.DetailHomeItemActivity;
import com.siragu.gameall.activity.StatusActivity;
import com.siragu.gameall.activity.UserProfileDetailActivity;
import com.siragu.gameall.fragment.CommentsFragment;
import com.siragu.gameall.fragment.HomeFeedsFragment;
import com.siragu.gameall.listener.OnCommentAddListener;
import com.siragu.gameall.listener.OnPopupMenuItemClickListener;
import com.siragu.gameall.model.Post;
import com.siragu.gameall.model.LikeDislikeScoreUpdate;
import com.siragu.gameall.R;
import com.siragu.gameall.model.UserMeta;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.EasyRecyclerViewAdapter;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.LinkTransformationMethod;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.siragu.gameall.util.SpringAnimationHelper;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.response.LikeDislikeResponse;
import com.google.gson.JsonObject;
import com.siragu.gameall.view.SquareVideoView;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by mayank on 9/7/16.
 */
public class HomeRecyclerAdapter extends EasyRecyclerViewAdapter<Post> {
    private Fragment fragment;
    private Context context;
    private HashMap<String, LikeDislikeScoreUpdate> likeDislikeUpdateMap;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private DrService foxyService;
    private ArrayList<UserResponse> storyUsers;

    public HomeRecyclerAdapter(Fragment fragment) {
        this.context = fragment.getContext();
        this.fragment = fragment;
        this.storyUsers = new ArrayList<>();
        likeDislikeUpdateMap = new HashMap<>();
        sharedPreferenceUtil = new SharedPreferenceUtil(fragment.getContext());
        foxyService = ApiUtils.getClient().create(DrService.class);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemsListSize() > position)
            return getItem(position).getId().equalsIgnoreCase("add") ? 2 : getItem(position).getId().equalsIgnoreCase("story") ? 3 : super.getItemViewType(position);
        else
            return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemView(ViewGroup parent, int viewType) {
       /* if (viewType == 2) {
            return new AddMobViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_add, parent, false));
        } else
           */

            if (viewType == 3) {
            return new StoriesContainerViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_story_container, parent, false));
        } else {
            return new HomeItemViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_home, parent, false));
        }
    }

    @Override
    public void onBindItemView(RecyclerView.ViewHolder commonHolder, Post currPost, int position) {
        if (commonHolder instanceof HomeItemViewHolder) {
            final HomeItemViewHolder holder = (HomeItemViewHolder) commonHolder;
            setPostData(holder, currPost);

            if (TextUtils.isEmpty(currPost.getTitle())) {
                holder.postTitle.setVisibility(View.GONE);
            } else {
                holder.postTitle.setVisibility(View.VISIBLE);
                holder.postTitle.setText(currPost.getTitle());
            }

            switch (currPost.getType()) {
                case "text":
                    holder.videoActionContainer.setVisibility(View.GONE);
                    holder.postText.setVisibility(View.VISIBLE);
                    holder.imageView.setVisibility(View.GONE);
                    holder.videoView.setVisibility(View.GONE);
                    holder.postText.setText(currPost.getText());
                    if (!TextUtils.isEmpty(currPost.getMedia_url())) {
                        holder.imageView.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .load(currPost.getMedia_url())
                                .apply(new RequestOptions().placeholder(R.drawable.placeholder).dontAnimate())
                                .into(holder.imageView);
                    }
                    break;
                case "image":
                    holder.videoActionContainer.setVisibility(View.GONE);
                    holder.postText.setVisibility(View.GONE);
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.videoView.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(currPost.getMedia_url())
                            .apply(new RequestOptions().placeholder(R.drawable.placeholder).dontAnimate())
                            .into(holder.imageView);
                    break;
                case "video":
                    holder.videoActionContainer.setVisibility(View.VISIBLE);
                    holder.postText.setVisibility(View.GONE);
                    holder.imageView.setVisibility(View.GONE);
                    holder.videoView.setVisibility(View.VISIBLE);

                    holder.videoProgress.setVisibility(View.VISIBLE);
                    holder.videoAction.setVisibility(View.GONE);

                    String videoUrl = currPost.getMedia_url();
                    holder.videoView.setVideoURI(Uri.parse(videoUrl));
                    holder.videoView.setVideoPath(videoUrl);
                    holder.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            holder.videoProgress.setVisibility(View.GONE);
                            return true;
                        }
                    });
                    holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            holder.videoProgress.setVisibility(View.GONE);
                            holder.videoAction.setVisibility(View.VISIBLE);
                            holder.videoView.seekTo(100);
                        }
                    });
                    holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            holder.videoAction.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_circle_outline_36dp));
                        }
                    });
                    holder.videoAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (holder.videoView.isPlaying()) {
                                holder.mediaStopPosition = holder.videoView.getCurrentPosition();
                                holder.videoView.pause();
                            } else {
                                holder.videoView.seekTo(holder.mediaStopPosition);
                                holder.videoView.start();
                            }
                            holder.videoAction.setImageDrawable(ContextCompat.getDrawable(context, holder.videoView.isPlaying() ? R.drawable.ic_pause_circle_outline_36dp : R.drawable.ic_play_circle_outline_36dp));
                        }
                    });
                    break;
            }
            if (currPost.getUserMetaData() != null) {
//                Glide.with(context)
//                        .load(currPost.getUserMetaData().getImage())
//                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(context, 8))).placeholder(R.drawable.ic_person_gray_24dp))
//                        .into(holder.foxyImage);

                Glide.with(context).load(currPost.getUserMetaData().getImage())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)).placeholder(R.drawable.placeholder))
                        .into(holder.foxyImage);
            }
        } else if (commonHolder instanceof StoriesContainerViewHolder) {
           ((StoriesContainerViewHolder) commonHolder).setData();
        }
    }


    /**
     * Binds the post data to the views in proper format.
     *
     * @param holder   {@link HomeItemViewHolder}
     * @param currPost the {@link Post} object
     */
    private void setPostData(HomeItemViewHolder holder, Post currPost) {
        holder.postText.setTransformationMethod(new LinkTransformationMethod());
        holder.postText.setMovementMethod(LinkMovementMethod.getInstance());
        holder.postTitle.setTransformationMethod(new LinkTransformationMethod());
        holder.postTitle.setMovementMethod(LinkMovementMethod.getInstance());

        String dateOfPost = currPost.getCreatedAt();

        String commentString = String.valueOf(currPost.getCommentCount()) + " " + context.getString(R.string.commented);
        String dislikeString = String.valueOf(currPost.getDislikeCount()) + " " + context.getString(R.string.find_it);
        String likeString = String.valueOf(currPost.getLikeCount()) + " " + context.getString(R.string.find_it);

        //holder.commentCount.setText(commentString);
        //holder.dislikeCount.setText(dislikeString);
        //holder.likeCount.setText(likeString);
        holder.postedTime.setText(context.getString(R.string.posted) + " " + Helper.timeDiff(dateOfPost));
        holder.userName.setText(currPost.getUserMetaData().getName());

        holder.setLikedView(currPost.getLiked() == 1);
        holder.setDislikedView(currPost.getDisliked() == 1);
    }

    public void storyShow(ArrayList<UserResponse> stories) {
        this.storyUsers.clear();
        this.storyUsers.add(new UserResponse(-1, "add", "add"));
        this.storyUsers.addAll(stories);

        if (itemsList.isEmpty() || !getItem(0).getId().equalsIgnoreCase("story")) {
            addItemOnTop(new Post("story"));
        } else {
            notifyItemChanged(0);
        }
    }

    public void storyShowMy(UserResponse userResponse) {
        if (storyUsers.isEmpty()) {
            ArrayList<UserResponse> toShow = new ArrayList<>();
            toShow.add(userResponse);
           storyShow(toShow);
        } else if (!storyUsers.contains(userResponse)) {
            storyUsers.add(1, userResponse);
            if (itemsList.isEmpty() || !getItem(0).getId().equalsIgnoreCase("story")) {
                addItemOnTop(new Post("story"));
            } else {
                notifyItemChanged(0);
            }
        }
    }

    public void storyProgress(boolean storyProgress) {
        if (!storyUsers.isEmpty()) {
            storyUsers.get(0).setStoryUpdateProgress(storyProgress);
            if (itemsList.isEmpty() || !getItem(0).getId().equalsIgnoreCase("story")) {
                addItemOnTop(new Post("story"));
            } else {
                notifyItemChanged(0);
            }
        }
    }



    class StoriesContainerViewHolder extends RecyclerView.ViewHolder {
        private TextView playAll;
        private LinearLayout storyContainer;
        private RecyclerView recyclerStory;

        public StoriesContainerViewHolder(View itemView) {
            super(itemView);
            playAll = itemView.findViewById(R.id.playAll);
            storyContainer = itemView.findViewById(R.id.storyContainer);
            recyclerStory = itemView.findViewById(R.id.recyclerStory);
           // storyContainer.setVisibility(View.GONE);
            playAll.setOnClickListener(v -> {
                if (storyUsers != null && storyUsers.size() > 1)
                    context.startActivity(StatusActivity.newIntent(context, storyUsers, 1));
            });
        }


        public void setData() {



            recyclerStory.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            recyclerStory.setNestedScrollingEnabled(false);
            recyclerStory.setAdapter(new StoriesAdapter(storyUsers, context, new StoriesAdapter.StoryClickListener() {
                @Override
                public void showStory(int pos) {
                    context.startActivity(StatusActivity.newIntent(context, storyUsers, pos));
                }

                @Override
                public void postStory() {
                    if (storyUsers.isEmpty() || !storyUsers.get(0).isStoryUpdateProgress()) {
                        if (fragment instanceof HomeFeedsFragment) {
                            ((HomeFeedsFragment) fragment).pickMedia();
                        }
                    }
                }
            }));
            playAll.setVisibility((storyUsers != null && storyUsers.size() > 1) ? View.VISIBLE : View.GONE);
           // playAll.setVisibility(View.GONE);
        }
    }

    class HomeItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView foxyImage;
        CardView cardView;
        TextView postedTime;
        TextView postText;
        TextView postTitle;
        ImageView imageView;
        View videoActionContainer;
        SquareVideoView videoView;
        LinearLayout commentNow;
        TextView userName;
        ImageView dislike;
        LinearLayout like;
        ImageView likeIcon;
        ImageView videoAction;
        ProgressBar videoProgress;

        int mediaStopPosition = 0;

//        @BindView(R.id.list_item_home_video_player_progress_bar)
//        ProgressBar progressBar;

        HomeItemViewHolder(View itemView) {
            super(itemView);
            foxyImage = itemView.findViewById(R.id.list_item_home_foxy_img);
            cardView = itemView.findViewById(R.id.cardView);
            postedTime = itemView.findViewById(R.id.list_item_home_posted_txt);
            postText = itemView.findViewById(R.id.list_item_home_text);
            postTitle = itemView.findViewById(R.id.list_item_home_title);
            imageView = itemView.findViewById(R.id.list_item_home_image);
            videoActionContainer = itemView.findViewById(R.id.videoActionContainer);
            videoView = itemView.findViewById(R.id.list_item_home_video);
            commentNow = itemView.findViewById(R.id.list_item_home_comment_now);
            userName = itemView.findViewById(R.id.list_item_home_posted_name);
            dislike = itemView.findViewById(R.id.list_item_home_dislike);
            like = itemView.findViewById(R.id.list_item_home_like);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            videoAction = itemView.findViewById(R.id.videoAction);
            videoProgress = itemView.findViewById(R.id.videoProgress);

            itemView.findViewById(R.id.list_item_home_menu).setOnClickListener(this);
            itemView.findViewById(R.id.userDetailContainer).setOnClickListener(this);
            itemView.findViewById(R.id.list_item_home_share).setOnClickListener(this);
            itemView.findViewById(R.id.list_item_home_txt_pic_vid_holder).setOnClickListener(this);
            commentNow.setOnClickListener(this);
            dislike.setOnClickListener(this);
            like.setOnClickListener(this);
        }

        /**
         * A function used to share the post on clicking the share button
         */
        void sharePost() {
            int pos = getLayoutPosition();
            if (pos != -1) {
                final Post post = getItem(getLayoutPosition());
                String dynamic_link_domain = context.getString(R.string.dynamic_link_domain);
                DynamicLink link = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setDomainUriPrefix(dynamic_link_domain.startsWith("https") ? dynamic_link_domain : ("https://" + dynamic_link_domain))
                        .setLink(Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName() + "&post=" + post.getId()))
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())).build())
                        .buildDynamicLink();

                FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLongLink(link.getUri())
                        .buildShortDynamicLink()
                        .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                            @Override
                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                if (task.isSuccessful()) {
                                    // Short link created
                                    Uri shortLink = task.getResult().getShortLink();
                                    Uri flowchartLink = task.getResult().getPreviewLink();

                                    String shareText = context.getString(R.string.view_amazin_post) + " " + context.getString(R.string.app_name) + " " + shortLink.toString();
                                    Helper.openShareIntent(context, itemView, shareText);
                                    foxyService.updateSharePost(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), post.getId()).enqueue(new Callback<JsonObject>() {
                                        @Override
                                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                            response.isSuccessful();
                                        }

                                        @Override
                                        public void onFailure(Call<JsonObject> call, Throwable t) {
                                            t.getMessage();
                                        }
                                    });
                                } else {
                                    // Error
                                    // ...
                                }
                            }
                        });
            }
        }

        /**
         * Opens the {@link CommentsFragment} for particular post on click of comment button
         */
        void commentPopUp() {
            int pos = getLayoutPosition();
            if (pos != -1) {
                final Post currPost = getItem(pos);
                String postId = currPost.getId();

                if (fragment != null && fragment.getActivity() != null) {
                    OnPopupMenuItemClickListener onPopupMenuItemClickListener = new OnPopupMenuItemClickListener() {
                        @Override
                        public void onReportNowClick() {

                        }

                        @Override
                        public void onDeleteClick() {
                            currPost.setCommentCount(currPost.getCommentCount() - 1);
                            String commentString = String.valueOf(currPost.getCommentCount()) + " " + context.getString(R.string.commented);
                            //commentCount.setText(commentString);
                        }
                    };

                    OnCommentAddListener onCommentAddListener = new OnCommentAddListener() {
                        @Override
                        public void onCommentAdded() {
                            currPost.setCommentCount(currPost.getCommentCount() + 1);
                            String commentString = String.valueOf(currPost.getCommentCount()) + " " + context.getString(R.string.commented);
                            //commentCount.setText(commentString);
                        }
                    };
                    Log.d("Post Id",postId);
                 //  Intent intent = new Intent( context, CommentActivity.class);
                  //context.startActivity(intent);


           CommentsFragment commentsFragment = CommentsFragment.newInstance(postId, onPopupMenuItemClickListener, onCommentAddListener);

                    fragment.getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.bottom_up, R.anim.bottom_down, R.anim.bottom_up, R.anim.bottom_down)
                            .add(R.id.activity_main_container, commentsFragment, CommentsFragment.class.getName())
                            .addToBackStack(null)
                            .commit();
                }
            }
        }

        void onDislikeClick() {
            final int position = getLayoutPosition();
            if (position != -1) {
                final Post currPost = getItem(position);

                boolean alreadyDisliked = currPost.getDisliked() == 1;
                currPost.setDisliked(alreadyDisliked ? 0 : 1);

                Intent postChangeEventIntent = new Intent(Constants.POST_CHANGE_EVENT);
                postChangeEventIntent.putExtra("post", currPost);
                postChangeEventIntent.putExtra("bookmark", true);
                LocalBroadcastManager.getInstance(context).sendBroadcast(postChangeEventIntent);
            }
        }

        void onLikeClick() {
            final int position = getLayoutPosition();
            if (position != -1) {
                final Post currPost = getItem(position);

                boolean alreadyLiked = currPost.getLiked() == 1;
                currPost.setLiked(alreadyLiked ? 0 : 1);
                currPost.setLikeCount(alreadyLiked ? (currPost.getLikeCount() - 1) : (currPost.getLikeCount() + 1));
                Intent postChangeEventIntent = new Intent(Constants.POST_CHANGE_EVENT);
                postChangeEventIntent.putExtra("post", currPost);
                LocalBroadcastManager.getInstance(context).sendBroadcast(postChangeEventIntent);
                if (!likeDislikeUpdateMap.containsKey(currPost.getId())) {
                    likeDislikeUpdateMap.put(currPost.getId(), new LikeDislikeScoreUpdate());
                }
                likeDislikeUpdateMap.get(currPost.getId()).setLike(alreadyLiked ? -1 : 1);
                executeLike(currPost.getId());
            }
        }

        void setDislikedView(boolean disliked) {
            //dislike.setTypeface(null, disliked ? Typeface.BOLD : Typeface.NORMAL);
            //dislike.setTextColor(ContextCompat.getColor(context, disliked ? R.color.colorAccent : R.color.colorText));
            //dislike.setCompoundDrawablesWithIntrinsicBounds(disliked ? R.drawable.ic_bookmark_blue_18dp : R.drawable.ic_bookmark_gray_18dp, 0, 0, 0);
            dislike.setImageDrawable(ContextCompat.getDrawable(context, disliked ? R.drawable.ic_bookmark_blue_18dp : R.drawable.ic_bookmark_gray_18dp));
        }

        void setLikedView(boolean liked) {
//            like.setTypeface(null, liked ? Typeface.BOLD : Typeface.NORMAL);
//            like.setTextColor(ContextCompat.getColor(context, liked ? R.color.colorPrimary : R.color.colorText));
            likeIcon.setImageResource(liked ? R.drawable.ic_like_blue_18dp : R.drawable.ic_like_gray_18dp);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.userDetailContainer:
                  //  UserMeta userMeta = getItem(getLayoutPosition()).getUserMetaData();
                 //   context.startActivity(UserProfileDetailActivity.newInstance(context, userMeta.getId().toString(), userMeta.getName(), userMeta.getImage()));
                    break;
                case R.id.list_item_home_share:
                    SpringAnimationHelper.performAnimation(view);
                    sharePost();
                    break;
                case R.id.list_item_home_comment_now:
                    SpringAnimationHelper.performAnimation(view);
                    commentPopUp();
                    break;
                case R.id.list_item_home_dislike:
                    SpringAnimationHelper.performAnimation(view);
                    onDislikeClick();
                    break;
                case R.id.list_item_home_like:
                    SpringAnimationHelper.performAnimation(view);
                    onLikeClick();
                    break;
                case R.id.list_item_home_menu:
                    final int pos = getLayoutPosition();
                    if (pos != -1) {
                        final Post post = getItem(pos);
                        UserResponse userMe = Helper.getLoggedInUser(sharedPreferenceUtil);
                        SpringAnimationHelper.performAnimation(view);
                        PopupMenu popup = new PopupMenu(context, view);
                        popup.inflate(R.menu.menu_home_item);
                        popup.getMenu().getItem(1).setVisible(post.getUserMetaData().getId().equals(userMe.getId()));
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.action_report:
                                        reportPost(post.getId());
                                        break;
                                    case R.id.action_delete:
                                        deletePost(post.getId());
                                        Toast.makeText(context, R.string.post_deleted, Toast.LENGTH_SHORT).show();
                                        removeItemAt(pos);
                                        break;
                                }
                                return false;
                            }
                        });
                        //displaying the popup
                        popup.show();
                    }
                    break;
                case R.id.list_item_home_txt_pic_vid_holder:
                    int posi = getLayoutPosition();
                    if (posi != -1) {
                        //  context.startActivity(DetailHomeItemActivity.newIntent(context, getItem(posi)));
                    }
                    break;
            }
        }
    }

    private void reportPost(String id) {
        foxyService.reportPost(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (context != null && response.isSuccessful()) {
                    Toast.makeText(context, R.string.post_reported, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.getMessage();
            }
        });
    }

    private void deletePost(String id) {
        foxyService.deletePost(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                response.isSuccessful();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.getMessage();
            }
        });
    }

    private void executeDislike(String id) {
        if (!likeDislikeUpdateMap.get(id).isInProgress()) {
            likeDislikeUpdateMap.get(id).setInProgress(true);
            foxyService.updatePostDislike(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), id).enqueue(new Callback<LikeDislikeResponse>() {
                @Override
                public void onResponse(Call<LikeDislikeResponse> call, Response<LikeDislikeResponse> response) {
                    if (response.isSuccessful()) {
                        likeDislikeUpdateMap.get(response.body().getId()).setInProgress(false);
                        if (likeDislikeUpdateMap.get(response.body().getId()).getDislike() != response.body().getStatus()) {
                            executeDislike(response.body().getId());
                        }
                    }
                }

                @Override
                public void onFailure(Call<LikeDislikeResponse> call, Throwable t) {

                }
            });
        }
    }

    private void executeLike(String id) {
        if (!likeDislikeUpdateMap.get(id).isInProgress()) {
            likeDislikeUpdateMap.get(id).setInProgress(true);
            foxyService.updatePostLike(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), id).enqueue(new Callback<LikeDislikeResponse>() {
                @Override
                public void onResponse(Call<LikeDislikeResponse> call, Response<LikeDislikeResponse> response) {
                    if (response.isSuccessful()) {
                        likeDislikeUpdateMap.get(response.body().getId()).setInProgress(false);
                        if (likeDislikeUpdateMap.get(response.body().getId()).getLike() != response.body().getStatus()) {
                            executeLike(response.body().getId());
                        }
                    }
                }

                @Override
                public void onFailure(Call<LikeDislikeResponse> call, Throwable t) {
                }
            });
        }
    }
}
