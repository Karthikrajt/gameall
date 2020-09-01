package com.siragu.gameall.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.siragu.gameall.R;
import com.siragu.gameall.fragment.ConfirmationDialogFragment;
import com.siragu.gameall.fragment.HomeFeedsFragment;
import com.siragu.gameall.model.Chat;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.network.request.ReportUserRequest;
import com.siragu.gameall.network.response.ProfileFollowRequestResponse;
import com.siragu.gameall.network.response.ProfileResponse;
import com.siragu.gameall.network.response.ProfileFollowResponse;
import com.siragu.gameall.network.response.ReportUserResponse;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by a_man on 09-02-2018.
 */

public class UserProfileDetailActivity extends AppCompatActivity {
    private static String EXTRA_DATA_USER_ID = "UserResponseId";
    private static String EXTRA_DATA_USER_NAME = "UserResponseName";
    private static String EXTRA_DATA_USER_IMAGE = "UserResponseImage";
    private static String CONFIRM_TAG = "confirmtag";

    private ImageView profileImage;
    private TextView userPostsCount, userFollowersCount, userFollowingCount;
    private TextView profileName;
    private ProgressBar progressBar;
    private FloatingActionButton floatingActionButton;
    private View followerCountContainer, followingCountContainer;

    private DrService owhloService;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private ProfileResponse userProfile;

    private String userId;
    private String userName, userImage;
    private UserResponse userMe;
    private MenuItem menuActionReport;

    private BottomSheetBehavior sheetBehavior;
    private RadioGroup radioGroupReportReasons;
    private Button reportConfirm;
    private Context mContext;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        mContext = this;
        owhloService = ApiUtils.getClient().create(DrService.class);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);

        Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_DATA_USER_ID) || !intent.hasExtra(EXTRA_DATA_USER_NAME) || !intent.hasExtra(EXTRA_DATA_USER_IMAGE)) {
            finish();
        } else {
            userId = intent.getStringExtra(EXTRA_DATA_USER_ID);
            userName = intent.getStringExtra(EXTRA_DATA_USER_NAME);
            userImage = intent.getStringExtra(EXTRA_DATA_USER_IMAGE);
            userMe = Helper.getLoggedInUser(sharedPreferenceUtil);
            initUi();
            setDetails();
            loadDetails();
        }

    }

    private void loadDetails() {
        owhloService.getProfile(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), userId).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (mContext != null) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        userProfile = response.body();
                        setDetails();
                        refreshFeeds();
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                if (mContext != null) {
                    progressBar.setVisibility(View.GONE);
                    t.getMessage();
                }
            }
        });
    }

    private void setDetails() {
        Glide.with(this)
                .load(userProfile != null ? userProfile.getImage() : userImage)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(this, 8))).placeholder(R.drawable.ic_person_white_72dp))
                .into(profileImage);
        Log.d("profileImage", userProfile != null ? userProfile.getImage() : userImage);
        profileName.setText(userProfile != null ? userProfile.getName() : userName);
        if (userProfile != null) {
            userPostsCount.setText(String.valueOf(userProfile.getPosts_count()));
            userFollowersCount.setText(String.valueOf(userProfile.getFollowers_count()));
            userFollowingCount.setText(String.valueOf(userProfile.getFollowing_count()));
            floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, userProfile.getIs_following() == 1 ? R.drawable.ic_done_white_24dp : userProfile.getIs_follow_requested() == 1 ? R.drawable.ic_person_white_24dp : R.drawable.ic_person_add_white_24dp));
            UserResponse userResponse = Helper.getLoggedInUser(sharedPreferenceUtil);
            if (menuActionReport != null)
                menuActionReport.setVisible(!(userResponse != null ? userResponse.getId().toString() : "-1").equals(userId));
        }
        followerCountContainer.setClickable(userProfile != null && userProfile.getIs_following() == 1);
        followingCountContainer.setClickable(userProfile != null && userProfile.getIs_following() == 1);
    }

    private void initUi() {
        findViewById(R.id.ll_top).setVisibility(View.GONE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left);
            toolbar.setTitleTextAppearance(this, R.style.MontserratBoldTextAppearance);
            actionBar.setTitle(userName);
        }
        profileImage = findViewById(R.id.userImage);
        profileName = findViewById(R.id.fullName);
        userPostsCount = findViewById(R.id.userPostsCount);
        userFollowersCount = findViewById(R.id.userFollowersCount);
        userFollowingCount = findViewById(R.id.userFollowingCount);

        sheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheetReport));
        radioGroupReportReasons = findViewById(R.id.radioGroup);
        reportConfirm = findViewById(R.id.reportConfirm);
        reportConfirm.setOnClickListener(v -> {
            RadioButton radioButton = findViewById(radioGroupReportReasons.getCheckedRadioButtonId());
            if (radioButton != null) {
                reportUser(radioButton.getText().toString());
                //confirmReport();
            }
        });

        progressBar = findViewById(R.id.profileRefreshProgress);
        floatingActionButton = findViewById(R.id.fab_setting);
        UserResponse userResponse = Helper.getLoggedInUser(sharedPreferenceUtil);
        if ((userResponse != null ? userResponse.getId().toString() : "-1").equals(userId))
            floatingActionButton.hide();
        else
            floatingActionButton.show();
        //floatingActionButton.setVisibility((userResponse != null ? userResponse.getId() : -1) == userId ? View.GONE : View.VISIBLE);
        floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_person_add_white_24dp));
        floatingActionButton.setOnClickListener(view -> actionProfile());
        followerCountContainer = findViewById(R.id.followerCountContainer);
        followingCountContainer = findViewById(R.id.followingCountContainer);
        followerCountContainer.setOnClickListener(v -> {
            if (userProfile != null)
                startActivity(FollowerFollowingActivity.newInstance(mContext, userProfile.getId().toString(), "Followers"));
        });
        followingCountContainer.setOnClickListener(v -> {
            if (userProfile != null)
                startActivity(FollowerFollowingActivity.newInstance(mContext, userProfile.getId().toString(), "Followings"));
        });
        followerCountContainer.setClickable(userProfile != null && userProfile.getIs_following() == 1);
        followingCountContainer.setClickable(userProfile != null && userProfile.getIs_following() == 1);
        FloatingActionButton floatingChatActionButton = findViewById(R.id.fab_bookmarks);
        floatingChatActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_message_white_24dp));
        floatingChatActionButton.setOnClickListener(v -> {
            if (userProfile != null) {
                Gson gson = new Gson();
                String jsonStr = gson.toJson(userProfile);

                Log.d("Chating Details",  jsonStr + "  "+gson.toJson(userMe));

                startActivity(MessagesActivity.newIntent(mContext, new Chat(userMe, userProfile)));
            }
        });
        if ((userResponse != null ? userResponse.getId().toString() : "-1").equals(userId))
            floatingChatActionButton.hide();
        else
            floatingChatActionButton.show();

        //inflateFeedsView();
    }

    private void actionProfile() {
        if (userProfile != null) {
            floatingActionButton.setClickable(false);
            progressBar.setVisibility(View.VISIBLE);
            if (userProfile.getIs_following() == 1 || userProfile.getIs_private() == 0) {
                owhloService.profileFollowAction(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), userId).enqueue(new Callback<ProfileFollowResponse>() {
                    @Override
                    public void onResponse(Call<ProfileFollowResponse> call, Response<ProfileFollowResponse> response) {
                        if (mContext != null) {
                            if (response.isSuccessful())
                                userProfile.setIs_following(response.body().isFollowed() ? 1 : 0);
                            refreshFeeds();
                            floatingActionButton.setClickable(true);
                            progressBar.setVisibility(View.GONE);
                            followerCountContainer.setClickable(response.isSuccessful() && response.body().isFollowed());
                            followingCountContainer.setClickable(response.isSuccessful() && response.body().isFollowed());
                            if (response.isSuccessful() && response.body().getSuccess() != 0) {
                                floatingActionButton.setImageDrawable(ContextCompat.getDrawable(mContext, response.body().isFollowed() ? R.drawable.ic_done_white_24dp : R.drawable.ic_person_add_white_24dp));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileFollowResponse> call, Throwable t) {
                        if (mContext != null) {
                            floatingActionButton.setClickable(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                owhloService.profileFollowActionRequest(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), userId).enqueue(new Callback<ProfileFollowRequestResponse>() {
                    @Override
                    public void onResponse(Call<ProfileFollowRequestResponse> call, Response<ProfileFollowRequestResponse> response) {
                        if (mContext != null) {
                            floatingActionButton.setClickable(true);
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                floatingActionButton.setImageDrawable(ContextCompat.getDrawable(mContext, response.body().getFollow_request() ? R.drawable.ic_person_white_24dp : R.drawable.ic_person_add_white_24dp));
                                Toast.makeText(mContext, getString(response.body().getFollow_request() ? R.string.follow_request_added : R.string.follow_request_removed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileFollowRequestResponse> call, Throwable t) {
                        if (mContext != null) {
                            floatingActionButton.setClickable(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }

    private void inflateFeedsView() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profileFrame, HomeFeedsFragment.newInstance("study", userId, false), "user_feed")
                .commit();

        new Handler().postDelayed(() -> {
            TextView empty_view_text = findViewById(R.id.empty_view_text);
            if (empty_view_text != null) {
                empty_view_text.setText(getString(R.string.need_follow) + " " + userName + " " + getString(R.string.tosee_posts));
            }
            TextView empty_view_sub_text = findViewById(R.id.empty_view_sub_text);
            if (empty_view_sub_text != null) {
                empty_view_sub_text.setVisibility(View.GONE);
            }
        }, 500);
    }

    private void refreshFeeds() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm != null) {
            HomeFeedsFragment homeFeedsFragment = (HomeFeedsFragment) fm.findFragmentByTag("user_feed");
            if (homeFeedsFragment == null)
                inflateFeedsView();
            else if (userProfile.getIs_following() == 1 || userProfile.getIs_private() == 0) {
                TextView empty_view_text = findViewById(R.id.empty_view_text);
                if (empty_view_text != null) {
                    empty_view_text.setText(getString(R.string.empty_feeds));
                }
                homeFeedsFragment.hideShowFeeds(true);
                homeFeedsFragment.refresh();
            } else {
                TextView empty_view_text = findViewById(R.id.empty_view_text);
                if (empty_view_text != null) {
                    empty_view_text.setText(getString(R.string.need_follow) + " " + userName + " " + getString(R.string.tosee_posts));
                }
                homeFeedsFragment.hideShowFeeds(false);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home_item, menu);
        menuActionReport = menu.findItem(R.id.action_report);
        menuActionReport.setVisible(false);
        menu.findItem(R.id.action_delete).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_report:
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmReport() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag(CONFIRM_TAG);
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }

        ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(getString(R.string.report_user),
                getString(R.string.report_user_confirm),
                getString(R.string.yes),
                getString(R.string.no),
                view -> {
                    RadioButton radioButton = findViewById(radioGroupReportReasons.getCheckedRadioButtonId());
                    if (radioButton != null) {
                        reportUser(radioButton.getText().toString());
                    }
                },
                view -> {
                    if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                });
        confirmationDialogFragment.show(manager, CONFIRM_TAG);
    }

    private void reportUser(String reason) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        owhloService.reportUser(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), userId, new ReportUserRequest(reason)).enqueue(new Callback<ReportUserResponse>() {
            @Override
            public void onResponse(Call<ReportUserResponse> call, Response<ReportUserResponse> response) {
                if (mContext != null) {
                    if (response.isSuccessful()) {
                        Toast.makeText(mContext, getString(R.string.reported), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReportUserResponse> call, Throwable t) {
                if (mContext != null) {
                    t.getMessage();
                }
            }
        });
    }

    public static Intent newInstance(Context context, String userId, String userName, String userImage) {
        Intent intent = new Intent(context, UserProfileDetailActivity.class);
        intent.putExtra(EXTRA_DATA_USER_ID, userId);
        intent.putExtra(EXTRA_DATA_USER_NAME, userName);
        intent.putExtra(EXTRA_DATA_USER_IMAGE, userImage);
        return intent;
    }
}
