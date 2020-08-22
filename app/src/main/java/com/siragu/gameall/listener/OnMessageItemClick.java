package com.siragu.gameall.listener;

import com.siragu.gameall.model.Message;

public interface OnMessageItemClick {
    void OnMessageClick(Message message, int position);

    void OnMessageLongClick(Message message, int position);
}
