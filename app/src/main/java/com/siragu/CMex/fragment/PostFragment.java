package com.siragu.CMex.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.ViewCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.kbeanie.multipicker.api.VideoPicker;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.siragu.CMex.activity.MainActivity;
import com.siragu.CMex.model.Post;
import com.siragu.CMex.R;
import com.siragu.CMex.network.ApiUtils;
import com.siragu.CMex.network.DrService;
import com.siragu.CMex.network.request.CreatePostRequest;
import com.siragu.CMex.network.response.CreatePostResponse;
import com.siragu.CMex.network.response.UserResponse;
import com.siragu.CMex.util.Constants;
import com.siragu.CMex.util.FirebaseUploader;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.SharedPreferenceUtil;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays a screen where user can type text to post or can select the camera option
 */
public class PostFragment extends Fragment implements ImagePickerCallback, VideoPickerCallback, View.OnClickListener {
    EditText postTitle;
    EditText postText;
    ProgressBar progressBar;
    ImageView imageView;
    ImageView userImage;
    ImageView pickMedia;
    LinearLayout llAddImage;
    TextView pickMediaMessage;
    View mediaPickerContainer;
    private View postButton;

    private ImagePicker imagePicker;
    private VideoPicker videoPicker;
    private CameraImagePicker cameraPicker;
    private String pickerPath;

    private SharedPreferenceUtil sharedPreferenceUtil;
    private String postType, mediaUrl;
    private File mediaFile;
    private final int REQUEST_CODE_CATEGORY = 9654;
    private final int REQUEST_CODE_GIF = 6542;
    private final int REQUEST_CODE_PERMISSION = 55;
    //private PostCategory selectedCategory;
    private FirebaseUploader firebaseUploader;
    private boolean textBackgroundSelected;
    private Context mContext;

    public static PostFragment newInstance(String type) {
        PostFragment postFragment = new PostFragment();
        postFragment.postType = type;
        return postFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        sharedPreferenceUtil = new SharedPreferenceUtil(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        postTitle = view.findViewById(R.id.frag_post_edt_title);
        postText = view.findViewById(R.id.frag_post_edt_txt);
        progressBar = view.findViewById(R.id.progress);
        imageView = view.findViewById(R.id.pickedMedia);
        userImage = view.findViewById(R.id.frag_post_foxy_logo);
        pickMedia = view.findViewById(R.id.pickMedia);
        llAddImage = view.findViewById(R.id.ll_add_image);
        pickMediaMessage = view.findViewById(R.id.pickMediaMessage);
        mediaPickerContainer = view.findViewById(R.id.mediaPickerContainer);
        mediaPickerContainer.setOnClickListener(this);
        view.findViewById(R.id.btn_close).setOnClickListener(this);
        postButton = view.findViewById(R.id.frag_post_post_btn);
        postButton.setOnClickListener(this);

        ViewCompat.setElevation(view, Helper.dpToPx(getContext(), 4));
        SharedPreferenceUtil sharedPreferenceUtil = new SharedPreferenceUtil(getActivity());

        //progressBar.setIndeterminate(true);

        ((MainActivity) getActivity()).hideBottomBar();

        imageView.setVisibility(postType.equals("text") ? View.GONE : View.VISIBLE);

       /* if(imageView.getVisibility()==View.VISIBLE)
        {
            llAddImage.setVisibility(View.GONE);
        }
        else
        {
            llAddImage.setVisibility(View.VISIBLE);
        }*/


     //   postTitle.requestFocus();


        Helper.openKeyboard(getActivity());

        setupViews();

        UserResponse profileResponse = Helper.getLoggedInUser(sharedPreferenceUtil);
        Glide.with(getContext()).load(profileResponse.getImage())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(getContext(), 8))).placeholder(R.drawable.ic_person_32dp)).into(userImage);
        userImage.setVisibility(View.GONE);
        mContext = getContext();

        return view;
    }

    private void setupViews() {
        switch (postType) {
            case "image":
                mediaPickerContainer.setVisibility(View.VISIBLE);
                postTitle.setVisibility(View.GONE);
                pickMedia.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_camera_alt_grey_24dp));
              //  postText.setVisibility(View.GONE);
                pickMediaMessage.setText(R.string.msg_add_picture);
                break;
            case "text":
                postTitle.setVisibility(View.GONE);
                mediaPickerContainer.setVisibility(View.GONE);
                postText.setVisibility(View.VISIBLE);
                break;
            case "video":
                mediaPickerContainer.setVisibility(View.VISIBLE);
                postTitle.setVisibility(View.GONE);
                pickMedia.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_video_white_24dp));
               // postText.setVisibility(View.GONE);
                pickMediaMessage.setText(R.string.msg_add_video);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeThis();
        mContext = null;
        ((MainActivity) getActivity()).showBottomBar();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Helper.closeKeyboard(getActivity());
    }

    public void pickMedia() {
        if (checkStoragePermissions()) {
            switch (postType) {
                case "image":
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setMessage(R.string.post_media_from);
                    alertDialog.setPositiveButton(getString(R.string.camera), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            cameraPicker = new CameraImagePicker(PostFragment.this);
                            cameraPicker.shouldGenerateMetadata(true);
                            cameraPicker.shouldGenerateThumbnails(true);
                            cameraPicker.setImagePickerCallback(PostFragment.this);
                            pickerPath = cameraPicker.pickImage();
                        }
                    });
                    alertDialog.setNegativeButton(getString(R.string.gallery), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            imagePicker = new ImagePicker(PostFragment.this);
                            imagePicker.shouldGenerateMetadata(true);
                            imagePicker.shouldGenerateThumbnails(true);
                            imagePicker.setImagePickerCallback(PostFragment.this);
                            imagePicker.pickImage();
                        }
                    });
                    alertDialog.create().show();
                    break;
                case "video":
                    videoPicker = new VideoPicker(this);
                    videoPicker.setVideoPickerCallback(this);
                    videoPicker.shouldGenerateMetadata(true);
                    videoPicker.shouldGeneratePreviewImages(true);
                    videoPicker.pickVideo();
                    break;
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION && checkStoragePermissions()) {
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
                case Picker.PICK_VIDEO_DEVICE:
                    if (videoPicker == null) {
                        videoPicker = new VideoPicker(this);
                        videoPicker.setVideoPickerCallback(this);
                    }
                    videoPicker.submit(data);
                    break;
            }
        }
    }

    private boolean checkStoragePermissions() {
        return
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        &&
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        &&
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Closes this fragment removing it from {@link FragmentManager}
     */
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

//    /**
//     * Opens {@link CameraDialog} for selecting icon_picture or video option
//     */
//    @OnClick(R.id.frag_post_camera)
//    public void onCameraButtonClick() {
//        FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
//        closeThis();
//        new CameraDialog().show(supportFragmentManager, "cam_dialog");
//    }

    /**
     * Posts the text to the server
     */
    public void onPostButtonClick() {
       /* if (TextUtils.isEmpty(postTitle.getText().toString())) {
            Toast.makeText(getContext(), R.string.err_field_post_title, Toast.LENGTH_SHORT).show();
            return;
        }*/
        if (this.postType.equals("text") && TextUtils.isEmpty(postText.getText().toString())) {
            Toast.makeText(getContext(), R.string.err_field_post_body, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!this.postType.equals("text") && mediaFile == null) {
            Toast.makeText(getContext(), R.string.err_field_post_media, Toast.LENGTH_SHORT).show();
            return;
        }
        postButton.setClickable(false);
        if (mediaFile != null) {
            firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
                @Override
                public void onUploadFail(String message) {
                    if (mContext != null) {
                        postButton.setClickable(true);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onUploadSuccess(String downloadUrl) {
                    if (mContext != null) {
                        Toast.makeText(getContext(), R.string.uploaded_post_media, Toast.LENGTH_SHORT).show();
                        if(postText.getText().toString().trim().equals(""))
                        {
                            post(new CreatePostRequest("&_karthikraj.than@gmail.com_&", "asd", postType, downloadUrl));

                        }
                        else {
                            post(new CreatePostRequest(postText.getText().toString(), "asd", postType, downloadUrl));
                        }
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
                    if (mContext != null) {
                        postButton.setClickable(true);
                    }
                }
            });
            switch (postType) {
                case "image":
                    firebaseUploader.uploadImage(getContext(), mediaFile);
                    break;
                case "video":
                    firebaseUploader.uploadVideo(getContext(), mediaFile);
                    break;
                default:
                    firebaseUploader.uploadOthers(getContext(), mediaFile);
                    break;
            }
            progressBar.setVisibility(View.VISIBLE);
        } else {
            post(new CreatePostRequest(postText.getText().toString(), "asd", postType));
        }
    }


    private void post(CreatePostRequest createPostRequest) {
        progressBar.setVisibility(View.VISIBLE);
        DrService service = ApiUtils.getClient().create(DrService.class);
        service.createPost(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null), createPostRequest).enqueue(new Callback<CreatePostResponse>() {
            @Override
            public void onResponse(Call<CreatePostResponse> call, Response<CreatePostResponse> response) {
                if (mContext != null) {
                    progressBar.setVisibility(View.INVISIBLE);
                    postButton.setClickable(true);
                    if (response.isSuccessful()) {
                        CreatePostResponse postResponse = response.body();
                        Intent postNewEventIntent = new Intent(Constants.POST_NEW_EVENT);
                        postNewEventIntent.putExtra("post", new Post(postResponse.getId(), postResponse.getTitle(), postResponse.getText(), postResponse.getMedia_url(), postResponse.getType(), -1, null, postResponse.getUser_profile_id()));
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(postNewEventIntent);
                        Toast.makeText(getContext(), R.string.post_added, Toast.LENGTH_SHORT).show();
                        closeThis();
                    } else {
                        Toast.makeText(getContext(), R.string.something_wrong, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<CreatePostResponse> call, Throwable t) {
                if (mContext != null) {
                    postButton.setClickable(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), R.string.something_wrong, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onImagesChosen(List<ChosenImage> images) {
        mediaFile = new File(Uri.parse(images.get(0).getOriginalPath()).getPath());
        Glide.with(getContext())
                .load(mediaFile)
                .apply(new RequestOptions().placeholder(R.drawable.placeholder).dontAnimate())
                .into(imageView);

        llAddImage.setVisibility(View.GONE);
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

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onVideosChosen(List<ChosenVideo> list) {
        mediaFile = new File(Uri.parse(list.get(0).getOriginalPath()).getPath());
        if (mediaFile.length() / 1024 <= 8192) { //if less than 8MB
            new AsyncTask<File, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(File... files) {
                    return ThumbnailUtils.createVideoThumbnail(files[0].getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    Glide.with(getContext())
                            .load(bitmap)
                            .apply(new RequestOptions().placeholder(R.drawable.placeholder).dontAnimate())
                            .into(imageView);
                    llAddImage.setVisibility(View.GONE);
                }
            }.execute(mediaFile);
        } else {
            mediaFile = null;
            Toast.makeText(getContext(), R.string.video_short_msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mediaPickerContainer:
                pickMedia();
                break;
            case R.id.btn_close:
                closeThis();
                break;
            case R.id.frag_post_post_btn:
                onPostButtonClick();
                break;
        }
    }
}
