package com.siragu.CMex.listener;

import java.io.File;

public interface RecordingViewInteractor {
    boolean isRecordingPlaying(String fileName);

    void playRecording(File file, String fileName, int position);
}
