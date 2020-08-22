package com.siragu.gameall.fragment;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.siragu.gameall.activity.MainActivity;
import com.siragu.gameall.adapter.CommentsRecyclerAdapter;
import com.siragu.gameall.listener.OnCommentAddListener;
import com.siragu.gameall.listener.OnPopupMenuItemClickListener;
import com.siragu.gameall.model.Comment;
import com.siragu.gameall.R;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.siragu.gameall.util.SpringAnimationHelper;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.network.request.CreateCommentRequest;
import com.siragu.gameall.network.response.BaseListModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A screen to display and add the comments
 */
public class CommentsFragment extends Fragment {
    RecyclerView recyclerView;
    EditText addACommentEdittext;
    View emptyView;
    SwipeRefreshLayout swipeRefresh;
    ImageView profileIcon;

    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private SharedPreferenceUtil sharedPreferenceUtil;

    private String postId;
    private static ArrayList<Comment> commentArrayList;
    private static String previousPostId = "";
    private OnPopupMenuItemClickListener onPopupMenuItemClickListener;
    private OnCommentAddListener onCommentAddListener;
    private boolean isFullScreen = true;

    private int pageNumber = 1;

    private DrService foxyService;
    private boolean allDone, isLoading;

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

    /**
     * Returns new instance of {@link CommentsFragment}
     *
     * @param postId                       The id of the post for which comments are to be shown and added
     * @param onPopupMenuItemClickListener A callback to denote the deletion or report of comment
     * @param onCommentAddListener         A callback which is used when new comment is added
     * @return {@link CommentsFragment}
     */
    public static CommentsFragment newInstance(String postId, OnPopupMenuItemClickListener onPopupMenuItemClickListener, OnCommentAddListener onCommentAddListener) {
        return newInstance(postId, true, onPopupMenuItemClickListener, onCommentAddListener);
    }


    /**
     * Returns new instance of {@link CommentsFragment}
     *
     * @param postId                       The id of the post for which comments are to be shown and added
     * @param isFullScreen                 Denotes whether to open the fragment is fullscreen or not
     * @param onPopupMenuItemClickListener A callback to denote the deletion or report of comment
     * @param onCommentAddListener         A callback which is used when new comment is added
     * @return {@link CommentsFragment}
     */
    public static CommentsFragment newInstance(String postId, boolean isFullScreen, OnPopupMenuItemClickListener onPopupMenuItemClickListener, OnCommentAddListener onCommentAddListener) {
        CommentsFragment commentsFragment = new CommentsFragment();
        commentsFragment.postId = postId;
        commentsFragment.isFullScreen = isFullScreen;
        commentsFragment.onPopupMenuItemClickListener = onPopupMenuItemClickListener;
        commentsFragment.onCommentAddListener = onCommentAddListener;
        return commentsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        recyclerView = view.findViewById(R.id.comment_recycler_view);
        addACommentEdittext = view.findViewById(R.id.add_a_comment_edittext);
        emptyView = view.findViewById(R.id.empty_view_container);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        profileIcon = view.findViewById(R.id.list_item_comment_foxy_img);
        view.findViewById(R.id.btn_post_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postCommentOnServer(v);
            }
        });

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomBar();
        }


//        if (!isFullScreen) {
//            recyclerView.setNestedScrollingEnabled(false);
//        }

        sharedPreferenceUtil = new SharedPreferenceUtil(getActivity());
        UserResponse userMe = Helper.getLoggedInUser(sharedPreferenceUtil);
        if (userMe != null) {
            Glide.with(getContext()).load(userMe.getImage())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(getContext(), 8))).placeholder(R.drawable.ic_person_gray_24dp)).into(profileIcon);
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(getContext());
        recyclerView.setAdapter(commentsRecyclerAdapter);
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        foxyService = ApiUtils.getClient().create(DrService.class);
        swipeRefresh.setRefreshing(true);
        if (commentsRecyclerAdapter.isLoaderShowing())
            commentsRecyclerAdapter.hideLoading();
        loadComments();
    }

    /**
     * Handles the click of send button to post the comment.
     * It also validates the comment of not being empty
     *
     * @param view
     */
    public void postCommentOnServer(View view) {
        SpringAnimationHelper.performAnimation(view);

        final String commentTextToPost = addACommentEdittext.getText().toString();
        if (TextUtils.isEmpty(commentTextToPost)) {
            Toast.makeText(getContext(), R.string.err_field_comment, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.comment_added, Toast.LENGTH_SHORT).show();
            onCommentAddListener.onCommentAdded();
            updateComments(new CreateCommentRequest(commentTextToPost));
            addACommentEdittext.setText("");
            Helper.closeKeyboard((Activity) getContext());
        }
    }

    /**
     * Adds the comment to adapter after posting n the server
     *
     * @param commentToUpdate The comment text
     */
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomBar();
        }
    }

    /**
     * Requests the focus of the comment adding edit text
     */
    public void requestEditTextFocus() {
        addACommentEdittext.requestFocus();
    }
}
