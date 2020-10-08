package com.siragu.CMex.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.github.piasy.rxandroidaudio.AudioRecorder;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.siragu.CMex.activity.MainActivity;
import com.siragu.CMex.adapter.CommentsRecyclerAdapter;
import com.siragu.CMex.listener.OnCommentAddListener;
import com.siragu.CMex.listener.OnPopupMenuItemClickListener;
import com.siragu.CMex.model.AttachmentTypes;
import com.siragu.CMex.model.Comment;
import com.siragu.CMex.R;
import com.siragu.CMex.network.request.CreatePostRequest;
import com.siragu.CMex.network.response.UserResponse;
import com.siragu.CMex.util.Constants;
import com.siragu.CMex.util.FirebaseUploader;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.SharedPreferenceUtil;
import com.siragu.CMex.util.SpringAnimationHelper;
import com.siragu.CMex.network.ApiUtils;
import com.siragu.CMex.network.DrService;
import com.siragu.CMex.network.request.CreateCommentRequest;
import com.siragu.CMex.network.response.BaseListModel;
import com.varunjohn1990.audio_record_view.AttachmentOption;
import com.varunjohn1990.audio_record_view.AttachmentOptionsListener;
import com.varunjohn1990.audio_record_view.AudioRecordView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A screen to display and add the comments
 */
public class CommentsFragment extends Fragment  implements AudioRecordView.RecordingListener, View.OnClickListener, AttachmentOptionsListener {
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
    String commentTextToPost = "";
    private int pageNumber = 1;
    Context c = null;
    private DrService foxyService;
    private boolean allDone, isLoading;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private static final String LOG_TAG = "AudioRecording";
    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    File mAudioFile ;
    SimpleArcDialog mDialog;
    File recordFile;
    private FirebaseUploader firebaseUploader;
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

    private void closeThis() {
        if (firebaseUploader != null) {
            firebaseUploader.cancelUpload();
        }
        try {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        } catch (Exception ex) {
            Log.e("closeThis", ex.toString());
        }
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

    @SuppressLint("WrongThread")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        c = view.getContext();

        audioRecordView = new AudioRecordView();
        // this is to make your layout the root of audio record view, root layout supposed to be empty..
        audioRecordView.initView((CardView) view.findViewById(R.id.layoutMain));
        // this is to provide the container layout to the audio record view..
        View containerView = audioRecordView.setContainerView(R.layout.fragment_comment);
        audioRecordView.setRecordingListener(this);
        containerView.findViewById(R.id.commentContainer).setVisibility(View.GONE);
        swipeRefresh = containerView.findViewById(R.id.swipeRefresh);
        swipeRefresh.setEnabled(false);
        recyclerView = containerView.findViewById(R.id.comment_recycler_view);
        addACommentEdittext =audioRecordView.getMessageView();
        emptyView = containerView.findViewById(R.id.empty_view_container);

       // swipeRefresh.setEnabled(false);
       /* mAudioRecorder = AudioRecorder.getInstance();
         mAudioFile = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + System.nanoTime() + ".file.m4a");
        mAudioRecorder.prepareRecord(MediaRecorder.AudioSource.MIC,
                MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC,
                mAudioFile);
        */
        profileIcon = containerView.findViewById(R.id.list_item_comment_foxy_img);

        audioRecordView.getMessageView().addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                audioRecordView.getCameraView().setVisibility(View.GONE);
                audioRecordView.getEmojiView().setVisibility(View.GONE);
                audioRecordView.getAttachmentView().setVisibility(View.GONE);
                commentTextToPost =  audioRecordView.getMessageView().getText().toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {  audioRecordView.getCameraView().setVisibility(View.GONE);
                audioRecordView.getEmojiView().setVisibility(View.GONE);
                audioRecordView.getAttachmentView().setVisibility(View.GONE);
                commentTextToPost =  audioRecordView.getMessageView().getText().toString();    }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                audioRecordView.getCameraView().setVisibility(View.GONE);
                audioRecordView.getEmojiView().setVisibility(View.GONE);
                audioRecordView.getAttachmentView().setVisibility(View.GONE);
                commentTextToPost =  audioRecordView.getMessageView().getText().toString();
            }
        });


        firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
            @Override
            public void onUploadFail(String message) {
                if (c != null) {

                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onUploadSuccess(String downloadUrl) {
                if (c != null) {
                    Toast.makeText(getContext(), R.string.comment_added, Toast.LENGTH_SHORT).show();
                    onCommentAddListener.onCommentAdded();
                    updateComments(new CreateCommentRequest("asd",downloadUrl,"2"));
                    addACommentEdittext.setText("");
                    Helper.closeKeyboard((Activity) getContext());

                }
            }

            @Override
            public void onUploadProgress(int progress) {
//                    if (progressBar.isIndeterminate())
//                        progressBar.setIndeterminate(false);
//                    progressBar.setProgress(progress);
            }

            @Override
            public void onUploadCancelled() {
                if (c != null) {
                 //   postButton.setClickable(true);
                }
            }
        });
        containerView.findViewById(R.id.btn_post_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postCommentOnServer(v);
            }
        });

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomBar();
        }



           recyclerView.setNestedScrollingEnabled(false);


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


        setListener();
        audioRecordView.getMessageView().requestFocus();
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

         mDialog = new SimpleArcDialog(c);
        mDialog.setConfiguration(new ArcConfiguration(c));
        mDialog.setCancelable(false);
        mDialog.setTitle("Uploading..");

    }

    /**
     * Handles the click of send button to post the comment.
     * It also validates the comment of not being empty
     *
     * @param view
     */
    public void postCommentOnServer(View view) {
        SpringAnimationHelper.performAnimation(view);

        // commentTextToPost = audioRecordView.getMessageView().getText().toString();
        if (TextUtils.isEmpty(commentTextToPost)) {

            audioRecordView.getCameraView().setVisibility(View.GONE);
            audioRecordView.getEmojiView().setVisibility(View.GONE);
            audioRecordView.getAttachmentView().setVisibility(View.GONE);
            Toast.makeText(getContext(), R.string.err_field_comment, Toast.LENGTH_SHORT).show();
        } else {
            audioRecordView.getCameraView().setVisibility(View.GONE);
            audioRecordView.getEmojiView().setVisibility(View.GONE);
            audioRecordView.getAttachmentView().setVisibility(View.GONE);
            Toast.makeText(getContext(), R.string.comment_added, Toast.LENGTH_SHORT).show();
            onCommentAddListener.onCommentAdded();
            updateComments(new CreateCommentRequest(audioRecordView.getMessageView().getText().toString(),"","1"));
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
                    audioRecordView.getMessageView().setText("");
                }
                mDialog.cancel();
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                mDialog.cancel();
            }
        });
    }

    private void setListener() {
        audioRecordView.getCameraView().setVisibility(View.GONE);
        audioRecordView.getEmojiView().setVisibility(View.GONE);
        audioRecordView.getAttachmentView().setVisibility(View.GONE);
        audioRecordView.getMessageView().setPadding(25,25,25,25);





        audioRecordView.getSendView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = audioRecordView.getMessageView().getText().toString().trim();

                //  messageAdapter.add(new Message(msg));
                audioRecordView.getCameraView().setVisibility(View.GONE);
                audioRecordView.getEmojiView().setVisibility(View.GONE);
                audioRecordView.getAttachmentView().setVisibility(View.GONE);
                postCommentOnServer(v);
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
    @Override
    public void onClick(View v) {
    }
    @Override
    public void onClick(AttachmentOption attachmentOption) {
    }
    @Override
    public void onRecordingStarted() {
       // showToast("started");
     //   mAudioRecorder.startRecord();
     //   audioRecordView.getCameraView().setVisibility(View.GONE);
     //   audioRecordView.getEmojiView().setVisibility(View.GONE);
     //   audioRecordView.getAttachmentView().setVisibility(View.GONE);
    //    time = System.currentTimeMillis() / (1000);
     //   recordingStart();

    }

    @Override
    public void onRecordingLocked() {
        //showToast("locked");
        audioRecordView.getCameraView().setVisibility(View.GONE);
        audioRecordView.getEmojiView().setVisibility(View.GONE);
        audioRecordView.getAttachmentView().setVisibility(View.GONE);
    }

    @Override
    public void onRecordingCompleted() {
       // showToast("completed");

     //   mAudioRecorder.stopRecord();
        int recordTime = (int) ((System.currentTimeMillis() / (1000)) - time);
        audioRecordView.getCameraView().setVisibility(View.GONE);
        audioRecordView.getEmojiView().setVisibility(View.GONE);
        audioRecordView.getAttachmentView().setVisibility(View.GONE);

       recordingStop(true);
    //  mRecorder.stop();

    }

    private void recordingStop(boolean send) {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (RuntimeException stopException) {
            mRecorder = null;
            send = false;
        }
       // recordTimerStop();

        if (send) {
          // newFileUploadTask(recordFilePath, AttachmentTypes.RECORDING, null);
            Log.d("File path",recordFile.getAbsolutePath());
            mDialog.show();
            firebaseUploader.uploadAudio(getContext(), recordFile);
        } else {
            new File(recordFilePath).delete();
        }
    }


    private String recordFilePath;
    private void recordingStart() {

             recordFile = new File(Environment.getExternalStorageDirectory(), "/" + getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(AttachmentTypes.RECORDING) + "/.sent/");
            boolean dirExists = recordFile.exists();
            if (!dirExists)
                dirExists = recordFile.mkdirs();
            if (dirExists) {
                try {
                    recordFile = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(AttachmentTypes.RECORDING) + "/.sent/", System.currentTimeMillis() + ".aac");
                    if (!recordFile.exists())
                        recordFile.createNewFile();
                    recordFilePath = recordFile.getAbsolutePath();
                    mRecorder = new MediaRecorder();


                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mRecorder.setOutputFile(recordFilePath);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

                    mRecorder.prepare();
                    mRecorder.start();
                  //  recordTimerStart (System.currentTimeMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                    mRecorder = null;
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                    mRecorder = null;
                }
            }

    }



    @Override
    public void onRecordingCanceled() {
        showToast("canceled");
        //debug("canceled");
        audioRecordView.getCameraView().setVisibility(View.GONE);
        audioRecordView.getEmojiView().setVisibility(View.GONE);
        audioRecordView.getAttachmentView().setVisibility(View.GONE);
        recordingStop(false);
      //  mRecorder.stop();
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(c, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
