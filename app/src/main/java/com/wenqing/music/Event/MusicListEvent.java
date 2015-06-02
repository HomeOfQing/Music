package com.wenqing.music.Event;

import android.graphics.Bitmap;
import android.util.Log;

import com.wenqing.music.Music;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DELL on 2015/4/28.
 */
public class MusicListEvent {
    private ArrayList<Music> list;
    private ArrayList<Music> shuffled;
    private HashMap<Long, Bitmap> albums;


    public MusicListEvent(ArrayList<Music> list, ArrayList<Music> shuffled, HashMap<Long, Bitmap> albums) {
        this.list = list;
        this.shuffled = shuffled;
        this.albums = albums;
    }

    public ArrayList<Music> getList() {
        return list;
    }

    public ArrayList<Music> getShuffled() {
        return shuffled;
    }

    public HashMap<Long, Bitmap> getAlbums() {
        return albums;
    }
}
