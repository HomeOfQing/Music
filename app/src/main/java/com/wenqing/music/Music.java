package com.wenqing.music;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.Serializable;

/**
 * Created by DELL on 2015/4/24.
 */
public class Music implements Serializable{
    private long id;
    private String displayName;
    private String data;
    private String title;
    private String album;
    private long albumId;
    private int duration;
    private String albumArt;
    private String artist;


    public Music() {
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getArtist() {
        return title.substring(0, title.lastIndexOf("-"));
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getAlbum() {
        return title.substring(title.lastIndexOf("-") + 2, title.lastIndexOf("."));
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDisplayName() {
        return displayName.substring(displayName.lastIndexOf("-")+2,displayName.lastIndexOf("."));
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getDuration() {
        return duration;
    }
    public String getFormatDuration() {
        int d = duration / 1000;
        int m = d / 60;
        int s = d % 60;
        return String.format("%2d:%02d", m, s);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", data='" + data + '\'' +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", albumId=" + albumId +
                ", duration=" + duration +
                ", albumArt='" + albumArt + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Music)) return false;

        Music music = (Music) o;

        if (albumId != music.albumId) return false;
        if (duration != music.duration) return false;
        if (id != music.id) return false;
        if (album != null ? !album.equals(music.album) : music.album != null) return false;
        if (albumArt != null ? !albumArt.equals(music.albumArt) : music.albumArt != null)
            return false;
        if (artist != null ? !artist.equals(music.artist) : music.artist != null) return false;
        if (data != null ? !data.equals(music.data) : music.data != null) return false;
        if (displayName != null ? !displayName.equals(music.displayName) : music.displayName != null)
            return false;
        if (title != null ? !title.equals(music.title) : music.title != null) return false;

        return true;
    }
}
