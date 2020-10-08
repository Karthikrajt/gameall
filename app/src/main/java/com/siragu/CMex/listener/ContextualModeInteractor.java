package com.siragu.CMex.listener;

public interface ContextualModeInteractor {
    void enableContextualMode();

    boolean isContextualMode();

    void updateSelectedCount(int count);
}