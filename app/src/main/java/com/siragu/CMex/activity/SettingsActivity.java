package com.siragu.CMex.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.siragu.CMex.fragment.ConfirmationDialogFragment;
import com.siragu.CMex.R;
import com.siragu.CMex.fragment.SettingsPushNotification;
import com.siragu.CMex.network.response.UserResponse;
import com.siragu.CMex.util.Constants;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.SharedPreferenceUtil;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private ActionBar actionBar;
    private FragmentManager supportFragmentManager;
    private static String CONFIRM_TAG = "confirmtag";

    TextView[] textViews = new TextView[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        textViews[0] = findViewById(R.id.settings_profile_edit);
        textViews[1] = findViewById(R.id.settings_push_notification);
        textViews[2] = findViewById(R.id.feedback);
        textViews[3] = findViewById(R.id.share);
        textViews[4] = findViewById(R.id.rate);
        textViews[5] = findViewById(R.id.logout);

        findViewById(R.id.ll_top).setVisibility(View.GONE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.MontserratBoldTextAppearance);
        toolbar.setTitle(R.string.title_settings);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left);
        }
        supportFragmentManager = getSupportFragmentManager();

     /*   AdView mAdView = findViewById(R.id.adView);
        if (BuildConfig.ENABLE_ADMOB) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }*/

        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setOnClickListener(this);
            Animation inAnimation = AnimationUtils.makeInAnimation(this, false);
            inAnimation.setDuration(500);
            inAnimation.setStartOffset(i * 100);
            textViews[i].startAnimation(inAnimation);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.activity_settings_menu_done).setVisible(false);
        setActionBarSubtitle("");
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void setActionBarSubtitle(String subtitle) {
        actionBar.setSubtitle(subtitle);
    }

    /**
     * Opens appropriate settings fragment according to the selection by the user
     *
     * @param view
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings_profile_edit:
                UserResponse profileMe = Helper.getLoggedInUser(new SharedPreferenceUtil(this));
                if (profileMe != null)
                    startActivity(EditProfileActivityActivity.newInstance(this, profileMe, false));
                break;
            case R.id.settings_push_notification:
                openFragment(new SettingsPushNotification(), SettingsPushNotification.class.getName());
                break;
            case R.id.share:
                Helper.openShareIntent(this, null, "http://play.google.com/store/apps/details?id=" + getPackageName());
                break;
            case R.id.rate:
                Helper.openPlayStoreIntent(this);
                break;
            case R.id.feedback:
                startActivity(new Intent(this, FeedbackActivity.class));
                break;
            case R.id.logout:
                FragmentManager manager = getSupportFragmentManager();
                Fragment frag = manager.findFragmentByTag(CONFIRM_TAG);
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }

                ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance("Logout",
                        getString(R.string.sure_logout),
                        getString(R.string.logout),
                        getString(R.string.cancel),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseAuth.getInstance().signOut();
                              //  LoginManager.getInstance().logOut();
                                logout();
                                Intent intent = new Intent(SettingsActivity.this, SplashScreenActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                confirmationDialogFragment.show(manager, CONFIRM_TAG);
                break;
        }
    }

    private void logout() {
        SharedPreferenceUtil sharedPreferenceUtil = new SharedPreferenceUtil(SettingsActivity.this);
        sharedPreferenceUtil.removePreference(Constants.USER);
        sharedPreferenceUtil.removePreference(Constants.NOTIFICATION_SETTING);
    }

    /**
     * It opens the fragment object passed to the function
     *
     * @param fragment     Fragment to be opened
     * @param fragmentName Tag name of the Fragment
     */
    private void openFragment(Fragment fragment, String fragmentName) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.activity_settings_container, fragment, fragmentName)
                .addToBackStack(fragmentName)
                .commit();
    }
}
