package com.wenqing.music.Event;

/**
 * Created by DELL on 2015/4/28.
 */
public class NextMusicEvent {
    String action;

    public NextMusicEvent(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
