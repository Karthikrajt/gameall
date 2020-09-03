package com.siragu.gameall.activity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.siragu.gameall.R;
import com.siragu.gameall.adapter.CommentsRecyclerAdapter;
import com.siragu.gameall.listener.OnCommentAddListener;
import com.siragu.gameall.listener.OnPopupMenuItemClickListener;
import com.siragu.gameall.model.Comment;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.network.request.CreateCommentRequest;
import com.siragu.gameall.network.response.BaseListModel;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.siragu.gameall.util.SpringAnimationHelper;
import com.varunjohn1990.audio_record_view.AudioRecordView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class CommentActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText addACommentEdittext;
    View emptyView;
    SwipeRefreshLayout swipeRefresh;
    ImageView profileIcon;
    private AudioRecordView audioRecordView;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private long time;
    private String postId;
    private static ArrayList<Comment> commentArrayList;
    private static String previousPostId = "";
    private OnPopupMenuItemClickListener onPopupMenuItemClickListener;
    private OnCommentAddListener onCommentAddListener;
    private boolean isFullScreen = true;
   // private AudioRecordView audioRecordView;
    private int pageNumber = 1;
    Context c = null;
    private DrService foxyService;
    private boolean allDone, isLoading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        postId = getIntent().getStringExtra("postId");
        findView_by();
        foxyService = ApiUtils.getClient().create(DrService.class);
        findViewById(R.id.btn_post_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postCommentOnServer(v);
            }
        });




//        if (!isFullScreen) {
//            recyclerView.setNestedScrollingEnabled(false);
//        }

        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        UserResponse userMe = Helper.getLoggedInUser(sharedPreferenceUtil);
        if (userMe != null) {
            Glide.with(this).load(userMe.getImage())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(this, 8))).placeholder(R.drawable.ic_person_gray_24dp)).into(profileIcon);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageNumber = 1;
                commentsRecyclerAdapter.clear();
                swipeRefresh.setRefreshing(true);
                loadComments();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(this);
        recyclerView.setAdapter(commentsRecyclerAdapter);
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        swipeRefresh.setColorSchemeResources(R.color.colorAccent);

        swipeRefresh.setRefreshing(true);
        if (commentsRecyclerAdapter.isLoaderShowing())
            commentsRecyclerAdapter.hideLoading();
        loadComments();

        audioRecordView = new AudioRecordView();
        // this is to make your layout the root of audio record view, root layout supposed to be empty..
        audioRecordView.initView((CardView) findViewById(R.id.layoutMain));
        // this is to provide the container layout to the audio record view..
        View containerView = audioRecordView.setContainerView(R.layout.fragment_comment);
        audioRecordView.setRecordingListener(new AudioRecordView.RecordingListener() {
            @Override
            public void onRecordingStarted() {
            }
            @Override
            public void onRecordingLocked() {
            }
            @Override
            public void onRecordingCompleted() {
            }
            @Override
            public void onRecordingCanceled() {
            }
        });


        setListener();
        audioRecordView.getMessageView().requestFocus();
    }
    private void setListener() {
        audioRecordView.getCameraView().setVisibility(View.GONE);
        audioRecordView.getEmojiView().setVisibility(View.GONE);
        audioRecordView.getMessageView().setPadding(25,25,25,25);
        audioRecordView.getAttachmentView().setVisibility(View.GONE);
        audioRecordView.getEmojiView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordView.hideAttachmentOptionView();
                //showToast("Emoji Icon Clicked");
            }
        });



        audioRecordView.getSendView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = audioRecordView.getMessageView().getText().toString().trim();
                audioRecordView.getMessageView().setText("");
                //  messageAdapter.add(new Message(msg));
            }
        });
    }

    private Callback<BaseListModel<Comment>> callBack = new Callback<BaseListModel<Comment>>() {
        @Override
        public void onResponse(Call<BaseListModel<Comment>> call, Response<BaseListModel<Comment>> response) {
            hideLoading();
            if (response.isSuccessful()) {
                BaseListModel<Comment> postResponse = response.body();
                if (postResponse.getData() == null || postResponse.getData().isEmpty()) {
                    allDone = true;
                    if (commentsRecyclerAdapter.getItemCount() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                } else {
                    commentsRecyclerAdapter.addItemsOnTop(postResponse.getData());
                }
            }
        }

        @Override
        public void onFailure(Call<BaseListModel<Comment>> call, Throwable t) {
            hideLoading();
        }
    };

    private void hideLoading() {
        isLoading = false;
        if (swipeRefresh.isRefreshing())
            swipeRefresh.setRefreshing(false);
        if (commentsRecyclerAdapter.isLoaderShowing())
            commentsRecyclerAdapter.hideLoading();
    }

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            // init
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            if (layoutManager.getChildCount() > 0) {
                // Calculations..
                int indexOfLastItemViewVisible = layoutManager.getChildCount() - 1;
                View lastItemViewVisible = layoutManager.getChildAt(indexOfLastItemViewVisible);
                int adapterPosition = layoutManager.getPosition(lastItemViewVisible);
                boolean isLastItemVisible = (adapterPosition == adapter.getItemCount() - 1);
                // check
                if (isLastItemVisible && !isLoading && !allDone) {
                    pageNumber++;
                    commentsRecyclerAdapter.showLoading();
                    loadComments();
                }
            }
        }
    };

    private void loadComments() {
        isLoading = true;
        foxyService.getComments(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), postId, pageNumber).enqueue(callBack);
    }


    public void findView_by()
    {
        recyclerView = findViewById(R.id.comment_recycler_view);
        addACommentEdittext = findViewById(R.id.add_a_comment_edittext);
        emptyView = findViewById(R.id.empty_view_container);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        profileIcon = findViewById(R.id.list_item_comment_foxy_img);
    }
    private void updateComments(CreateCommentRequest commentToUpdate) {
        foxyService.createComment(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), postId, commentToUpdate).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful()) {
                    commentsRecyclerAdapter.addItemOnTop(response.body());
                    recyclerView.scrollToPosition(0);
                    if (emptyView.getVisibility() == View.VISIBLE) {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {

            }
        });
    }

    public void postCommentOnServer(View view) {
        SpringAnimationHelper.performAnimation(view);

        final String commentTextToPost = addACommentEdittext.getText().toString();
        if (TextUtils.isEmpty(commentTextToPost)) {
            Toast.makeText(this, R.string.err_field_comment, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.comment_added, Toast.LENGTH_SHORT).show();
            onCommentAddListener.onCommentAdded();
            updateComments(new CreateCommentRequest(commentTextToPost));
            addACommentEdittext.setText("");
            Helper.closeKeyboard(this);
        }
    }
}