package com.siragu.gameall.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.siragu.gameall.R;
import com.siragu.gameall.adapter.ChatAdapter;
import com.siragu.gameall.model.Chat;
import com.siragu.gameall.model.Message;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.siragu.gameall.view.MyRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
public class MyChatsFragment extends Fragment {
    private MyRecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private UserResponse userMe;
    private DatabaseReference myInboxRef;
    private ArrayList<Chat> chatsAll = new ArrayList<Chat>();
    private Context mContext;
    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Message newMessage = dataSnapshot.getValue(Message.class);
            if (mContext != null && newMessage != null && newMessage.getId() != null && newMessage.getChatId() != null) {
                Chat newChat = new Chat(newMessage, newMessage.getSenderId().equals(userMe.getId().toString()));
                chatsAll.add(newChat);
                Collections.sort(chatsAll, (one, two) -> {
                    Long oneTime = Long.valueOf(one.getDateTimeStamp());
                    Long twoTime = Long.valueOf(two.getDateTimeStamp());
                    return twoTime.compareTo(oneTime);
                });
                refreshUnreadIndicatorFor(newChat.getChatId(), false);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Message updatedMessage = dataSnapshot.getValue(Message.class);
            if (mContext != null && updatedMessage != null && updatedMessage.getId() != null && updatedMessage.getChatId() != null) {
                Chat newChat = new Chat(updatedMessage, updatedMessage.getSenderId().equals(userMe.getId().toString()));
                int oldIndex = -1;
                for (int i = 0; i < chatsAll.size(); i++) {
                    if (newChat.getChatId().equals(chatsAll.get(i).getChatId())) {
                        oldIndex = i;
                        break;
                    }
                }
                if (oldIndex != -1) {
                    chatsAll.remove(oldIndex);
                    chatsAll.add(0, newChat);
                    refreshUnreadIndicatorFor(newChat.getChatId(), false);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_recycler, container, false);
        mContext = getContext();
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setEmptyView(view.findViewById(R.id.emptyView));
        recyclerView.setEmptyImageView(view.findViewById(R.id.emptyImage));
        recyclerView.setEmptyTextView(view.findViewById(R.id.emptyText));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferenceUtil sharedPreferenceUtil = new SharedPreferenceUtil(getContext());
        userMe = Helper.getLoggedInUser(sharedPreferenceUtil);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        myInboxRef = firebaseDatabase.getReference(Constants.REF_INBOX).child(userMe.getId().toString());

        chatsAll = new ArrayList<>();
        chatAdapter = new ChatAdapter(getActivity(), chatsAll);
        recyclerView.setAdapter(chatAdapter);
        myInboxRef.addChildEventListener(childEventListener);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext = null;
        if (myInboxRef != null && childEventListener != null)
            myInboxRef.removeEventListener(childEventListener);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (chatAdapter != null && chatAdapter.getItemCount() > 0)
                chatAdapter.notifyDataSetChanged();
        }
    }

    public void refreshUnreadIndicatorFor(String chatChild, boolean force) {
        if (mContext != null && chatAdapter != null && chatAdapter.getItemCount() > 0) {
            chatAdapter.loadLastReadIds(chatChild, force);
            chatAdapter.notifyDataSetChanged();
        }
    }

    //    public void disableContextualMode() {
//        chatAdapter.disableContextualMode();
//    }

}
