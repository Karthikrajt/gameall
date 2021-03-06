package com.siragu.gameall.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;
import com.siragu.gameall.BuildConfig;
import com.siragu.gameall.adapter.UniversalPagerAdapter;
import com.siragu.gameall.fragment.MyChatsFragment;
import com.siragu.gameall.fragment.PostTypeFragment;
import com.siragu.gameall.fragment.CommentsFragment;
import com.siragu.gameall.fragment.HomeFragment;
import com.siragu.gameall.fragment.NotificationFragment;
import com.siragu.gameall.fragment.PostFragment;
import com.siragu.gameall.fragment.ProfileFragment;
import com.siragu.gameall.fragment.SearchUserFragment;
import com.siragu.gameall.listener.OnFragmentStateChangeListener;
import com.siragu.gameall.R;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.siragu.gameall.util.SpringAnimationHelper;
import com.siragu.gameall.view.NonSwipeableViewPager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnFragmentStateChangeListener {
    public static final int REQUEST_CODE_DETAIL_ACTIVITY = 0;
    public static final int REQUEST_CODE_ADAPTER = 1;
    final String FRAG_TAG_SEARCH_USER = "fragSearchUser";
    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private static String USER_SELECT_TAG = "userselectdialog";
    private static String CONFIRM_TAG = "confirmtag";

    LinearLayout bottomBar;
    NonSwipeableViewPager viewPager;
    Toolbar toolbar;
    TextView tvTitle, actionBuy;
    LinearLayout homeTitleContainer;
    ImageView homeTitleLogo;
    LinearLayout[] bottomImageViews = new LinearLayout[5];
    private SharedPreferenceUtil sharedPreferenceUtil;
    private UniversalPagerAdapter adapter;
    private MenuItem menuSearch;
    private SearchUserFragment searchUserFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (BuildConfig.ENABLE_ADMOB) {
            MobileAds.initialize(getApplicationContext(), initializationStatus -> {
            });
        }*/
        setContentView(R.layout.activity_main);
        bottomBar = findViewById(R.id.bottom_bar);
        viewPager = findViewById(R.id.main_activity_view_pager);
        tvTitle = findViewById(R.id.tv_title);
        actionBuy = findViewById(R.id.actionBuy);
        homeTitleContainer = findViewById(R.id.ll_top);
        homeTitleLogo = findViewById(R.id.toolbarLogo);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.MontserratBoldTextAppearance);

        bottomImageViews[0] = findViewById(R.id.bottom_bar_tab1);
        bottomImageViews[1] = findViewById(R.id.bottom_bar_tab2);
        bottomImageViews[2] = findViewById(R.id.bottom_bar_tab3);
        bottomImageViews[3] = findViewById(R.id.bottom_bar_tab4);
        bottomImageViews[4] = findViewById(R.id.bottom_bar_tab5);
        for (LinearLayout linearLayout : bottomImageViews)
            linearLayout.setOnClickListener(this);

        actionBuy.setOnClickListener(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvTitle.setText(getString(R.string.app_name).toUpperCase());
        //tvTitle.setTypeface(Helper.getMontserratBold(this));

        sharedPreferenceUtil = new SharedPreferenceUtil(this);

        adapter = new UniversalPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new HomeFragment(), getString(R.string.app_name).toUpperCase());
        adapter.addFrag(new MyChatsFragment(), getString(R.string.chats).toUpperCase());
        adapter.addFrag(new NotificationFragment(), getString(R.string.notification).toUpperCase());
        adapter.addFrag(new ProfileFragment(), getString(R.string.profile).toUpperCase());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

        selectTabIndex(0);
        updateFcmToken();

        if (BuildConfig.IS_DEMO) {
            actionBuy.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String chatChildToRefreshLastReads = Helper.getChatChildToRefreshUnreadIndicatorFor(sharedPreferenceUtil);
        if (!TextUtils.isEmpty(chatChildToRefreshLastReads)) {
            MyChatsFragment groupChatsFragment = null;
            if (adapter != null && adapter.getCount() >= 2)
                groupChatsFragment = ((MyChatsFragment) adapter.getItem(1));
            if (groupChatsFragment != null)
                groupChatsFragment.refreshUnreadIndicatorFor(chatChildToRefreshLastReads, true);
        }
        Helper.clearRefreshUnreadIndicatorFor(sharedPreferenceUtil);
    }

    private void updateFcmToken() {
        OneSignal.idsAvailable((userId, registrationId) -> {
            if (userId != null) {
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                UserResponse userResponse = Helper.getLoggedInUser(sharedPreferenceUtil);
                if (userResponse != null) {
                    firebaseDatabase.getReference(Constants.REF_USER).child(userResponse.getId().toString()).child("userPlayerId").setValue(userId);
                }
            }
        });
//        OneSignal.addSubscriptionObserver(stateChanges -> {
//            Log.d("addSubscriptionObserver", stateChanges.toString());
//            if (!stateChanges.getFrom().getSubscribed() && stateChanges.getTo().getSubscribed()) {
//                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//                UserResponse userResponse = Helper.getLoggedInUser(sharedPreferenceUtil);
//                if (userResponse != null) {
//                    firebaseDatabase.getReference(Constants.REF_USER).child(userResponse.getId().toString()).child("userPlayerId").setValue(OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId());
//                }
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menuSearch = menu.getItem(0);

        final SearchView searchView = (SearchView) this.menuSearch.getActionView();

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(Color.parseColor("#cacaca"));
        searchEditText.setHint(getString(R.string.hint_search_users));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (getSupportFragmentManager().findFragmentByTag(FRAG_TAG_SEARCH_USER) == null) {
                    searchUserFragment = SearchUserFragment.newInstance(query);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.bottom_up, R.anim.bottom_down, R.anim.bottom_up, R.anim.bottom_down)
                            .add(R.id.frameLayout, searchUserFragment, FRAG_TAG_SEARCH_USER)
                            .addToBackStack(null)
                            .commit();
                } else {
                    searchUserFragment = (SearchUserFragment) getSupportFragmentManager().findFragmentByTag(FRAG_TAG_SEARCH_USER);
                    searchUserFragment.newQuery(query);
                }
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        menuSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                if (getSupportFragmentManager().findFragmentByTag(FRAG_TAG_SEARCH_USER) != null)
                    getSupportFragmentManager().popBackStackImmediate();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.actionDelete:
//                confirmDelete();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    private void confirmDelete() {
//        FragmentManager manager = getSupportFragmentManager();
//        Fragment frag = manager.findFragmentByTag(CONFIRM_TAG);
//        if (frag != null) {
//            manager.beginTransaction().remove(frag).commit();
//        }
//
//        ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance("Delete chat",
//                "Continue deleting selected chats?",
//                "Yes",
//                "No",
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        MyChatsFragment myChatsFragment = (MyChatsFragment) adapter.getItem(1);
//                        if (myChatsFragment != null) myChatsFragment.deleteSelectedChats();
//                        disableContextualMode();
//                    }
//                },
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        disableContextualMode();
//                    }
//                });
//        confirmationDialogFragment.show(manager, CONFIRM_TAG);
//    }

//    private void initialiseAdMob() {
//        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
//    }

    /**
     * Highlights the view and shows the discription to the user. It is used to tell the user about the features
     */
//    private void showTapTargetView() {
//        if (sharedPreferenceUtil.getBooleanPreference(Constants.KEY_SHOW_TAP_TARGET_VIEW, true)) {
//            new TapTargetSequence(this)
//                    .targets(
//                            TapTarget
//                                    .forView(bottomImageViews[4], "Your Profile", "Your profile contains your total score, your posts, your activity logs etc.")
//                                    .cancelable(false)
//                                    .tintTarget(false)
//                                    .descriptionTextColor(android.R.color.black),
//                            TapTarget
//                                    .forView(bottomImageViews[0], "Post Anonymously!!", "Yes, you can compressAndUpload textual post, picture and video without revealing your identity")
//                                    .cancelable(false)
//                                    .tintTarget(false)
//                                    .descriptionTextColor(android.R.color.black))
//                    .start();
//            sharedPreferenceUtil.setBooleanPreference(Constants.KEY_SHOW_TAP_TARGET_VIEW, false);
//        }
//    }
    public void selectTabIndex(int index) {
        for (int i = 0; i < bottomImageViews.length; i++) {
            if (i == index) {
                SpringAnimationHelper.performAnimation(bottomImageViews[i]);
                int currentItem = i;
                if (currentItem > 2)
                    currentItem--;
                final int finalCurrentItem = currentItem;

                viewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(finalCurrentItem);
                    }
                });
                homeTitleLogo.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
                tvTitle.setText(adapter.getPageTitle(currentItem));
//                getSupportActionBar().setDisplayShowTitleEnabled(index != 0);
//                getSupportActionBar().setTitle(adapter.getPageTitle(currentItem));
                //bottomImageViews[i].setBackgroundResource(R.drawable.top_border_primary_dark);
                bottomImageViews[i].setAlpha(1f);
            } else if (i != 2) {
                //bottomImageViews[i].setBackgroundResource(0);
                bottomImageViews[i].setAlpha(0.4f);
            }
        }
        if (menuSearch != null) {
            menuSearch.setVisible(index == 0);
        }
    }

    public void onHomeTabClicked() {
        selectTabIndex(0);
    }

    public void onBookmarksTabClicked() {
        selectTabIndex(1);
    }


    /**
     * Displays Fragment to post icon_text, icon_picture or video
     */
    public void onAddTabClicked() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager.findFragmentByTag(PostTypeFragment.class.getName()) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.bottom_up, R.anim.bottom_down, R.anim.bottom_up, R.anim.bottom_down)
                    .add(R.id.frameLayout, new PostTypeFragment(), PostTypeFragment.class.getName())
                    .addToBackStack(null)
                    .commit();

            setAddNewView(false);
            if (BuildConfig.IS_DEMO) {
                actionBuy.setVisibility(View.GONE);
            }
        } else {
            supportFragmentManager.popBackStackImmediate();
            if (BuildConfig.IS_DEMO) {
                actionBuy.setVisibility(View.VISIBLE);
            }
        }
    }

    public void openPostFragment(String type) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(PostTypeFragment.class.getName());
        if (fragment != null) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        if (getSupportFragmentManager().findFragmentByTag(PostFragment.class.getName()) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frameLayout, PostFragment.newInstance(type), PostFragment.class.getName())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void setAddNewView(boolean invisible) {
        int currentItem = viewPager.getCurrentItem();
        for (int i = 0; i < bottomImageViews.length; i++) {
            if (i == 2) {
                bottomImageViews[i].animate().setDuration(200).rotationBy(invisible ? -45 : 45).start();
            } else {
                bottomImageViews[i].setClickable(invisible);
                bottomImageViews[i].setFocusable(invisible);
                if (i == currentItem) {
                    //bottomImageViews[i].setBackgroundResource(invisible ? R.drawable.top_border_primary_dark : 0);
                    bottomImageViews[i].setAlpha(invisible ? 1f : 0.4f);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = Helper.getCurrentFragment(this);
        if (fragment instanceof CommentsFragment) {
            getSupportFragmentManager().popBackStackImmediate();
            return;
        } else if (viewPager.getCurrentItem() != 0) {
            onHomeTabClicked();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getSupportFragmentManager().findFragmentByTag(PostFragment.class.getName()) != null) {
            getSupportFragmentManager().findFragmentByTag(PostFragment.class.getName()).onActivityResult(requestCode, resultCode, data);
        }
    }

//    @Override
//    void myUsersResult(ArrayList<UserRealm> myUsers) {
//        MyChatsFragment myChatsFragment = (MyChatsFragment) adapter.getItem(1);
//        if (myChatsFragment != null) myChatsFragment.notifyMyUsersUpdate(myUsers);
//    }

    public void hideBottomBar() {
        Animation slide_down = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        slide_down.setFillAfter(true);
        slide_down.setDuration(200);
        slide_down.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomBar.startAnimation(slide_down);
    }

    public void showBottomBar() {
        Animation slide_up = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slide_up.setFillAfter(true);
        slide_up.setDuration(200);
        bottomBar.setVisibility(View.VISIBLE);
        bottomBar.startAnimation(slide_up);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom_bar_tab3:
                onAddTabClicked();
                break;
            case R.id.bottom_bar_tab2:
                selectTabIndex(1);
                break;
            case R.id.bottom_bar_tab1:
                selectTabIndex(0);
                break;
            case R.id.bottom_bar_tab5:
                selectTabIndex(4);
                break;
            case R.id.bottom_bar_tab4:
                selectTabIndex(3);
                break;
            case R.id.actionBuy:
                if (!TextUtils.isEmpty(BuildConfig.DEMO_ACTION_LINK)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DEMO_ACTION_LINK)));
                }
                break;
        }
    }

    @Override
    public void onDetachPostTypeFragment() {
    }

    @Override
    public void onPausePostTypeFragment() {
        setAddNewView(true);
    }

    @Override
    public void onOtherPostTypeFragment(String postType) {
        openPostFragment(postType);
        selectTabIndex(0);
    }

//    @Override
//    public void enableContextualMode() {
//        if (menuDelete != null) menuDelete.setVisible(true);
//    }
//
//    @Override
//    public boolean isContextualMode() {
//        return menuDelete != null && menuDelete.isVisible();
//    }
//
//    @Override
//    public void updateSelectedCount(int count) {
//        if (count > 0) {
//            tvTitle.setText(String.format("%d selected", count));
//        } else {
//            disableContextualMode();
//        }
//    }
//
//    @Override
//    public void OnUserClick(UserRealm user, int position, View userImage) {
//        Intent intent = MessagesActivity.newIntent(this, null, user);
//        startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD);
//    }
//
//    public void disableContextualMode() {
//        if (menuDelete != null) menuDelete.setVisible(false);
//        tvTitle.setText(adapter.getPageTitle(viewPager.getCurrentItem()));
//        MyChatsFragment myChatsFragment = (MyChatsFragment) adapter.getItem(1);
//        if (myChatsFragment != null) myChatsFragment.disableContextualMode();
//    }
}