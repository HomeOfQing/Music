package com.wenqing.music.Event;

import com.wenqing.music.Music;

/**
 * Created by DELL on 2015/4/28.
 */
public class PlayMusicEvent {
    private Music music;
    String action;
    public PlayMusicEvent(Music music, String action) {
        this.music = music;
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public Music getMusic() {
        return music;
    }
}
