package com.siragu.CMex.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.siragu.CMex.R;
import com.siragu.CMex.model.Post;
import com.siragu.CMex.network.ApiUtils;
import com.siragu.CMex.network.DrService;
import com.siragu.CMex.network.response.UserResponse;
import com.siragu.CMex.util.Constants;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.SharedPreferenceUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;

import jp.shts.android.storiesprogressview.StoriesProgressView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class StatusActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {
    private static final String DATA_STORY_USERS = "StoryUsers";

    private StoriesProgressView storiesProgressView;
    private ImageView storyImage, storyUserImage;
    private TextView storyUserName, storyUserTime;
    private ProgressBar storyProgress;
    private ArrayList<UserResponse> storyUsers;
    private ArrayList<Post> stories;

    private SharedPreferenceUtil sharedPreferenceUtil;
    private DrService weService;

    private int counterImage = 0, counterStory = 0, currentStory = -1;

    long pressTime = 0L;
    long limit = 500L;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_status);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        weService = ApiUtils.getClient().create(DrService.class);
        storyUsers = getIntent().getParcelableArrayListExtra(DATA_STORY_USERS);
        initUi();
        startStory(counterStory);
    }

    private void startStory(int pos) {
        currentStory = pos;
        resetComplete();
        Glide.with(this)
                .load(storyUsers.get(pos).getImage())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(this, 8))).override(Helper.dp2px(this, 38), Helper.dp2px(this, 38)).placeholder(R.drawable.ic_person_gray_24dp))
                .into(storyUserImage);
        storyUserName.setText(storyUsers.get(pos).getName());
        storyProgress.setVisibility(View.VISIBLE);
        storyUserTime.setVisibility(View.GONE);

        weService.getStory(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), storyUsers.get(pos).getId().toString()).enqueue(new Callback<ArrayList<Post>>() {
            @Override
            public void onResponse(Call<ArrayList<Post>> call, Response<ArrayList<Post>> response) {
                if (response.isSuccessful()) {
                    storyProgress.setVisibility(View.GONE);
                    if (response.body() != null && !response.body().isEmpty())
                        setupStories(response.body());
                    else {
                        Toast.makeText(StatusActivity.this, R.string.empty_stories, Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Post>> call, Throwable t) {
                storyProgress.setVisibility(View.GONE);
            }
        });
    }

    private void resetComplete() {
        Field field = null;
        try {
            field = storiesProgressView.getClass().getDeclaredField("isComplete");
            // Allow modification on the field
            field.setAccessible(true);
            // Sets the field to the new value for this instance
            field.set(storiesProgressView, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupStories(ArrayList<Post> posts) {
        counterImage = 0;
        this.stories = posts;
        storiesProgressView.setStoriesCount(stories.size());
        storiesProgressView.setStoryDuration(6000L);
        storiesProgressView.setStoriesListener(this);
        Glide.with(this).load(stories.get(counterImage).getMedia_url()).apply(new RequestOptions().placeholder(R.drawable.placeholder)).into(storyImage);
        storyUserTime.setText(Helper.timeDiff(stories.get(counterImage).getCreatedAt()).toString());
        storyUserTime.setVisibility(View.VISIBLE);
        storiesProgressView.startStories();
    }

    private void initUi() {
        storyImage = findViewById(R.id.image);
        storyUserImage = findViewById(R.id.storyUserImage);
        storyUserName = findViewById(R.id.storyUserName);
        storyUserTime = findViewById(R.id.storyUserTime);
        storiesProgressView = findViewById(R.id.stories);
        storyProgress = findViewById(R.id.storyProgress);

        findViewById(R.id.storyUserContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStory != -1 && storyUsers.size() > currentStory && storyUsers.get(currentStory) != null) {
                    UserResponse su = storyUsers.get(currentStory);
                    startActivity(UserProfileDetailActivity.newInstance(StatusActivity.this, su.getId().toString(), su.getName(), su.getImage()));
                }
            }
        });

        // bind reverse view
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onNext() {
        counterImage++;
        if (counterImage < stories.size()) {
            Glide.with(this).load(stories.get(counterImage).getMedia_url()).apply(new RequestOptions().placeholder(R.drawable.placeholder)).into(storyImage);
            storyUserTime.setText(Helper.timeDiff(stories.get(counterImage).getCreatedAt()).toString());
            storyUserTime.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPrev() {
        if (counterImage > 0) {
            counterImage--;
            storiesProgressView.pause();
            Glide.with(this).load(stories.get(counterImage).getMedia_url()).apply(new RequestOptions().placeholder(R.drawable.placeholder)).into(storyImage);
            storyUserTime.setText(Helper.timeDiff(stories.get(counterImage).getCreatedAt()).toString());
            storyUserTime.setVisibility(View.VISIBLE);
        } else if (counterStory != 0) {
            counterStory--;
            storiesProgressView.destroy();
            startStory(counterStory);
        }
    }

    @Override
    public void onComplete() {
        //finish();
        counterStory++;
        if (counterStory < storyUsers.size()) {
            startStory(counterStory);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }

    public static Intent newIntent(Context context, ArrayList<UserResponse> storyUsers, int pos) {
        Intent intent = new Intent(context, StatusActivity.class);
        ArrayList<UserResponse> toPass = new ArrayList<>();
        for (int i = pos; i < storyUsers.size(); i++) {
            toPass.add(storyUsers.get(i));
        }
        intent.putParcelableArrayListExtra(DATA_STORY_USERS, toPass);
        return intent;
    }
}