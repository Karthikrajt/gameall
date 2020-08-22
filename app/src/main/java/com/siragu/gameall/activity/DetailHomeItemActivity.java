package com.siragu.gameall.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.siragu.gameall.fragment.CommentsFragment;
import com.siragu.gameall.listener.OnCommentAddListener;
import com.siragu.gameall.listener.OnPopupMenuItemClickListener;
import com.siragu.gameall.model.Post;
import com.siragu.gameall.model.LikeDislikeScoreUpdate;
import com.siragu.gameall.R;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.google.gson.JsonObject;
import com.siragu.gameall.view.SquareVideoView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * An Activity that is used to display details of the feed post.<br/>
 * It shows complete post along with the comments.
 */

public class DetailHomeItemActivity extends AppCompatActivity {
    private static String POST_ID = "post_id";
    private static String POST = "post";

    ImageView foxyImage;
    TextView postedBy;
    TextView postedTime;
    TextView postText;
    TextView postTitle;
    ImageView imageView;
    //    TextView commentCount;
//    TextView dislikeCount;
//    TextView likeCount;
    ImageView dislike;
    ImageView likeIcon;
    LinearLayout like;
    LinearLayout layout;
    //SwipeRefreshLayout swipeRefresh;
    //CardView layoutContainer;

    ImageView videoAction;
    ProgressBar videoProgress;
    View videoActionContainer;
    SquareVideoView videoView;
    int mediaStopPosition = 0;

    private Post post;
    private CommentsFragment commentsFragment;
    private DrService service;
    private SharedPreferenceUtil sharedPreference;
    private String postId;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("detail", "create");
        setContentView(R.layout.activity_detail_home_item);
        initUi();

        service = ApiUtils.getClient().create(DrService.class);
        sharedPreference = new SharedPreferenceUtil(this);

//        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getPost(postId);
//            }
//        });

        Intent inIntent = getIntent();
        if (inIntent.hasExtra(POST)) {
            post = inIntent.getParcelableExtra(POST);
            if (post != null && !TextUtils.isEmpty(post.getId())) {
                postId = post.getId();
                init();
            } else {
                finish();
            }
        } else if (inIntent.hasExtra(POST_ID)) {
            postId = getIntent().getStringExtra(POST_ID);
            if (!TextUtils.isEmpty(postId)) {
//                swipeRefresh.setRefreshing(true);
//                layoutContainer.setVisibility(View.GONE);
                getPost(postId);
            } else {
                finish();
            }
        } else {
            finish();
        }

    }

    private void initUi() {
        coordinatorLayout = findViewById(R.id.coordinator);
        appBarLayout = findViewById(R.id.appBarLayout);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        foxyImage = findViewById(R.id.list_item_home_foxy_img);
        postedBy = findViewById(R.id.list_item_home_posted_name);
        postedTime = findViewById(R.id.list_item_home_posted_txt);
        postTitle = findViewById(R.id.list_item_home_title);
        postText = findViewById(R.id.list_item_home_text);
        imageView = findViewById(R.id.list_item_home_image);
//        commentCount = findViewById(R.id.list_item_home_comment_count);
//        dislikeCount = findViewById(R.id.list_item_home_dislike_count);
//        likeCount = findViewById(R.id.list_item_home_like_count);
        dislike = findViewById(R.id.list_item_home_dislike);
        like = findViewById(R.id.list_item_home_like);
        likeIcon = findViewById(R.id.likeIcon);
        layout = findViewById(R.id.list_item_home_layout);
//        swipeRefresh = findViewById(R.id.swipeRefreshMain);
//        layoutContainer = findViewById(R.id.layoutContainer);
        videoView = findViewById(R.id.list_item_home_video);
        videoActionContainer = findViewById(R.id.videoActionContainer);
        videoProgress = findViewById(R.id.videoProgress);
        videoAction = findViewById(R.id.videoAction);

        findViewById(R.id.list_item_home_dislike).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDislikeClick();
            }
        });
        findViewById(R.id.list_item_home_like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLikeClick();
            }
        });
        findViewById(R.id.list_item_home_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePost();
            }
        });
        findViewById(R.id.list_item_home_comment_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCommentNowClick();
            }
        });

        appBarLayout.setExpanded(true);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    private void getPost(String postId) {
        service.getPostById(sharedPreference.getStringPreference(Constants.KEY_API_KEY, null), postId).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                //swipeRefresh.setRefreshing(false);
                if (response.isSuccessful()) {
                    post = response.body();
                    init();
                } else {
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                //swipeRefresh.setRefreshing(false);
                finish();
            }
        });
    }

    private void init() {
        //layoutContainer.setVisibility(View.VISIBLE);
        initHomeListItem();

        OnPopupMenuItemClickListener onPopupMenuItemClickListener = new OnPopupMenuItemClickListener() {
            @Override
            public void onReportNowClick() {

            }

            @Override
            public void onDeleteClick() {
                post.setCommentCount(post.getCommentCount() - 1);
                String commentString = String.valueOf(post.getCommentCount())
                        + " " + getString(R.string.commented);
                //commentCount.setText(commentString);
            }
        };

        OnCommentAddListener onCommentAddListener = new OnCommentAddListener() {
            @Override
            public void onCommentAdded() {
                post.setCommentCount(post.getCommentCount() + 1);
                String commentString = String.valueOf(post.getCommentCount())
                        + " " + getString(R.string.commented);
                //commentCount.setText(commentString);
            }
        };

        commentsFragment = CommentsFragment.newInstance(post.getId(), false, onPopupMenuItemClickListener, onCommentAddListener);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.bottom_up, R.anim.bottom_down, R.anim.bottom_up, R.anim.bottom_down)
                .replace(R.id.detailFrame, commentsFragment, CommentsFragment.class.getName())
                .commit();

        //setAppBarOffset(Helper.getDisplayMetrics().widthPixels / 2);
    }

    /**
     * Initializes the views for a particular kind of post.<br/>
     * Post Type: text, image and video
     * It also sets the data to each view<br/>
     */
    private void initHomeListItem() {
        String dateOfPost = post.getCreatedAt();

        String commentString = String.valueOf(post.getCommentCount())
                + " " + getString(R.string.commented);
        String dislikeString = String.valueOf(post.getDislikeCount())
                + " " + getString(R.string.find_it);
        String likeString = String.valueOf(post.getLikeCount())
                + " " + getString(R.string.find_it);


        if (post.getUserMetaData() != null) {
            Glide.with(this).load(post.getUserMetaData().getImage()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_person)).into(foxyImage);
            postedBy.setText(post.getUserMetaData().getName());
        }

        if (TextUtils.isEmpty(post.getTitle())) {
            postTitle.setVisibility(View.GONE);
        } else {
            postTitle.setVisibility(View.VISIBLE);
            postTitle.setText(post.getTitle());
        }

        switch (post.getType()) {
            case "text":
                videoActionContainer.setVisibility(View.GONE);
                postText.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                postText.setText(post.getText());
                if (!TextUtils.isEmpty(post.getMedia_url())) {
                    imageView.setVisibility(View.VISIBLE);
                    Glide.with(this)
                            .load(post.getMedia_url())
                            .apply(new RequestOptions().placeholder(R.drawable.placeholder).dontAnimate())
                            .into(imageView);
                }
                break;
            case "image":
                videoActionContainer.setVisibility(View.GONE);
                postText.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                Glide.with(this)
                        .load(post.getMedia_url())
                        .apply(new RequestOptions().placeholder(R.drawable.placeholder).dontAnimate())
                        .into(imageView);
                break;
            case "video":
                videoActionContainer.setVisibility(View.VISIBLE);
                postText.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);

                videoProgress.setVisibility(View.VISIBLE);
                videoAction.setVisibility(View.GONE);

                String videoUrl = post.getMedia_url();
                videoView.setVideoURI(Uri.parse(videoUrl));
                videoView.setVideoPath(videoUrl);
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        videoProgress.setVisibility(View.GONE);
                        return true;
                    }
                });
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        videoProgress.setVisibility(View.GONE);
                        videoAction.setVisibility(View.VISIBLE);
                    }
                });
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        videoAction.setImageDrawable(ContextCompat.getDrawable(DetailHomeItemActivity.this, R.drawable.ic_play_circle_outline_36dp));
                    }
                });
                videoAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (videoView.isPlaying()) {
                            mediaStopPosition = videoView.getCurrentPosition();
                            videoView.pause();
                        } else {
                            videoView.seekTo(mediaStopPosition);
                            videoView.start();
                        }
                        videoAction.setImageDrawable(ContextCompat.getDrawable(DetailHomeItemActivity.this, videoView.isPlaying() ? R.drawable.ic_pause_circle_outline_36dp : R.drawable.ic_play_circle_outline_36dp));
                    }
                });

//                    String videoThumbUrl = post.getVideoThumbnailUrl();
//
//                    Glide.with(context)
//                            .load(videoThumbUrl)
//                            .apply(new RequestOptions().placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).dontAnimate())
//                            .into(imageView);
                break;
        }


//        commentCount.setText(commentString);
//        2dislikeCount.setText(dislikeString);
//        likeCount.setText(likeString);
        postedTime.setText(Helper.timeDiff(dateOfPost));

        setLikedView(post.getLiked() == 1);
        setDislikedView(post.getDisliked() == 1);
    }


    void setDislikedView(boolean disliked) {
        dislike.setImageDrawable(ContextCompat.getDrawable(this, disliked ? R.drawable.ic_bookmark_blue_18dp : R.drawable.ic_bookmark_gray_18dp));
//        dislike.setTypeface(null, disliked ? Typeface.BOLD : Typeface.NORMAL);
//        dislike.setTextColor(ContextCompat.getColor(this, disliked ? R.color.colorAccent : R.color.colorText));
//        dislike.setCompoundDrawablesWithIntrinsicBounds(disliked ? R.drawable.ic_bookmark_blue_18dp : R.drawable.ic_bookmark_gray_18dp, 0, 0, 0);
    }

    void setLikedView(boolean liked) {
//        like.setTypeface(null, liked ? Typeface.BOLD : Typeface.NORMAL);
//        like.setTextColor(ContextCompat.getColor(this, liked ? R.color.colorAccent : R.color.colorText));
        likeIcon.setImageResource(liked ? R.drawable.ic_like_blue_18dp : R.drawable.ic_like_gray_18dp);
    }

    /**
     * Updates the view data when dislike is clicked.<br/>
     * <b>-1</b> denotes dislike<br/>
     * <b>0</b> denotes neutral<br/>
     * <b>1</b> denotes like<br/>
     */
    void onDislikeClick() {
        LikeDislikeScoreUpdate likeDislikeScoreUpdate = new LikeDislikeScoreUpdate();

        boolean alreadyDisliked = post.getDisliked() == 1;
        post.setDisliked(alreadyDisliked ? 0 : 1);
        post.setDislikeCount(alreadyDisliked ? (post.getDislikeCount() - 1) : (post.getDislikeCount() + 1));
        //likeDislikeScoreUpdate.setDislike(!alreadyDisliked);

        initHomeListItem();
    }


    /**
     * Updates the view data when like is clicked.<br/>
     * <b>-1</b> denotes dislike<br/>
     * <b>0</b> denotes neutral<br/>
     * <b>1</b> denotes like<br/>
     */
    void onLikeClick() {
        LikeDislikeScoreUpdate likeDislikeScoreUpdate = new LikeDislikeScoreUpdate();

        boolean alreadyLiked = post.getLiked() == 1;
        post.setLiked(alreadyLiked ? 0 : 1);
        post.setLikeCount(alreadyLiked ? (post.getLikeCount() - 1) : (post.getLikeCount() + 1));

        initHomeListItem();
    }

    /**
     * Opens the sharing intent to share the post on different apps.
     */
    public void sharePost() {
        DynamicLink link = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setDynamicLinkDomain(getString(R.string.dynamic_link_domain))
                .setLink(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName() + "&post=" + post.getId()))
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())).build())
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

                            String shareText = getString(R.string.view_amazin_post) + " " + getString(R.string.app_name) + " " + shortLink.toString();
                            Helper.openShareIntent(DetailHomeItemActivity.this, layout, shareText);
                            service.updateSharePost(sharedPreference.getStringPreference(Constants.KEY_API_KEY, null), post.getId()).enqueue(new Callback<JsonObject>() {
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

    public void onCommentNowClick() {
        commentsFragment.requestEditTextFocus();
    }

    public static Intent newIntent(Context context, String postId) {
        Intent intent = new Intent(context, DetailHomeItemActivity.class);
        intent.putExtra(POST_ID, postId);
        return intent;
    }

    public static Intent newIntent(Context context, Post post) {
        Intent intent = new Intent(context, DetailHomeItemActivity.class);
        intent.putExtra(POST, post);
        return intent;
    }

    private void setAppBarOffset(int offsetPx) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setTopAndBottomOffset(0);
        behavior.onNestedPreScroll(coordinatorLayout, appBarLayout, null, 0, offsetPx, new int[2], 1);
        params.setBehavior(behavior);
        appBarLayout.setLayoutParams(params);
    }
}
