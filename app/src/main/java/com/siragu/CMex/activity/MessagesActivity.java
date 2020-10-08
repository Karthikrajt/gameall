package com.siragu.CMex.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.kbeanie.multipicker.api.AudioPicker;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.FilePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.VideoPicker;
import com.kbeanie.multipicker.api.callbacks.AudioPickerCallback;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenAudio;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.onesignal.OneSignal;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import com.siragu.CMex.R;
import com.siragu.CMex.adapter.MessageAdapter;
import com.siragu.CMex.listener.OnMessageItemClick;
import com.siragu.CMex.listener.RecordingViewInteractor;
import com.siragu.CMex.model.Attachment;
import com.siragu.CMex.model.AttachmentTypes;
import com.siragu.CMex.model.Chat;
import com.siragu.CMex.model.ChatUser;
import com.siragu.CMex.model.DownloadFileEvent;
import com.siragu.CMex.model.Message;
import com.siragu.CMex.network.response.UserResponse;
import com.siragu.CMex.util.Constants;
import com.siragu.CMex.util.DownloadUtil;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.SharedPreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import id.zelory.compressor.Compressor;

public class MessagesActivity extends AppCompatActivity implements View.OnClickListener, OnMessageItemClick, RecordingViewInteractor, ImagePickerCallback, AudioPickerCallback, VideoPickerCallback {
    private static final int REQUEST_PERMISSION_RECORD = 159;
    private static String EXTRA_DATA_CHAT = "extradatachat";
    private MessageAdapter messageAdapter;
    private ArrayList<Message> dataList = new ArrayList<>();
    private String chatChild;
    private int countSelected = 0;
    private Handler recordWaitHandler, recordTimerHandler;
    private Runnable recordRunnable, recordTimerRunnable;
    private MediaRecorder mRecorder = null;
    private String recordFilePath;
    private float displayWidth;
    private ArrayList<Integer> adapterPositions = new ArrayList<>();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String currentlyPlaying = "";
    private Toolbar toolbar;
    private TableLayout addAttachmentLayout;
    private RecyclerView recyclerView;
    private EmojiEditText newMessage;
    private ImageView usersImage, addAttachment, sendMessage, attachment_emoji;
    private LinearLayout rootView, sendContainer;
    private EmojiPopup emojIcon;
    private String pickerPath;
    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;
    private FilePicker filePicker;
    private AudioPicker audioPicker;
    private VideoPicker videoPicker;
    private Chat chat;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private UserResponse userMe;
    private DatabaseReference inboxRef, chatRef, userRef;
    private ChatUser chatUser;
    private Context mContext;

    private String[] permissionsRecord = {Manifest.permission.VIBRATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private String[] permissionsStorage = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String[] permissionsCamera = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //Download complete listener
    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null)
                switch (intent.getAction()) {
                    case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                        if (adapterPositions.size() > 0 && messageAdapter != null)
                            for (int pos : adapterPositions)
                                if (pos != -1)
                                    messageAdapter.notifyItemChanged(pos);
                        adapterPositions.clear();
                        break;
                }
        }
    };

    //Download event listener
    private BroadcastReceiver downloadEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadFileEvent downloadFileEvent = intent.getParcelableExtra("data");
            if (downloadFileEvent != null) {
                downloadFile(downloadFileEvent);
            }
        }
    };

    private ChildEventListener childValueEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Message message = dataSnapshot.getValue(Message.class);
            if (message != null && mContext != null) {
                message.setTimeDiff(Helper.timeDiff(Long.valueOf(message.getDateTimeStamp())).toString());
                dataList.add(message);
                messageAdapter.notifyItemInserted(dataList.size() - 1);
                recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);

                boolean isMeSender = message.getSenderId().equals(chat.getMyId());
                chat.setChatImage(isMeSender ? message.getRecipientImage() : message.getSenderImage());
                chat.setChatName(isMeSender ? message.getRecipientName() : message.getSenderName());
                chat.setChatStatus(isMeSender ? message.getRecipientStatus() : message.getSenderStatus());

                if (!message.isDelivered()) {
                    chatRef.child(chatChild).child(message.getId()).child("delivered").setValue(true);
                }

                getSupportActionBar().setTitle(chat.getChatName());
                Glide.with(mContext).load(chat.getChatImage()).apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(mContext, 8))).placeholder(R.drawable.ic_person_gray_24dp)).into(usersImage);
                dismissNotifications();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Message message = dataSnapshot.getValue(Message.class);
            if (message != null && message.getId() != null && mContext != null) {
                message.setTimeDiff(Helper.timeDiff(Long.valueOf(message.getDateTimeStamp())).toString());
                int pos = -1;
                for (int i = dataList.size() - 1; i >= 0; i--) {
                    if (dataList.get(i).getId().equals(message.getId())) {
                        pos = i;
                        break;
                    }
                }
                if (pos != -1) {
                    dataList.set(pos, message);
                    messageAdapter.notifyItemChanged(pos);
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void dismissNotifications() {
        try {
            if (mContext != null && chatChild != null) {
                OneSignal.cancelGroupedNotifications(chatChild);
            }
        } catch (Exception ex) {
        }
    }

    private void notifyMessage(Message message) {
        ArrayList<String> userPlayerIds = new ArrayList<>();
        userPlayerIds.add(chatUser.getUserPlayerId());
        try {
            OneSignal.postNotification(new JSONObject("{'headings': {'en':'" + message.getSenderName() + "'}, 'contents': {'en':'" + message.getBody() + "'}, 'include_player_ids': " + userPlayerIds.toString() + ",'data': " + new Gson().toJson(message) + ",'android_group':" + message.getChatId() + " }"),
                    new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Log.i("OneSignalExample", "postNotification Success: " + response.toString());
                        }

                        @Override
                        public void onFailure(JSONObject response) {
                            Log.e("OneSignalExample", "postNotification Failure: " + response.toString());
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        EmojiManager.install(new GoogleEmojiProvider());
        setContentView(R.layout.activity_messages);
        initUi();

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_DATA_CHAT)) {
            sharedPreferenceUtil = new SharedPreferenceUtil(mContext);
            chat = intent.getParcelableExtra(EXTRA_DATA_CHAT);
            userMe = Helper.getLoggedInUser(sharedPreferenceUtil);
            chatChild = Helper.getChatChild(chat.getMyId(), chat.getChatId());
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            inboxRef = firebaseDatabase.getReference(Constants.REF_INBOX);
            chatRef = firebaseDatabase.getReference(Constants.REF_CHAT);
            userRef = firebaseDatabase.getReference(Constants.REF_USER);
            userRef.child(chat.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ChatUser cu = dataSnapshot.getValue(ChatUser.class);
                    if (cu != null && cu.getUserPlayerId() != null && mContext != null)
                        chatUser = cu;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            getSupportActionBar().setTitle(chat.getChatName());
            Glide.with(mContext).load(chat.getChatImage()).apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(mContext, 8))).placeholder(R.drawable.ic_person_gray_24dp)).into(usersImage);
            //setup recycler view
            messageAdapter = new MessageAdapter(mContext, dataList, userMe.getId().toString());
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            recyclerView.setAdapter(messageAdapter);
            recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (bottom < oldBottom) {
                    recyclerView.postDelayed(() -> recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1), 100);
                }
            });

            emojIcon = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiPopupShownListener(() -> {
                if (addAttachmentLayout.getVisibility() == View.VISIBLE) {
                    addAttachmentLayout.setVisibility(View.GONE);
                    addAttachment.animate().setDuration(400).rotationBy(-45).start();
                }
            }).build(newMessage);
//        emojIcon.setUseSystemEmoji(true);
//        newMessage.setUseSystemDefault(true);
//        newMessage.setEmojiconSize(getResources().getDimensionPixelSize(R.dimen.emoji_icon_size));

            displayWidth = Helper.getDisplayWidth(this);

            mediaPlayer.setOnCompletionListener(mediaPlayer -> notifyRecordingPlaybackCompletion());

            chatRef.child(chatChild).limitToLast(20).addChildEventListener(childValueEventListener);

            registerUserUpdates();
        } else {
            finish();//temporary fix
        }
        Log.d("lifecycle", "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        userRef.child(chat.getMyId()).child("online").setValue(true);
    }

    private void registerUserUpdates() {
        //Publish logged in user's typing status
        newMessage.addTextChangedListener(new TextWatcher() {
            CountDownTimer timer = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //sendMessage.setImageDrawable(ContextCompat.getDrawable(mContext, s.length() == 0 ? R.drawable.ic_keyboard_voice_24dp : R.drawable.ic_send_message));

//                if (user != null) {
//                    if (timer != null) {
//                        timer.cancel();
//                        usersRef.child(userMe.getId()).child("typing").setValue(true);
//                    }
//                    timer = new CountDownTimer(1500, 1000) {
//                        public void onTick(long millisUntilFinished) {
//                        }
//
//                        public void onFinish() {
//                            usersRef.child(userMe.getId()).child("typing").setValue(false);
//                        }
//                    }.start();
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUi() {
        toolbar = findViewById(R.id.chatToolbar);
        toolbar.setTitleTextAppearance(mContext, R.style.MontserratBoldTextAppearance);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left);
        }

        addAttachmentLayout = findViewById(R.id.add_attachment_layout);
        usersImage = findViewById(R.id.userImage);
        recyclerView = findViewById(R.id.recycler_view);
        newMessage = findViewById(R.id.new_message);
        addAttachment = findViewById(R.id.add_attachment);
        sendMessage = findViewById(R.id.send);
        sendContainer = findViewById(R.id.sendContainer);
        rootView = findViewById(R.id.rootView);
        attachment_emoji = findViewById(R.id.attachment_emoji);

        ImageView chatBackground = findViewById(R.id.chatBackground);
        Glide.with(this).load(R.drawable.chat_bg).into(chatBackground);

        addAttachment.setOnClickListener(this);
        attachment_emoji.setOnClickListener(this);
        sendMessage.setOnClickListener(this);
        usersImage.setOnClickListener(this);
        findViewById(R.id.attachment_camera).setOnClickListener(this);
        findViewById(R.id.attachment_gallery).setOnClickListener(this);
        findViewById(R.id.attachment_video).setOnClickListener(this);
        newMessage.setOnTouchListener((v, event) -> {
            if (addAttachmentLayout.getVisibility() == View.VISIBLE) {
                addAttachmentLayout.setVisibility(View.GONE);
                addAttachment.animate().setDuration(400).rotationBy(-45).start();
            }
            return false;
        });
        sendMessage.setOnTouchListener(voiceMessageListener);
    }

    private View.OnTouchListener voiceMessageListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("TAG", "touched down");
                    if (newMessage.getText().toString().trim().isEmpty()) {
                        if (recordWaitHandler == null)
                            recordWaitHandler = new Handler();
                        recordRunnable = () -> recordingStart();
                        recordWaitHandler.postDelayed(recordRunnable, 600);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("TAG", "moving: (" + displayWidth + ", " + x + ")");
                    if (mRecorder != null && newMessage.getText().toString().trim().isEmpty()) {
                        if (Math.abs(event.getX()) / displayWidth > 0.35f) {
                            recordingStop(false);
                            Toast.makeText(mContext, R.string.recording_cancelled, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("TAG", "touched up");
                    if (recordWaitHandler != null && newMessage.getText().toString().trim().isEmpty())
                        recordWaitHandler.removeCallbacks(recordRunnable);
                    if (mRecorder != null && newMessage.getText().toString().trim().isEmpty()) {
                        recordingStop(true);
                    }
                    break;
            }
            return false;
        }
    };

    private void recordingStop(boolean send) {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (IllegalStateException ex) {
            mRecorder = null;
        }
        recordTimerStop();
        if (send) {
            newFileUploadTask(recordFilePath, AttachmentTypes.RECORDING, null);
        } else {
            new File(recordFilePath).delete();
        }
    }

    private void recordingStart() {
        if (permissionsAvailable(permissionsRecord)) {
            File recordFile = new File(Environment.getExternalStorageDirectory(), "/" + getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(AttachmentTypes.RECORDING) + "/.sent/");
            boolean dirExists = recordFile.exists();
            if (!dirExists)
                dirExists = recordFile.mkdirs();
            if (dirExists) {
                try {
                    recordFile = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(AttachmentTypes.RECORDING) + "/.sent/", System.currentTimeMillis() + ".mp3");
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
                    recordTimerStart (System.currentTimeMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                    mRecorder = null;
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                    mRecorder = null;
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsRecord, REQUEST_PERMISSION_RECORD);
        }
    }

    private void recordTimerStart(final long currentTimeMillis) {
        Toast.makeText(this, R.string.recording_progress, Toast.LENGTH_SHORT).show();
        recordTimerRunnable = new Runnable() {
            public void run() {
                Long elapsedTime = System.currentTimeMillis() - currentTimeMillis;
                newMessage.setHint(Helper.timeFormater(elapsedTime) + " " + getString(R.string.swipe_retry_left));
                recordTimerHandler.postDelayed(this, 1000);
            }
        };
        if (recordTimerHandler == null)
            recordTimerHandler = new Handler();
        recordTimerHandler.post(recordTimerRunnable);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) v.vibrate(100);
    }

    private void recordTimerStop() {
        recordTimerHandler.removeCallbacks(recordTimerRunnable);
        newMessage.setHint(R.string.type_message);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) v.vibrate(100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadEventReceiver, new IntentFilter(Constants.BROADCAST_DOWNLOAD_EVENT));
        dismissNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (chat != null && dataList != null && !dataList.isEmpty()) {
            Helper.setLastRead(sharedPreferenceUtil, chat.getChatId(), dataList.get(dataList.size() - 1).getId());
        }
        unregisterReceiver(downloadCompleteReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadEventReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
        userRef.child(chat.getMyId()).child("online").setValue(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
        if (Constants.CHAT_CAB)
            undoSelectionPrepared();
        chatRef.removeEventListener(childValueEventListener);
        Log.d("lifecycle", "onDestroy");
    }

    @Override
    public void onBackPressed() {
        if (Constants.CHAT_CAB)
            undoSelectionPrepared();
        else {
            Helper.closeKeyboard(this);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_attachment:
                Helper.closeKeyboard(this, view);
                if (addAttachmentLayout.getVisibility() == View.VISIBLE) {
                    addAttachmentLayout.setVisibility(View.GONE);
                    addAttachment.animate().setDuration(400).rotationBy(-45).start();
                } else {
                    addAttachmentLayout.setVisibility(View.VISIBLE);
                    addAttachment.animate().setDuration(400).rotationBy(45).start();
                    emojIcon.dismiss();
                }
                break;
            case R.id.send:
                if (!TextUtils.isEmpty(newMessage.getText().toString().trim())) {
                    sendMessage(AttachmentTypes.NONE_TEXT, null);
                }
                break;
            case R.id.userImage:
                startActivity(UserProfileDetailActivity.newInstance(this, chat.getChatId(), chat.getChatName(), chat.getChatImage()));
                break;
//            case R.id.attachment_contact:
//                openContactPicker();
//                break;
//            case R.id.attachment_camera:
//                openImageClick();
//                break;
            case R.id.attachment_emoji:
                emojIcon.toggle();
                break;
            case R.id.attachment_gallery:
                openImagePick();
                break;
            case R.id.attachment_camera:
                openImageClick();
                break;
//            case R.id.attachment_audio:
//                openAudioPicker();
//                break;
            case R.id.attachment_video:
                openVideoPicker();
                break;
//            case R.id.attachment_location:
//                openPlacePicker();
//                break;
//            case R.id.attachment_document:
//                openDocumentPicker();
//                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_copy:
                ArrayList<Message> msgsSelected = new ArrayList<>();
                for (Message message : dataList) {//Get all selected messages in a String
                    if (message.isSelected() && !TextUtils.isEmpty(message.getBody())) {
                        msgsSelected.add(message);
                    }
                }
                if (msgsSelected.size() > 1) {
                    StringBuilder stringBuilder = new StringBuilder("");
                    for (Message message : msgsSelected) {//Get all selected messages in a String
                        boolean isMeSender = message.getSenderId().equals(chat.getMyId());
                        stringBuilder.append(isMeSender ? message.getSenderName() : message.getRecipientName());
                        stringBuilder.append("(");
                        stringBuilder.append(Helper.getTime(Long.valueOf(message.getDateTimeStamp())));
                        stringBuilder.append(")");
                        stringBuilder.append(" : ");
                        stringBuilder.append(message.getBody());
                        stringBuilder.append("\n");
                    }
                    //Add String in clipboard
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("simple text", stringBuilder.toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, R.string.copied_msgs, Toast.LENGTH_SHORT).show();
                } else if (!msgsSelected.isEmpty()) {
                    //Add String in clipboard
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("simple text", msgsSelected.get(0).getBody());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, R.string.copied_msg, Toast.LENGTH_SHORT).show();
                }
                undoSelectionPrepared();
                break;
        }
        return true;
    }

    private void sendMessage(@AttachmentTypes.AttachmentType int attachmentType, Attachment attachment) {
        //Create message object
        Message message = new Message();
        message.setAttachmentType(attachmentType);
        message.setAttachment(attachment);
        message.setChatId(chatChild);
        message.setBody(newMessage.getText().toString());
        message.setDateTimeStamp(String.valueOf(System.currentTimeMillis()));
        message.setSent(true);
        message.setDelivered(false);
        message.setRecipientId(chat.getChatId());
        message.setRecipientImage(chat.getChatImage());
        message.setRecipientName(chat.getChatName());
        message.setRecipientStatus(chat.getChatStatus());
        message.setSenderId(chat.getMyId());
        message.setSenderName(userMe.getName());
        message.setSenderImage(userMe.getImage());
        message.setSenderStatus("status");
        message.setId(chatRef.child(chatChild).push().getKey());

        //Add messages in chat child
        chatRef.child(chatChild).child(message.getId()).setValue(message);
        inboxRef.child(message.getRecipientId()).child(message.getSenderId()).setValue(message);
        inboxRef.child(message.getSenderId()).child(message.getRecipientId()).setValue(message);
        newMessage.setText("");

        if (chatUser != null && chatUser.getUserPlayerId() != null) {
            notifyMessage(message);
        }
    }

    private void openVideoPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            videoPicker = new VideoPicker(this);
            videoPicker.shouldGenerateMetadata(true);
            videoPicker.shouldGeneratePreviewImages(true);
            videoPicker.setVideoPickerCallback(this);
            videoPicker.pickVideo();
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 41);
        }
    }


    void openAudioPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            audioPicker = new AudioPicker(this);
            audioPicker.setAudioPickerCallback(this);
            audioPicker.pickAudio();
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 25);
        }
    }

    public void openImagePick() {
        if (permissionsAvailable(permissionsStorage)) {
            imagePicker = new ImagePicker(this);
            imagePicker.shouldGenerateMetadata(true);
            imagePicker.shouldGenerateThumbnails(true);
            imagePicker.setImagePickerCallback(this);
            imagePicker.pickImage();
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 36);
        }
    }

    void openImageClick() {
        if (permissionsAvailable(permissionsCamera)) {
            cameraPicker = new CameraImagePicker(this);
            cameraPicker.shouldGenerateMetadata(true);
            cameraPicker.shouldGenerateThumbnails(true);
            cameraPicker.setImagePickerCallback(this);
            pickerPath = cameraPicker.pickImage();
        } else {
            ActivityCompat.requestPermissions(this, permissionsCamera, 47);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 25:
                if (permissionsAvailable(permissions))
                    openAudioPicker();
                break;
            case 36:
                if (permissionsAvailable(permissions))
                    openImagePick();
                break;
            case 47:
                if (permissionsAvailable(permissions))
                    openImageClick();
                break;
            case 41:
                if (permissionsAvailable(permissions))
                    openVideoPicker();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case Picker.PICK_IMAGE_DEVICE:
                    if (imagePicker == null) {
                        imagePicker = new ImagePicker(this);
                        imagePicker.setImagePickerCallback(this);
                    }
                    imagePicker.submit(data);
                    break;
                case Picker.PICK_IMAGE_CAMERA:
                    if (cameraPicker == null) {
                        cameraPicker = new CameraImagePicker(this);
                        cameraPicker.setImagePickerCallback(this);
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
                case Picker.PICK_FILE:
                    filePicker.submit(data);
                    break;
                case Picker.PICK_AUDIO:
                    audioPicker.submit(data);
                    break;
            }
        }
    }

    private void uploadImage(String filePath) {
        newFileUploadTask(filePath, AttachmentTypes.IMAGE, null);
    }

    private void uploadThumbnail(final String filePath) {
        Toast.makeText(this, R.string.just_moment, Toast.LENGTH_LONG).show();
        File file = new File(filePath);
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(getString(R.string.app_name)).child("video").child("thumbnail").child(file.getName() + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //If thumbnail exists
                Attachment attachment = new Attachment();
                attachment.setData(uri.toString());
                newFileUploadTask(filePath, AttachmentTypes.VIDEO, attachment);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                @SuppressLint("StaticFieldLeak") AsyncTask<String, Void, byte[]> thumbnailTask = new AsyncTask<String, Void, byte[]>() {
                    @Override
                    protected byte[] doInBackground(String... params) {
                        //Create thumbnail
                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(params[0], MediaStore.Video.Thumbnails.MINI_KIND);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        return baos.toByteArray();
                    }

                    @Override
                    protected void onPostExecute(byte[] data) {
                        super.onPostExecute(data);
                        if (data != null && data.length > 1) {
                            //Upload thumbnail and then upload video
                            UploadTask uploadTask = storageReference.putBytes(data);
                            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    // Continue with the task to get the download URL
                                    return storageReference.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        Attachment attachment = new Attachment();
                                        attachment.setData(downloadUri.toString());
                                        newFileUploadTask(filePath, AttachmentTypes.VIDEO, attachment);
                                    } else {
                                        newFileUploadTask(filePath, AttachmentTypes.VIDEO, null);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    newFileUploadTask(filePath, AttachmentTypes.VIDEO, null);
                                }
                            });
                        } else {
                            newFileUploadTask(filePath, AttachmentTypes.VIDEO, null);
                        }
                    }
                };
                thumbnailTask.execute(filePath);
            }
        });
    }

    private void newFileUploadTask(String filePath, @AttachmentTypes.AttachmentType final int attachmentType, final Attachment attachment) {
        if (addAttachmentLayout.getVisibility() == View.VISIBLE) {
            addAttachmentLayout.setVisibility(View.GONE);
            addAttachment.animate().setDuration(400).rotationBy(-45).start();
        }

        final File fileToUpload = new File(filePath);
        final String fileName = Uri.fromFile(fileToUpload).getLastPathSegment();

        Attachment preSendAttachment = attachment;//Create/Update attachment
        if (preSendAttachment == null) preSendAttachment = new Attachment();
        preSendAttachment.setName(fileName);
        preSendAttachment.setBytesCount(fileToUpload.length());
        preSendAttachment.setUrl("loading");

        Message message = new Message();
        message.setAttachmentType(attachmentType);
        message.setAttachment(attachment);
        message.setChatId(chatChild);
        message.setBody("");
        message.setDateTimeStamp(String.valueOf(System.currentTimeMillis()));
        message.setSent(false);
        message.setDelivered(false);
        message.setRecipientId(chat.getChatId());
        message.setRecipientImage(chat.getChatImage());
        message.setRecipientName(chat.getChatName());
        message.setRecipientStatus(chat.getChatStatus());
        message.setSenderId(chat.getMyId());
        message.setSenderName(userMe.getName());
        message.setSenderImage(userMe.getImage());
        message.setSenderStatus("status");
        message.setId(chatRef.child(chatChild).push().getKey());

        //presend this.. then upload.. then actual send this and modify existing message with this id from list.
    }

    public void downloadFile(DownloadFileEvent downloadFileEvent) {
        if (permissionsAvailable(permissionsStorage)) {
            new DownloadUtil().checkAndLoad(mContext, downloadFileEvent);
            adapterPositions.add(downloadFileEvent.getPosition());
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 47);
        }
    }

    private void prepareToSelect() {
        usersImage.setVisibility(View.GONE);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_chat_cab);
        Constants.CHAT_CAB = true;
    }

    private void undoSelectionPrepared() {
        for (Message msg : dataList) {
            msg.setSelected(false);
        }
        messageAdapter.notifyDataSetChanged();
        toolbar.getMenu().clear();
        getSupportActionBar().setTitle(chat.getChatName());
        usersImage.setVisibility(View.VISIBLE);
        countSelected = 0;
        Constants.CHAT_CAB = false;
    }

    @Override
    public boolean isRecordingPlaying(String fileName) {
        return isMediaPlayerPlaying() && currentlyPlaying.equals(fileName);
    }

    private boolean isMediaPlayerPlaying() {
        try {
            return mediaPlayer.isPlaying();
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    @Override
    public void playRecording(File file, String fileName, int position) {
        if (permissionsAvailable(permissionsRecord)) {
            if (isMediaPlayerPlaying()) {
                mediaPlayer.stop();
                notifyRecordingPlaybackCompletion();
                if (!fileName.equals(currentlyPlaying)) {
                    if (startPlayback(file)) {
                        currentlyPlaying = fileName;
                        messageAdapter.notifyItemChanged(position);
                    }
                }
            } else {
                if (startPlayback(file)) {
                    currentlyPlaying = fileName;
                    messageAdapter.notifyItemChanged(position);
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsRecord, REQUEST_PERMISSION_RECORD);
        }
    }

    private boolean startPlayback(File file) {
        boolean started = true;
        resetMediaPlayer();
        try {
            FileInputStream is = new FileInputStream(file);
            FileDescriptor fd = is.getFD();
            mediaPlayer.setDataSource(fd);
            is.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            started = false;
        }
        return started;
    }

    private void resetMediaPlayer() {
        try {
            mediaPlayer.reset();
        } catch (IllegalStateException ex) {
            mediaPlayer = new MediaPlayer();
        }
    }

    private void notifyRecordingPlaybackCompletion() {
        if (recyclerView != null && messageAdapter != null) {
            int total = dataList.size();
            for (int i = total - 1; i >= 0; i--) {
                if (dataList.get(i).getAttachment() != null
                        &&
                        dataList.get(i).getAttachment().getName().equals(currentlyPlaying)) {
                    messageAdapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onVideosChosen(List<ChosenVideo> list) {
        if (list != null && !list.isEmpty() && new File(Uri.parse(list.get(0).getOriginalPath()).getPath()).length() / 1024 <= 10240)
            uploadThumbnail(Uri.parse(list.get(0).getOriginalPath()).getPath());
        else
            Toast.makeText(mContext, R.string.video_short_msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAudiosChosen(List<ChosenAudio> list) {
        if (list != null && !list.isEmpty())
            newFileUploadTask(Uri.parse(list.get(0).getOriginalPath()).getPath(), AttachmentTypes.AUDIO, null);
    }

    @Override
    public void onImagesChosen(List<ChosenImage> list) {
        if (list != null && !list.isEmpty()) {
            Uri originalFileUri = Uri.parse(list.get(0).getOriginalPath());
            //File tempFile = new File(getCacheDir(), originalFileUri.getLastPathSegment());
            try {
                uploadImage(new Compressor(mContext).compressToFile(new File(list.get(0).getOriginalPath())).getAbsolutePath());
            } catch (Exception ex) {
                uploadImage(originalFileUri.getPath());
            }
        }
    }

    @Override
    public void onError(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // You have to save path in case your activity is killed.
        // In such a scenario, you will need to re-initialize the CameraImagePicker
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // After Activity recreate, you need to re-intialize these
        // two values to be able to re-intialize CameraImagePicker
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("picker_path")) {
                pickerPath = savedInstanceState.getString("picker_path");
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private boolean permissionsAvailable(String[] permissions) {
        boolean granted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    public static Intent newIntent(Context context, Chat chat) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra(EXTRA_DATA_CHAT, chat);
        return intent;
    }

    @Override
    public void OnMessageClick(Message message, int position) {
        if (Constants.CHAT_CAB) {
            message.setSelected(!message.isSelected());//Toggle message selection
            messageAdapter.notifyItemChanged(position);//Notify changes

            if (message.isSelected())
                countSelected++;
            else
                countSelected--;

            getSupportActionBar().setTitle(String.valueOf(countSelected));//Update count
            if (countSelected == 0)
                undoSelectionPrepared();//If count is zero then reset selection
        }
    }

    @Override
    public void OnMessageLongClick(Message message, int position) {
        if (!Constants.CHAT_CAB) {//Prepare selection if not in selection mode
            prepareToSelect();
            message.setSelected(true);
            messageAdapter.notifyItemChanged(position);
            countSelected++;
            getSupportActionBar().setTitle(String.valueOf(countSelected));
        }
    }
}

