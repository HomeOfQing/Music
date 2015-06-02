package com.wenqing.music.Event;

import com.wenqing.music.Music;

/**
 * Created by DELL on 2015/4/28.
 */
public class ChangeMusicEvent {
    private Music music;
    private boolean isPlaying;
    private boolean isFirst;
    private String playMode;

    public ChangeMusicEvent(Music music, boolean isPlaying, boolean isFirst, String playMode) {
        this.music = music;
        this.isPlaying = isPlaying;
        this.isFirst = isFirst;
        this.playMode = playMode;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public String getPlayMode() {
        return playMode;
    }

    public Music getMusic() {
        return music;
    }
}
