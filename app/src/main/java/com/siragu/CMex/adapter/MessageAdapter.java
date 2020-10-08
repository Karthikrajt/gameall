package com.siragu.CMex.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.vanniktech.emoji.EmojiTextView;
import com.siragu.CMex.R;
import com.siragu.CMex.activity.ImageViewerActivity;
import com.siragu.CMex.listener.OnMessageItemClick;
import com.siragu.CMex.listener.RecordingViewInteractor;
import com.siragu.CMex.model.AttachmentTypes;
import com.siragu.CMex.model.DownloadFileEvent;
import com.siragu.CMex.model.Message;
import com.siragu.CMex.util.ClickableMovementMethod;
import com.siragu.CMex.util.Constants;
import com.siragu.CMex.util.FileUtils;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.LinkTransformationMethod;
import com.siragu.CMex.util.MyFileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.BaseMessageViewHolder> {
    private OnMessageItemClick itemClickListener;
    private RecordingViewInteractor recordingViewInteractor;
    private Integer _48dpInPx;
    private String myId;
    private Context context;
    private ArrayList<Message> dataList;

    public MessageAdapter(Context context, ArrayList<Message> messages, String myId) {
        this.context = context;
        this.dataList = messages;
        this.myId = myId;
        this._48dpInPx = Helper.dp2px(context, 48);

        if (context instanceof OnMessageItemClick) {
            this.itemClickListener = (OnMessageItemClick) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnUserGroupItemClick");
        }

        if (context instanceof RecordingViewInteractor) {
            this.recordingViewInteractor = (RecordingViewInteractor) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RecordingViewInteractor");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).getAttachmentType();
    }

    @NonNull
    @Override
    public BaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case AttachmentTypes.RECORDING:
                return new RecordingMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_attachment_recording, parent, false));
            case AttachmentTypes.IMAGE:
                return new ImageMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_attachment_image, parent, false));
            case AttachmentTypes.VIDEO:
                return new VideoMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_attachment_video, parent, false));
            case AttachmentTypes.NONE_TYPING:
                return new TypingMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_typing, parent, false));
            case AttachmentTypes.NONE_TEXT:
            default:
                return new TextMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_text, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseMessageViewHolder holder, int position) {
        holder.setData(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class TextMessageViewHolder extends BaseMessageViewHolder {
        private EmojiTextView text;

        TextMessageViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            text.setTransformationMethod(new LinkTransformationMethod());
            text.setMovementMethod(ClickableMovementMethod.getInstance());
            // Reset for TextView.fixFocusableAndClickableSettings(). We don't want View.onTouchEvent()
            // to consume touch events.
            text.setClickable(false);
            text.setLongClickable(false);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        onItemClick(true, pos);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        onItemClick(false, pos);
                    }
                    return true;
                }
            });
        }

        @Override
        protected void setData(Message message) {
            super.setData(message);
            LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) text.getLayoutParams();
            text.setGravity(isMine() ? Gravity.END : Gravity.START);
            textParams.gravity = isMine() ? Gravity.END : Gravity.START;
            text.setLayoutParams(textParams);
            text.setText(message.getBody());
            text.setTextColor(ContextCompat.getColor(context, isMine() ? android.R.color.white : android.R.color.black));
        }
    }

    class TypingMessageViewHolder extends BaseMessageViewHolder {
        TypingMessageViewHolder(View itemView) {
            super(itemView);
        }
    }

    class VideoMessageViewHolder extends BaseMessageViewHolder {
        private File file;
        private TextView text, durationOrSize;
        private ImageView videoThumbnail, videoPlay;
        private ProgressBar progressBar;

        VideoMessageViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            durationOrSize = itemView.findViewById(R.id.videoSize);
            videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            videoPlay = itemView.findViewById(R.id.videoPlay);
            progressBar = itemView.findViewById(R.id.progressBar);

            itemView.findViewById(R.id.videoPlay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1)
                        downloadFile(pos);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        onItemClick(true, pos);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        onItemClick(false, pos);
                    }
                    return true;
                }
            });
        }

        @Override
        protected void setData(Message message) {
            super.setData(message);
            boolean loading = message.getAttachment().getUrl().equals("loading");
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            videoPlay.setVisibility(loading ? View.GONE : View.VISIBLE);

            text.setText(message.getAttachment().getName());
            Glide.with(context).load(message.getAttachment().getData()).apply(new RequestOptions().placeholder(R.drawable.ic_video_24dp).centerCrop()).into(videoThumbnail);

            file = new File(Environment.getExternalStorageDirectory() + "/"
                    +
                    context.getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(message.getAttachmentType()) + (isMine() ? "/.sent/" : "")
                    , message.getAttachment().getName());
            if (file.exists()) {
//            Uri uri = Uri.fromFile(file);
//            try {
//                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//                mmr.setDataSource(context, uri);
//                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//                long millis = Long.parseLong(durationStr);
//                durationOrSize.setText(TimeUnit.MILLISECONDS.toMinutes(millis) + ":" + TimeUnit.MILLISECONDS.toSeconds(millis));
//                Log.e("CHECK", String.valueOf(millis));
//                mmr.release();
//            } catch (RuntimeException e) {
//                Cursor cursor = MediaStore.Video.query(context.getContentResolver(), uri, new
//                        String[]{MediaStore.Video.VideoColumns.DURATION});
//                long duration = 0;
//                if (cursor != null && cursor.moveToFirst()) {
//                    duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
//                    Log.e("CHECK", String.valueOf(duration));
//                    durationOrSize.setText(TimeUnit.MILLISECONDS.toMinutes(duration) + ":" + TimeUnit.MILLISECONDS.toSeconds(duration));
//                }
//                if (cursor != null && !cursor.isClosed())
//                    cursor.close();
//            }
            } else
                durationOrSize.setText(FileUtils.getReadableFileSize(message.getAttachment().getBytesCount()));

            videoPlay.setImageDrawable(ContextCompat.getDrawable(context, file.exists() ? R.drawable.ic_play_circle_outline : R.drawable.ic_file_download_40dp));
        }

        void downloadFile(int pos) {
            if (!Constants.CHAT_CAB)
                if (file.exists()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = MyFileProvider.getUriForFile(context,
                            context.getString(R.string.authority),
                            file);
                    intent.setDataAndType(uri, FileUtils.getMimeType(context, uri)); //storage path is path of your vcf file and vFile is name of that file.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                } else if (!isMine())
                    broadcastDownloadEvent(dataList.get(pos));
                else
                    Toast.makeText(context, "File unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    class ImageMessageViewHolder extends BaseMessageViewHolder {
        private ImageView image;

        ImageMessageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        if (!Constants.CHAT_CAB)
                            context.startActivity(ImageViewerActivity.newInstance(context, dataList.get(pos).getAttachment().getUrl()));
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        onItemClick(true, pos);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        onItemClick(false, pos);
                    }
                    return true;
                }
            });
        }

        @Override
        protected void setData(Message message) {
            super.setData(message);
            Glide.with(context).load(message.getAttachment().getUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                    .into(image);
        }
    }

    class RecordingMessageViewHolder extends BaseMessageViewHolder {
        private TextView text, durationOrSize;
        private ProgressBar progressBar;
        private ImageView playPauseToggle;
        private File file;

        RecordingMessageViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            durationOrSize = itemView.findViewById(R.id.duration);
            progressBar = itemView.findViewById(R.id.progressBar);
            playPauseToggle = itemView.findViewById(R.id.playPauseToggle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        if (!Constants.CHAT_CAB)
                            downloadFile(pos);
                        onItemClick(true, pos);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        onItemClick(false, pos);
                    }
                    return true;
                }
            });
        }

        @Override
        protected void setData(Message message) {
            super.setData(message);
            boolean loading = message.getAttachment().getUrl().equals("loading");
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            playPauseToggle.setVisibility(loading ? View.GONE : View.VISIBLE);

            file = new File(Environment.getExternalStorageDirectory() + "/"
                    +
                    context.getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(message.getAttachmentType()) + (isMine() ? "/.sent/" : "")
                    , message.getAttachment().getName());
            if (file.exists()) {
                Uri uri = Uri.fromFile(file);
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(context, uri);
                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    int millis = Integer.parseInt(durationStr);
                    durationOrSize.setText(TimeUnit.MILLISECONDS.toMinutes(millis) + ":" + TimeUnit.MILLISECONDS.toSeconds(millis));
                    mmr.release();
                } catch (Exception e) {
                }
            } else
                durationOrSize.setText(FileUtils.getReadableFileSize(message.getAttachment().getBytesCount()));

            playPauseToggle.setImageDrawable(ContextCompat.getDrawable(context, file.exists() ? recordingViewInteractor.isRecordingPlaying(message.getAttachment().getName()) ? R.drawable.ic_stop : R.drawable.ic_play_circle_outline : R.drawable.ic_file_download_accent_36dp));
        }

        void downloadFile(int pos) {
            if (file.exists()) {
                recordingViewInteractor.playRecording(file, dataList.get(pos).getAttachment().getName(), pos);
            } else if (!isMine() && !dataList.get(pos).getAttachment().getUrl().equals("loading")) {
                broadcastDownloadEvent(dataList.get(pos));
            } else {
                Toast.makeText(context, R.string.file_no, Toast.LENGTH_SHORT).show();
            }
        }
    }

    class BaseMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView time, senderName;
        private CardView cardView;
        private LinearLayout ll;
        private boolean isMine, done;

        BaseMessageViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            senderName = itemView.findViewById(R.id.senderName);
            cardView = itemView.findViewById(R.id.card_view);
            ll = itemView.findViewById(R.id.container);
        }

        protected void setData(Message message) {
            this.isMine = myId.equals(message.getSenderId());
            if (message.getAttachmentType() == AttachmentTypes.NONE_TYPING)
                return;

            senderName.setVisibility(View.GONE);
            //senderName.setText(isMine() ? "You" : message.getSenderName());
            time.setText(message.getTimeDiff());
            time.setTextColor(ContextCompat.getColor(context, isMine() ? R.color.card_msg_time_color_me : R.color.card_msg_time_color));
            time.setCompoundDrawablesWithIntrinsicBounds(0, 0, isMine() ? R.drawable.ic_done_blue : 0, 0);
            //time.setCompoundDrawablesWithIntrinsicBounds(0, 0, isMine() ? (message.isSent() ? (message.isDelivered() ? R.drawable.ic_done_all_black : R.drawable.ic_done_black) : R.drawable.ic_waiting) : 0, 0);
            //cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorDivider : R.color.colorBgLight));
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, isMine() ? R.color.colorPrimary : android.R.color.white));
            //ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorDivider) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));
            ll.setBackgroundResource(message.isSelected() ? (isMine() ? R.drawable.gradient_selected_right : R.drawable.gradient_selected_left) : 0);

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) cardView.getLayoutParams();
            if (isMine()) {
                layoutParams.gravity = Gravity.END;
                layoutParams.setMargins(_48dpInPx, 0, 0, 0);
            } else {
                layoutParams.gravity = Gravity.START;
                layoutParams.setMargins(0, 0, _48dpInPx, 0);
            }
            cardView.setLayoutParams(layoutParams);
        }

        boolean isMine() {
            return this.isMine;
        }

        void onItemClick(boolean b, int pos) {
            if (itemClickListener != null) {
                if (b)
                    itemClickListener.OnMessageClick(dataList.get(pos), pos);
                else
                    itemClickListener.OnMessageLongClick(dataList.get(pos), pos);
            }
        }

        void broadcastDownloadEvent(Message message) {
            Intent intent = new Intent(Constants.BROADCAST_DOWNLOAD_EVENT);
            intent.putExtra("data", new DownloadFileEvent(message.getAttachmentType(), message.getAttachment(), getAdapterPosition()));
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
