package com.wenqing.music.Event;

/**
 * Created by DELL on 2015/4/28.
 */
public class ChangeProgressEvent {
    int progress;

    public ChangeProgressEvent(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }
}
