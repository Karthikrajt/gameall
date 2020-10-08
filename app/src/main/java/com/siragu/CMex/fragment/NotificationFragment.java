package com.siragu.CMex.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siragu.CMex.activity.DetailHomeItemActivity;
import com.siragu.CMex.adapter.NotificationRecyclerAdapter;
import com.siragu.CMex.listener.ActivityClickListener;
import com.siragu.CMex.model.Activity;
import com.siragu.CMex.R;
import com.siragu.CMex.model.Post;
import com.siragu.CMex.network.ApiError;
import com.siragu.CMex.network.ApiUtils;
import com.siragu.CMex.network.DrService;
import com.siragu.CMex.network.ErrorUtils;
import com.siragu.CMex.network.request.FollowRequestReview;
import com.siragu.CMex.network.response.BaseListModel;
import com.siragu.CMex.network.response.FollowRequest;
import com.siragu.CMex.network.response.ProfileFollowRequestReviewResponse;
import com.siragu.CMex.util.Constants;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.SharedPreferenceUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays the notifications mimicking a popup
 */
public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private LinearLayout emptyViewContainer;
    private TextView empty_view_text;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NotificationRecyclerAdapter notificationAdapter;
    private DrService foxyService;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private boolean allDone, isLoading;
    private int pageNumber = 1;
    private Call<BaseListModel<Activity>> getActivities;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferenceUtil = new SharedPreferenceUtil(getContext());
        foxyService = ApiUtils.getClient().create(DrService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.fragment_notification_recycler_view);
        emptyViewContainer = view.findViewById(R.id.empty_view_container);
        empty_view_text = view.findViewById(R.id.empty_view_text);
        swipeRefreshLayout = view.findViewById(R.id.frag_home_feeds_swipe_refresh_layout);

        ViewCompat.setElevation(view, Helper.dpToPx(getContext(), 8));
        view.bringToFront();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageNumber = 1;
                notificationAdapter.clear();
                allDone = false;
                loadPosts();
                recyclerView.setVisibility(View.VISIBLE);
                emptyViewContainer.setVisibility(View.GONE);
                //addInitialPosts(true);
            }
        });

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        notificationAdapter = new NotificationRecyclerAdapter(getContext(), new ActivityClickListener() {
            @Override
            public void onActivityClick(final Activity activity, final int pos) {
                if (activity.getFollowRequest() == null && activity.getPost_id() != null && !activity.isInProgress()) {
                    activity.setInProgress(true);
                    notificationAdapter.notifyItemChanged(pos);
                    foxyService.getPostById(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), activity.getPost_id()).enqueue(new Callback<Post>() {
                        @Override
                        public void onResponse(Call<Post> call, Response<Post> response) {
                            if (mContext != null) {
                                activity.setInProgress(false);
                                notificationAdapter.notifyItemChanged(pos);
                                if (response.isSuccessful()) {
                                    startActivity(DetailHomeItemActivity.newIntent(mContext, response.body()));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Post> call, Throwable t) {
                            if (mContext != null) {
                                activity.setInProgress(false);
                                notificationAdapter.notifyItemChanged(pos);
                            }
                        }
                    });
                }
            }

            @Override
            public void onActivityFollowRequestClick(final Activity activity, FollowRequestReview followRequestReview, final int pos) {
                if (activity.getFollowRequest() != null && !activity.isInProgress()) {
                    activity.setInProgress(true);
                    notificationAdapter.notifyItemChanged(pos);
                    foxyService.profileFollowActionReview(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), String.valueOf(activity.getFollowRequest().getId()), followRequestReview).enqueue(new Callback<ProfileFollowRequestReviewResponse>() {
                        @Override
                        public void onResponse(Call<ProfileFollowRequestReviewResponse> call, Response<ProfileFollowRequestReviewResponse> response) {
                            if (mContext != null) {
                                activity.setInProgress(false);
                                notificationAdapter.notifyItemChanged(pos);

                                if (response.isSuccessful()) notificationAdapter.removeItemAt(pos);
                                recyclerView.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.GONE : View.VISIBLE);
                                emptyViewContainer.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.VISIBLE : View.GONE);
                                empty_view_text.setText(R.string.nothing_found);
                            }
                        }

                        @Override
                        public void onFailure(Call<ProfileFollowRequestReviewResponse> call, Throwable t) {
                            if (mContext != null) {
                                activity.setInProgress(false);
                                notificationAdapter.notifyItemChanged(pos);

                                recyclerView.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.GONE : View.VISIBLE);
                                emptyViewContainer.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.VISIBLE : View.GONE);
                                empty_view_text.setText(R.string.nothing_found);
                            }
                        }
                    });
                }
            }
        });
        recyclerView.setAdapter(notificationAdapter);

        initialize();

        return view;
    }

    private void initialize() {
        loadPosts();
        recyclerView.setVisibility(View.VISIBLE);
        emptyViewContainer.setVisibility(View.GONE);
    }

    private void loadPosts() {
        isLoading = true;
        getActivities = foxyService.getActivities(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), pageNumber);
        getActivities.enqueue(callBack);
        if (pageNumber == 1) {
            foxyService.profileFollowRequests(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null)).enqueue(new Callback<ArrayList<FollowRequest>>() {
                @Override
                public void onResponse(Call<ArrayList<FollowRequest>> call, Response<ArrayList<FollowRequest>> response) {
                    if (mContext != null) {
                        if (response.isSuccessful()) {
                            ArrayList<Activity> frs = new ArrayList<>();
                            for (FollowRequest fr : response.body()) {
                                frs.add(new Activity(fr));
                            }
                            notificationAdapter.addItemsOnTop(frs);
                        }
                        recyclerView.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.GONE : View.VISIBLE);
                        emptyViewContainer.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.VISIBLE : View.GONE);
                        empty_view_text.setText(R.string.nothing_found);
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<FollowRequest>> call, Throwable t) {

                }
            });
        }
    }

    private Callback<BaseListModel<Activity>> callBack = new Callback<BaseListModel<Activity>>() {
        @Override
        public void onResponse(Call<BaseListModel<Activity>> call, Response<BaseListModel<Activity>> response) {
            if (mContext != null) {
                isLoading = false;
                if (notificationAdapter.isLoaderShowing())
                    notificationAdapter.hideLoading();
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    BaseListModel<Activity> postResponse = response.body();
                    if (postResponse.getData() == null || postResponse.getData().isEmpty()) {
                        allDone = true;
                        if (notificationAdapter.itemsList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyViewContainer.setVisibility(View.VISIBLE);
                            empty_view_text.setText(R.string.nothing_found);
                        }
                    } else {
                        notificationAdapter.addItemsAtBottom(postResponse.getData());
                    }
                } else {
                    ApiError apiError = ErrorUtils.parseError(response);
                    Toast.makeText(mContext, apiError.status() == 417 ? getString(R.string.admin_block) : TextUtils.isEmpty(apiError.message()) ? getString(R.string.something_wrong) : apiError.message(), Toast.LENGTH_LONG).show();
                }
                recyclerView.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.GONE : View.VISIBLE);
                emptyViewContainer.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.VISIBLE : View.GONE);
                empty_view_text.setText(R.string.nothing_found);
            }
        }

        @Override
        public void onFailure(Call<BaseListModel<Activity>> call, Throwable t) {
            if (mContext != null) {
                isLoading = false;
                if (notificationAdapter.isLoaderShowing())
                    notificationAdapter.hideLoading();
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
                recyclerView.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.GONE : View.VISIBLE);
                emptyViewContainer.setVisibility(notificationAdapter.itemsList.isEmpty() ? View.VISIBLE : View.GONE);
                empty_view_text.setText(R.string.nothing_found);
                empty_view_text.setText(R.string.something_wrong);
            }
        }
    };

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
                    notificationAdapter.showLoading();
                    loadPosts();
                }
            }
        }
    };
}
