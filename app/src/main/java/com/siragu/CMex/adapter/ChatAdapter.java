package com.siragu.CMex.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.siragu.CMex.R;
import com.siragu.CMex.activity.MessagesActivity;
import com.siragu.CMex.model.Chat;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.SharedPreferenceUtil;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<Chat> dataList;
    private ArrayList<String> lastReadIds;
    private SharedPreferenceUtil sharedPreferenceUtil;
    //private OnUserGroupItemClick itemClickListener;
    //private ContextualModeInteractor contextualModeInteractor;
    //private int selectedCount = 0;

    public ChatAdapter(Context context, ArrayList<Chat> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.sharedPreferenceUtil = new SharedPreferenceUtil(context);
        this.lastReadIds = new ArrayList<>();
        loadLastReadIds(null, false);
//        if (context instanceof OnUserGroupItemClick) {
//            this.itemClickListener = (OnUserGroupItemClick) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement OnUserGroupItemClick");
//        }

//        if (context instanceof ContextualModeInteractor) {
//            this.contextualModeInteractor = (ContextualModeInteractor) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement ContextualModeInteractor");
//        }
    }

    public void loadLastReadIds(String chatChild, boolean force) {
        if (lastReadIds == null) lastReadIds = new ArrayList<>();
        if (TextUtils.isEmpty(chatChild)) {
            if (!lastReadIds.isEmpty()) lastReadIds.clear();
        }

        if (sharedPreferenceUtil != null) {
            for (Chat chat : dataList) {
                if (TextUtils.isEmpty(chatChild)) {
                    if (force) {
                        lastReadIds.add(0, chat.getLastMessageId());
                    } else {
                        String lasReadMsgId = Helper.getLastRead(chat.getChatId(), sharedPreferenceUtil);
                        if (!TextUtils.isEmpty(lasReadMsgId))
                            lastReadIds.add(lasReadMsgId);
                    }
                } else {
                    if (chat.getChatId().equals(chatChild)) {
                        if (force) {
                            lastReadIds.add(0, chat.getLastMessageId());
                        } else {
                            String lasReadMsgId = Helper.getLastRead(chat.getChatId(), sharedPreferenceUtil);
                            if (!TextUtils.isEmpty(lasReadMsgId))
                                lastReadIds.add(lasReadMsgId);
                        }
                        break;
                    }
                }
            }
        }

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name, lastMessage, time;
        private ImageView image;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            time = itemView.findViewById(R.id.time);
            lastMessage = itemView.findViewById(R.id.message);
            image = itemView.findViewById(R.id.user_image);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != -1) {

                    Chat chat = dataList.get(pos);
                    Gson gson = new Gson();
                    String jsonStr = gson.toJson(chat);

                    Log.d("Chating Details",  jsonStr);
                    context.startActivity(MessagesActivity.newIntent(context, chat));
                }
//                    if (contextualModeInteractor.isContextualMode()) {
//                        toggleSelection(dataList.get(getAdapterPosition()), getAdapterPosition());
//                    } else {
//                        int pos = getAdapterPosition();
//                        if (pos != -1) {
//                            Chat chat = dataList.get(pos);
//                            if (chat.getUser() != null)
//                                itemClickListener.OnUserClick(chat.getUser(), pos, image);
//                        }
//                    }
            });
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    contextualModeInteractor.enableContextualMode();
//                    toggleSelection(dataList.get(getAdapterPosition()), getAdapterPosition());
//                    return true;
//                }
//            });
        }

        private void setData(Chat chat) {
//            Glide.with(context).load(chat.getChatImage()).apply(new RequestOptions().placeholder(R.drawable.ic_person_gray_24dp)).into(image);
            Glide.with(context).load(chat.getChatImage())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(Helper.dp2px(context, 5))).override(Helper.dp2px(context, 38), Helper.dp2px(context, 38)).placeholder(R.drawable.ic_person_gray_24dp))
                    .into(image);
            name.setText(chat.getChatName());
            time.setText(chat.getTimeDiff());
            lastMessage.setText(chat.getLastMessage());
            lastMessage.setTextColor(ContextCompat.getColor(context, !chat.isRead() ? R.color.colorText : R.color.colorTextSecondary));
            lastMessage.setCompoundDrawablesWithIntrinsicBounds((lastReadIds != null && lastReadIds.contains(chat.getLastMessageId())) ? 0 : R.drawable.circle_unread, 0, 0, 0);
            //user_details_container.setBackgroundColor(ContextCompat.getColor(context, (chat.isSelected() ? R.color.bg_gray : R.color.white)));
        }
    }

//    private void toggleSelection(Chat chat, int position) {
//        chat.setSelected(!chat.isSelected());
//        notifyItemChanged(position);
//
//        if (chat.isSelected())
//            selectedCount++;
//        else
//            selectedCount--;
//
//        contextualModeInteractor.updateSelectedCount(selectedCount);
//    }
//
//    public void disableContextualMode() {
//        selectedCount = 0;
//        for (int i = 0; i < dataList.size(); i++) {
//            if (dataList.get(i).isSelected()) {
//                dataList.get(i).setSelected(false);
//                notifyItemChanged(i);
//            }
//        }
//    }

}
