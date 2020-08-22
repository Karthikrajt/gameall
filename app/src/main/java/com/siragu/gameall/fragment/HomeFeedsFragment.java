package com.siragu.gameall.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.siragu.gameall.BuildConfig;
import com.siragu.gameall.adapter.HomeRecyclerAdapter;
import com.siragu.gameall.model.Post;
import com.siragu.gameall.R;
import com.siragu.gameall.network.ApiError;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.network.ErrorUtils;
import com.siragu.gameall.network.request.CreatePostRequest;
import com.siragu.gameall.network.response.CreatePostResponse;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.FirebaseUploader;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.response.BaseListModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays the feed
 */
public class HomeFeedsFragment extends Fragment implements ImagePickerCallback {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private TextView empty_view_text;

    private LinearLayoutManager linearLayoutManager;
    private HomeRecyclerAdapter homeRecyclerAdapter;
    private String postType;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private int pageNumber = 1;
    private String userId;

    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;
    private String pickerPath;
    private File mediaFile;
    private FirebaseUploader firebaseUploader;
    private final int REQUEST_CODE_PERMISSION = 55;

    private static float MAX_SWIPE_DISTANCE_FACTOR = 0.6f;
    private static int DEFAULT_REFRESH_TRIGGER_DISTANCE = 200;
    private int refreshTriggerDistance = DEFAULT_REFRESH_TRIGGER_DISTANCE;

    private DrService weService;
    private boolean allDone, isLoading, showStory;

    private ArrayList<Post> bookmarkedPosts = new ArrayList<>();
    private Call<BaseListModel<Post>> getPosts;
    private Context mContext;

    private Callback<BaseListModel<Post>> callBack = new Callback<BaseListModel<Post>>() {
        @Override
        public void onResponse(Call<BaseListModel<Post>> call, Response<BaseListModel<Post>> response) {
            if (mContext != null) {
                isLoading = false;
                if (homeRecyclerAdapter != null && homeRecyclerAdapter.isLoaderShowing())
                    homeRecyclerAdapter.hideLoading();
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    BaseListModel<Post> postResponse = response.body();
                    if (postResponse.getData() == null || postResponse.getData().isEmpty()) {
                        if (homeRecyclerAdapter != null && homeRecyclerAdapter.itemsList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                        allDone = true;
                    } else {
                        if (homeRecyclerAdapter != null) {
                            if (bookmarkedPosts != null) {
                                for (Post post : postResponse.getData()) {
                                    post.setDisliked(bookmarkedPosts.contains(post) ? 1 : 0);
                                }
                            }
                            ArrayList<Post> postData = new ArrayList<>();
                           /* if (BuildConfig.ENABLE_ADMOB) {
                                int addedCount = 0;
                                for (Post np : postResponse.getData()) {
                                    postData.add(np);
                                    addedCount++;
                                    if (addedCount == 3) {
                                        postData.add(new Post("add"));
                                        addedCount = 0;
                                    }
                                }
                            } else {
                                postData.addAll(postResponse.getData());
                            }*/

                            postData.addAll(postResponse.getData());
                            homeRecyclerAdapter.addItemsAtBottom(postData);
                        }
                    }
                } else {
                    if (homeRecyclerAdapter != null && homeRecyclerAdapter.itemsList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    ApiError apiError = ErrorUtils.parseError(response);
                    Toast.makeText(mContext, apiError.status() == 417 ? "BLOCKED by admin." : TextUtils.isEmpty(apiError.message()) ? "Something went wrong." : apiError.message(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<BaseListModel<Post>> call, Throwable t) {
            if (mContext != null) {
                isLoading = false;
                if (homeRecyclerAdapter != null && homeRecyclerAdapter.isLoaderShowing())
                    homeRecyclerAdapter.hideLoading();
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
                if (homeRecyclerAdapter != null && homeRecyclerAdapter.itemsList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }
    };

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (layoutManager.getChildCount() > 0) {
                // Calculations..
                int indexOfLastItemViewVisible = layoutManager.getChildCount() - 1;
                View lastItemViewVisible = layoutManager.getChildAt(indexOfLastItemViewVisible);
                int adapterPosition = layoutManager.getPosition(lastItemViewVisible);
                boolean isLastItemVisible = (adapterPosition == adapter.getItemCount() - 1);
                if (!postType.equals("bookmark") && isLastItemVisible && !isLoading && !allDone) {
                    pageNumber++;
                    homeRecyclerAdapter.showLoading();
                    loadPosts();
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);

        mContext = getContext();
        sharedPreferenceUtil = new SharedPreferenceUtil(mContext);
        weService = ApiUtils.getClient().create(DrService.class);
        bookmarkedPosts = Helper.getBookmarkedPosts(sharedPreferenceUtil);

        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.frag_home_feeds_swipe_refresh_layout);
        emptyView = view.findViewById(R.id.empty_view_container);
        empty_view_text = view.findViewById(R.id.empty_view_text);
        linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        //recyclerView.setNestedScrollingEnabled(false);
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        //nestedScrollView.setOnScrollChangeListener(nestedScrollViewChangeListener);
        homeRecyclerAdapter = new HomeRecyclerAdapter(this);
        recyclerView.setAdapter(homeRecyclerAdapter);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Float mDistanceToTriggerSync = Math.min(swipeRefreshLayout.getHeight() * MAX_SWIPE_DISTANCE_FACTOR, refreshTriggerDistance * metrics.density);
        swipeRefreshLayout.setDistanceToTriggerSync(mDistanceToTriggerSync.intValue());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipeRefreshLayout.setRefreshing(true);

        loadPosts();
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        if (showStory) loadStoryUsers();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION && checkStoragePermissions() && mContext != null) {
            pickMedia();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Picker.PICK_IMAGE_DEVICE:
                    if (imagePicker == null) {
                        imagePicker = new ImagePicker(this);
                    }
                    imagePicker.submit(data);
                    break;
                case Picker.PICK_IMAGE_CAMERA:
                    if (cameraPicker == null) {
                        cameraPicker = new CameraImagePicker(this);
                        cameraPicker.reinitialize(pickerPath);
                    }
                    cameraPicker.submit(data);
                    break;
            }
        }
    }

    public void hideShowFeeds(boolean show) {
        if (mContext != null) {
            if (homeRecyclerAdapter != null && homeRecyclerAdapter.isLoaderShowing())
                homeRecyclerAdapter.hideLoading();
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            emptyView.setVisibility(show ? View.GONE : View.VISIBLE);
            recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void refresh() {
        if (mContext != null) {
            swipeRefreshLayout.setRefreshing(true);
            pageNumber = 1;
            homeRecyclerAdapter.clear();
            allDone = false;
            if (showStory) loadStoryUsers();
            loadPosts();
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void pickMedia() {
        if (checkStoragePermissions()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setMessage(R.string.story_media_from);
            alertDialog.setPositiveButton(getString(R.string.camera), (dialogInterface, i) -> {
                dialogInterface.dismiss();

                cameraPicker = new CameraImagePicker(HomeFeedsFragment.this);
                cameraPicker.shouldGenerateMetadata(true);
                cameraPicker.shouldGenerateThumbnails(true);
                cameraPicker.setImagePickerCallback(HomeFeedsFragment.this);
                pickerPath = cameraPicker.pickImage();
            });
            alertDialog.setNegativeButton(getString(R.string.gallery), (dialogInterface, i) -> {
                dialogInterface.dismiss();

                imagePicker = new ImagePicker(HomeFeedsFragment.this);
                imagePicker.shouldGenerateMetadata(true);
                imagePicker.shouldGenerateThumbnails(true);
                imagePicker.setImagePickerCallback(HomeFeedsFragment.this);
                imagePicker.pickImage();
            });
            alertDialog.create().show();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION);
        }
    }

    private boolean checkStoragePermissions() {
        return
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        &&
                        ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        &&
                        ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void setStoryProgress(boolean progress) {
        homeRecyclerAdapter.storyProgress(progress);
    }

    private void loadStoryUsers() {
        weService.getStoryUsers(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null)).enqueue(new Callback<ArrayList<UserResponse>>() {
            @Override
            public void onResponse(Call<ArrayList<UserResponse>> call, Response<ArrayList<UserResponse>> response) {
                if (response.isSuccessful() && mContext != null) {
                    homeRecyclerAdapter.storyShow(response.body());
                    recyclerView.smoothScrollToPosition(0);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<UserResponse>> call, Throwable t) {
                Log.e("CHECK_STORY", t.getMessage());
            }
        });
    }

    @Override
    public void onImagesChosen(List<ChosenImage> images) {
        mediaFile = new File(Uri.parse(images.get(0).getOriginalPath()).getPath());

        firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
            @Override
            public void onUploadFail(String message) {
                setStoryProgress(false);
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUploadSuccess(String downloadUrl) {
                post(new CreatePostRequest("StoryTitle", "StoryText", "image", downloadUrl));
            }

            @Override
            public void onUploadProgress(int progress) {
            }

            @Override
            public void onUploadCancelled() {
                setStoryProgress(false);
            }
        });
        firebaseUploader.uploadImage(mContext, mediaFile);
        setStoryProgress(true);
        Toast.makeText(mContext, R.string.uploading_story, Toast.LENGTH_SHORT).show();
    }

    private void post(CreatePostRequest createPostRequest) {
        createPostRequest.setIs_story(true);
        weService.createPost(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), createPostRequest).enqueue(new Callback<CreatePostResponse>() {
            @Override
            public void onResponse(Call<CreatePostResponse> call, Response<CreatePostResponse> response) {
                if (mContext != null) {
                    setStoryProgress(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(mContext, R.string.uploadded_story, Toast.LENGTH_SHORT).show();
                        CreatePostResponse postResponse = response.body();
                        homeRecyclerAdapter.storyShowMy(new UserResponse(postResponse.getUser_profile_id().getId(), postResponse.getUser_profile_id().getImage(), postResponse.getUser_profile_id().getName()));
                    }
                }
            }

            @Override
            public void onFailure(Call<CreatePostResponse> call, Throwable t) {
                if (mContext != null) setStoryProgress(false);
            }
        });
    }

    @Override
    public void onError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // You have to save path in case your activity is killed.
        // In such a scenario, you will need to re-initialize the CameraImagePicker
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("picker_path")) {
                pickerPath = savedInstanceState.getString("picker_path");
            }
        }
    }

    private void loadPosts() {
        if (postType != null && mContext != null) {
            if (postType.equals("bookmark")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (homeRecyclerAdapter.isLoaderShowing())
                            homeRecyclerAdapter.hideLoading();
                        if (swipeRefreshLayout.isRefreshing())
                            swipeRefreshLayout.setRefreshing(false);

                        homeRecyclerAdapter.addItemsAtBottom(bookmarkedPosts);
                        if (bookmarkedPosts.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    }
                }, 200);
            } else {
                isLoading = true;
                if (TextUtils.isEmpty(userId))
                    getPosts = weService.getPosts(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), postType.equals("hot") ? 1 : 0, pageNumber);
                else
                    getPosts = weService.getPostsByUserId(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), userId, postType.equals("hot") ? 1 : 0, pageNumber);
                getPosts.enqueue(callBack);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
        if (getPosts != null && !getPosts.isCanceled())
            getPosts.cancel();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && sharedPreferenceUtil != null && bookmarkedPosts != null && homeRecyclerAdapter != null) {
            ArrayList<Post> bps = Helper.getBookmarkedPosts(sharedPreferenceUtil);
            if (bookmarkedPosts.size() != bps.size()) {
                bookmarkedPosts.clear();
                bookmarkedPosts.addAll(bps);
                for (Post post : homeRecyclerAdapter.getItemsList()) {
                    post.setDisliked(bookmarkedPosts.contains(post) ? 1 : 0);
                }
                homeRecyclerAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.POST_CHANGE_EVENT);
        intentFilter.addAction(Constants.POST_NEW_EVENT);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(postEventReceiver, intentFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(profileEventReceiver, new IntentFilter(Constants.PROFILE_CHANGE_EVENT));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(postEventReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(profileEventReceiver);
    }

    private BroadcastReceiver profileEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (recyclerView != null && homeRecyclerAdapter != null && mContext != null) {
                UserResponse profileResponse = Helper.getLoggedInUser(sharedPreferenceUtil);
                if (profileResponse != null) {
                    boolean changed = false;
                    for (Post post : homeRecyclerAdapter.itemsList) {
                        if (!post.getId().equalsIgnoreCase("add") && post.getUserMetaData() != null && post.getUserMetaData().getId().toString().equals(userId)) {
                            if (!TextUtils.isEmpty(profileResponse.getName()))
                                post.getUserMetaData().setName(profileResponse.getName());
                            if (!TextUtils.isEmpty(profileResponse.getImage()))
                                post.getUserMetaData().setImage(profileResponse.getImage());
                            changed = true;
                        }
                    }
                    if (changed) {
                        homeRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private BroadcastReceiver postEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && mContext != null) {
                if (intent.getAction().equals(Constants.POST_CHANGE_EVENT)) {
                    Post post = intent.getParcelableExtra("post");
                    if (post != null) {
                        boolean isBookmarkEvent = intent.getBooleanExtra("bookmark", false);
                        if (homeRecyclerAdapter != null) {
                            if (isBookmarkEvent && postType.equals("bookmark") && post.getDisliked() == 0) {
                                homeRecyclerAdapter.findAndRemoveItem(post);
                            } else {
                                homeRecyclerAdapter.updateItem(post);
                            }
                        }
                        if (isBookmarkEvent && bookmarkedPosts != null) {
                            int pos = bookmarkedPosts.indexOf(post);
                            if (pos != -1) {
                                bookmarkedPosts.remove(pos);
                            } else {
                                bookmarkedPosts.add(post);
                            }
                            Helper.setBookmarkedPosts(sharedPreferenceUtil, bookmarkedPosts);
                        }
                    }
                } else if (intent.getAction().equals(Constants.POST_NEW_EVENT)) {
                    Post post = intent.getParcelableExtra("post");
                    //postType != null && postType.equals("bookmark")
                    if (post != null && postType.equals("study_me")) {
                        if (homeRecyclerAdapter.itemsList.isEmpty() || !homeRecyclerAdapter.getItem(0).getId().equalsIgnoreCase("story")) {
                            homeRecyclerAdapter.addItemOnTop(post);
                        } else {
                            homeRecyclerAdapter.addItemAtPos(post, 1);
                        }
                        recyclerView.scrollToPosition(0);
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

//    @OnClick(R.id.frag_home_feeds_refresh_indicator)
//    public void scrollToTop() {
//        if (recyclerView != null) {
//            recyclerView.scrollToPosition(0);
//        }
//        if (refreshIndicator != null) {
//            refreshIndicator.setVisibility(View.GONE);
//        }
//    }

    public static HomeFeedsFragment newInstance(String name, String userId, boolean showStory) {
        HomeFeedsFragment homeFeedsFragment = new HomeFeedsFragment();
        homeFeedsFragment.postType = name;
        homeFeedsFragment.userId = userId;
        homeFeedsFragment.showStory = showStory;
        return homeFeedsFragment;
    }
}