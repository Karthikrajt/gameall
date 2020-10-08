package com.siragu.CMex.listener;

import com.siragu.CMex.model.Message;

public interface OnMessageItemClick {
    void OnMessageClick(Message message, int position);

    void OnMessageLongClick(Message message, int position);
}
