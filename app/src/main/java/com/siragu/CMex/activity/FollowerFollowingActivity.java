package com.siragu.CMex.activity;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.siragu.CMex.R;
import com.siragu.CMex.adapter.SearchUserResultAdapter;
import com.siragu.CMex.network.ApiUtils;
import com.siragu.CMex.network.DrService;
import com.siragu.CMex.network.response.BaseListModel;
import com.siragu.CMex.network.response.ProfileFollowRequestResponse;
import com.siragu.CMex.network.response.UserResponse;
import com.siragu.CMex.network.response.ProfileFollowResponse;
import com.siragu.CMex.util.Constants;
import com.siragu.CMex.util.SharedPreferenceUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowerFollowingActivity extends AppCompatActivity {
    private static String DATA_EXTRA_FOLLOW_FOLLOWING = "FollowOrFollowing";
    private static String DATA_EXTRA_PROFILE_ID = "ProfileId";

    private RecyclerView usersRecycler;
    private ArrayList<UserResponse> users = new ArrayList<>();
    private ProgressBar searchProgress;
    private DrService owhloService;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private boolean isLoading, allDone;
    private int pageNumber = 1;
    private TextView noResults;
    private String title;
    private String profileId;
    private Context mContext;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_following);
        mContext = this;
        owhloService = ApiUtils.getClient().create(DrService.class);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        title = getIntent().getStringExtra(DATA_EXTRA_FOLLOW_FOLLOWING);
        profileId = getIntent().getStringExtra(DATA_EXTRA_PROFILE_ID);
        initUi();
        loadResults();
        searchProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initUi() {
        findViewById(R.id.ll_top).setVisibility(View.GONE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.MontserratBoldTextAppearance);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(title.equalsIgnoreCase("followers") ? R.string.followers : R.string.following));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left);
        }

        usersRecycler = findViewById(R.id.usersRecycler);
        searchProgress = findViewById(R.id.searchProgress);
        noResults = findViewById(R.id.noResults);

        usersRecycler.setLayoutManager(new LinearLayoutManager(this));
        usersRecycler.addOnScrollListener(recyclerViewOnScrollListener);
        usersRecycler.setAdapter(new SearchUserResultAdapter(this, users, new SearchUserResultAdapter.SearchUserActionClickListener() {
            @Override
            public void onActionClick(final UserResponse user) {
                if (user.getIs_following() == 1 || user.getIs_private() == 0) {
                    owhloService.profileFollowAction(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), user.getId().toString()).enqueue(new Callback<ProfileFollowResponse>() {
                        @Override
                        public void onResponse(Call<ProfileFollowResponse> call, Response<ProfileFollowResponse> response) {
                            if (mContext != null) {
                                if (response.isSuccessful() && response.body().getSuccess() != 0) {
                                    user.setIs_following(response.body().isFollowed() ? 1 : 0);
                                    int index = users.indexOf(user);
                                    if (index == -1)
                                        usersRecycler.getAdapter().notifyDataSetChanged();
                                    else
                                        usersRecycler.getAdapter().notifyItemChanged(index);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ProfileFollowResponse> call, Throwable t) {

                        }
                    });
                } else {
                    owhloService.profileFollowActionRequest(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), user.getId().toString()).enqueue(new Callback<ProfileFollowRequestResponse>() {
                        @Override
                        public void onResponse(Call<ProfileFollowRequestResponse> call, Response<ProfileFollowRequestResponse> response) {
                            if (mContext != null) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(mContext, getString(response.body().getFollow_request() ? R.string.follow_request_added : R.string.follow_request_removed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ProfileFollowRequestResponse> call, Throwable t) {
                        }
                    });
                }
            }
        }));
    }

    private Callback<BaseListModel<UserResponse>> searchCallback = new Callback<BaseListModel<UserResponse>>() {
        @Override
        public void onResponse(Call<BaseListModel<UserResponse>> call, Response<BaseListModel<UserResponse>> response) {
            searchProgress.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                allDone = response.body().getData().isEmpty();
                users.addAll(response.body().getData());
                usersRecycler.getAdapter().notifyDataSetChanged();
                if (users.isEmpty())
                    noResults.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailure(Call<BaseListModel<UserResponse>> call, Throwable t) {
            searchProgress.setVisibility(View.GONE);
            noResults.setVisibility(View.VISIBLE);
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
                    loadResults();
                }
            }
        }
    };

    private void loadResults() {
        Call<BaseListModel<UserResponse>> call;
        if (title.equalsIgnoreCase("Followers"))
            call = owhloService.getFollowers(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), profileId, pageNumber);
        else
            call = owhloService.getFollowings(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), profileId, pageNumber);
        call.enqueue(searchCallback);
        noResults.setVisibility(View.GONE);
    }

    public static Intent newInstance(Context context, String profileId, String follow) {
        Intent intent = new Intent(context, FollowerFollowingActivity.class);
        intent.putExtra(DATA_EXTRA_FOLLOW_FOLLOWING, follow);
        intent.putExtra(DATA_EXTRA_PROFILE_ID, profileId);
        return intent;
    }
}
