package com.siragu.gameall.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.siragu.gameall.activity.BookmarksActivity;
import com.siragu.gameall.activity.FollowerFollowingActivity;
import com.siragu.gameall.R;
import com.siragu.gameall.activity.SettingsActivity;
import com.siragu.gameall.network.ApiError;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.network.ErrorUtils;
import com.siragu.gameall.network.response.ProfileResponse;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.siragu.gameall.network.ApiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private ImageView userImage;
    private TextView userPostsCount, userFollowersCount, userFollowingCount;
    private TextView userName;
    private ProgressBar profileRefreshProgress;

    private DrService foxyService;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private ProfileResponse profileMe;
    private UserResponse userMe;
    private FloatingActionButton fab_setting, fab_bookmarks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferenceUtil = new SharedPreferenceUtil(getContext());
        foxyService = ApiUtils.getClient().create(DrService.class);
        userMe = Helper.getLoggedInUser(sharedPreferenceUtil);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        userImage = view.findViewById(R.id.userImage);
        userName = view.findViewById(R.id.fullName);
        userPostsCount = view.findViewById(R.id.userPostsCount);
        userFollowersCount = view.findViewById(R.id.userFollowersCount);
        userFollowingCount = view.findViewById(R.id.userFollowingCount);
        profileRefreshProgress = view.findViewById(R.id.profileRefreshProgress);
        view.findViewById(R.id.followerCountContainer).setOnClickListener(view1 -> {
            if (userMe != null)
                startActivity(FollowerFollowingActivity.newInstance(getContext(), userMe.getId().toString(), "Followers"));
        });
        view.findViewById(R.id.followingCountContainer).setOnClickListener(view12 -> {
            if (userMe != null)
                startActivity(FollowerFollowingActivity.newInstance(getContext(), userMe.getId().toString(), "Followings"));
        });

        if (userMe != null)
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profileFrame, HomeFeedsFragment.newInstance("study_me", userMe.getId().toString(), false), "my_feed")
                    .commit();

        fab_setting = view.findViewById(R.id.fab_setting);
        fab_bookmarks = view.findViewById(R.id.fab_bookmarks);
        fab_setting.setOnClickListener(v -> startActivity(new Intent(getContext(), SettingsActivity.class)));
        fab_bookmarks.setOnClickListener(v -> startActivity(new Intent(getContext(), BookmarksActivity.class)));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (profileMe != null) {
            setDetails();
        }
        if (userMe != null) {
            refreshProfile();
        }
    }

    private void refreshFeeds() {
        HomeFeedsFragment myFeedsFragment = (HomeFeedsFragment) getChildFragmentManager().findFragmentByTag("my_feed");
        if (myFeedsFragment != null) myFeedsFragment.refresh();
    }

    private void refreshProfile() {
        profileRefreshProgress.setVisibility(View.VISIBLE);
        foxyService.getProfile(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), String.valueOf(userMe.getId())).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                profileRefreshProgress.setVisibility(View.INVISIBLE);
                if (response.isSuccessful()) {
                    profileMe = response.body();
                    setDetails();
//                    if (TextUtils.isEmpty(profileMe.getName())) {
//                        startActivity(EditProfileActivityActivity.newInstance(getContext(), profileMe, true));
//                    }
                } else {
                    ApiError apiError = ErrorUtils.parseError(response);
                    Toast.makeText(getContext(), apiError.status() == 417 ? getString(R.string.admin_block) : TextUtils.isEmpty(apiError.message()) ? getString(R.string.something_wrong) : apiError.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                profileRefreshProgress.setVisibility(View.INVISIBLE);
                t.getMessage();
            }
        });
    }

    private void setDetails() {
        if (profileMe == null) profileMe = new ProfileResponse();
        Glide.with(getContext()).load(userMe.getImage()).apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(getContext(), 8))).placeholder(R.drawable.ic_person_white_72dp)).into(userImage);
        userName.setText(userMe.getName());
        userPostsCount.setText(String.valueOf(profileMe.getPosts_count()));
        userFollowersCount.setText(String.valueOf(profileMe.getFollowers_count()));
        userFollowingCount.setText(String.valueOf(profileMe.getFollowing_count()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userMe != null) {
            refreshProfile();

            if (sharedPreferenceUtil.getBooleanPreference(Constants.KEY_UPDATED, false)) {
                sharedPreferenceUtil.setBooleanPreference(Constants.KEY_UPDATED, false);
                userMe = Helper.getLoggedInUser(sharedPreferenceUtil);

                Glide.with(getContext()).load(userMe.getImage()).apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(getContext(), 8))).placeholder(R.drawable.ic_person_white_72dp)).into(userImage);
                userName.setText(userMe.getName());
                new Handler().postDelayed(() -> LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.PROFILE_CHANGE_EVENT)), 200);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && userMe != null) {
            refreshProfile();
            refreshFeeds();
        }
    }
}
