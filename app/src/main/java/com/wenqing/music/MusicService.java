package com.wenqing.music;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.wenqing.music.Event.ChangeMusicEvent;
import com.wenqing.music.Event.ChangeProgressEvent;
import com.wenqing.music.Event.ChangeServiceProgressEvent;
import com.wenqing.music.Event.MusicListEvent;
import com.wenqing.music.Event.NextMusicEvent;
import com.wenqing.music.Event.PlayModeEvent;
import com.wenqing.music.Event.PlayMusicEvent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "MusicService";
    public static final int REQ_CANCEL = 2;
    public static final int REQ_NEXT = 3;
    public static final int REQ_PRE = 4;
    public static final int REQ_PLAY = 5;
    private MediaPlayer mediaPlayer;
    //播放列表
    private ArrayList<Music> musicList = new ArrayList<>(0);
    private ArrayList<Music> musicListShuffled = new ArrayList<>(0);
    private HashMap<Long, Bitmap> albums = new HashMap<>();
    private Music music;
    private int current;
    private boolean isFirst = true;
    private String action;
    private boolean hasMusicList;
    private Handler handler;
    private Runnable callback = new Runnable() {
        @Override
        public void run() {
            EventBus.getDefault().post(new ChangeProgressEvent(mediaPlayer.getCurrentPosition()));
            handler.postDelayed(this, 1000);
        }
    };
    private Notification notification;
    private RemoteViews remoteView;
    private Bitmap disc, center;
    private MyReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new MyReceiver();
        registerReceiver(receiver, new IntentFilter("com.wenqing.app.ACTION_NOTIFICATION"));
        EventBus.getDefault().register(this);
        SharedPreferences preferences = getBaseContext().getSharedPreferences("playMode", MODE_PRIVATE);
        action = preferences.getString("mode", "顺序播放");
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        try {
            ObjectInputStream in = new ObjectInputStream(getApplicationContext().openFileInput("currentMusic"));
            music = (Music) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().postSticky(new ChangeMusicEvent(music, mediaPlayer.isPlaying(), isFirst, action));
        initMusicList();
//        EventBus.getDefault().postSticky(new MusicListEvent(musicList, musicListShuffled, albums));
        loadAlbumBitmap();
        EventBus.getDefault().postSticky(new MusicListEvent(musicList, musicListShuffled, albums));
    }

    private void loadAlbumBitmap() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (Music m : musicList) {
                    disc = BitmapFactory.decodeResource(getResources(), R.drawable.app_widget_default);
                    disc = MusicUtil.big(disc);
                    String albumArt = m.getAlbumArt();
                    if (albumArt != null) {
                        center = BitmapFactory.decodeFile(albumArt);
                        center = MusicUtil.getRoundedCornerBitmap(center);
                        disc = MusicUtil.combineBitmap(disc, center);
                    }
                    albums.put(m.getId(), disc);
                }
                Log.d("MusicService", "done");
            }
        }.start();
    }

    private Notification getNotification() {
        remoteView = new RemoteViews(getPackageName(), R.layout.notification_music);
        Bitmap bitmap = BitmapFactory.decodeFile(music.getAlbumArt());
        remoteView.setImageViewBitmap(R.id.imageView_noti_album, bitmap);
        remoteView.setImageViewResource(R.id.imageButton_noti_play, (mediaPlayer.isPlaying() ? R.drawable.note_btn_pause : R.drawable.desklrc_btn_play));
        remoteView.setTextViewText(R.id.textView_noti_title, music.getAlbum());
        remoteView.setTextViewText(R.id.textView_noti_artist, music.getArtist());
//        remoteView.setOnClickPendingIntent(R.id.imageButton_noti_pre,
//                Pe);
        Intent intent = new Intent(getApplicationContext(), MyReceiver.class);
        intent.putExtra("REQCODE", REQ_CANCEL);
        remoteView.setOnClickPendingIntent(R.id.imageButton_noti_cancel,
                PendingIntent.getBroadcast(getApplicationContext(), REQ_CANCEL, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        Intent intent1 = new Intent(getApplicationContext(), MyReceiver.class);
        intent1.putExtra("REQCODE", REQ_NEXT);
        remoteView.setOnClickPendingIntent(R.id.imageButton_noti_next,
                PendingIntent.getBroadcast(getApplicationContext(), REQ_NEXT, intent1, PendingIntent.FLAG_CANCEL_CURRENT));
        Intent intent2 = new Intent(getApplicationContext(), MyReceiver.class);
        intent2.putExtra("REQCODE", REQ_PRE);
        remoteView.setOnClickPendingIntent(R.id.imageButton_noti_pre,
                PendingIntent.getBroadcast(getApplicationContext(), REQ_PRE, intent2, PendingIntent.FLAG_CANCEL_CURRENT));
        Intent intent3 = new Intent(getApplicationContext(), MyReceiver.class);
        intent3.putExtra("REQCODE", REQ_PLAY);
        intent3.putExtra("music", music);
        remoteView.setOnClickPendingIntent(R.id.imageButton_noti_play,
                PendingIntent.getBroadcast(getApplicationContext(), REQ_PLAY, intent3, PendingIntent.FLAG_CANCEL_CURRENT));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.song_play_icon)
                .setLargeIcon(BitmapFactory.decodeFile(music.getAlbumArt()))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContent(remoteView)
                .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, DetailActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));

        notification = builder.build();
        notification.bigContentView = remoteView;
        return notification;
    }


    private void initMusicList() {
        Uri uri = Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                Media._ID,
                Media.ALBUM,
                Media.DATA,
                Media.DURATION,
                Media.TITLE,
                Media.DISPLAY_NAME,
                Media.ALBUM_ID,
                Media.ARTIST
        };
        String selection = Media.IS_MUSIC + "= ?";
        String[] selectionArgs = {"1"};
        String sortOrder = Media.ARTIST;
        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        while (cursor.moveToNext()) {
            Music m = new Music();
            long id = cursor.getLong(cursor.getColumnIndex(Media._ID));
            String displayName = cursor.getString(cursor.getColumnIndex(Media.DISPLAY_NAME));
            String album = cursor.getString(cursor.getColumnIndex(Media.ALBUM));
            String data = cursor.getString(cursor.getColumnIndex(Media.DATA));
            long albumID = cursor.getLong(cursor.getColumnIndex(Media.ALBUM_ID));
            int duration = cursor.getInt(cursor.getColumnIndex(Media.DURATION));
            String albumArt = getAlbumArt(albumID);
            String artist = cursor.getString(cursor.getColumnIndex(Media.ARTIST));
            m.setId(id);
            m.setAlbum(album);
            m.setAlbumId(albumID);
            m.setDisplayName(displayName);
            m.setData(data);
            m.setDuration(duration);
            m.setTitle(displayName);
            m.setAlbumArt(albumArt);
            m.setArtist(artist);
            musicList.add(m);
        }
        cursor.close();

        musicListShuffled.addAll(musicList);
        Collections.shuffle(musicListShuffled);

    }

    private String getAlbumArt(long album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = this.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + String.valueOf(album_id)),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
        handler.removeCallbacks(callback);
        try {
            saveInstanceState();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaPlayer.release();
            stopForeground(true);
        }

    }

    private void saveInstanceState() throws IOException {
        if (music != null) {
            OutputStream out = getBaseContext().openFileOutput("currentMusic", MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(music);
            objectOutputStream.close();
        }
        SharedPreferences preferences = getBaseContext().getSharedPreferences("playMode", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("mode", action);
        editor.commit();

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (action.equals("单曲循环")) {
            play(current);
        } else {
            doPlayNext();
        }
    }

    /**
     * DetailActivity发来改变进度事件
     *
     * @param e
     */
    public void onEvent(ChangeServiceProgressEvent e) {
        mediaPlayer.seekTo(e.getProgress());
    }

    /**
     * 活动发来播放音乐事件
     *
     * @param e
     */
    public void onEvent(PlayMusicEvent e) {
        music = e.getMusic();
        String ac = e.getAction();
        int index = (action.equals("随机播放") ? musicListShuffled : musicList).indexOf(music);
        if (ac.equals("change")) {
            play(index);
        } else if (ac.equals("play")) {
            doPlay();
        } else if (ac.equals("other")) {

            justPlay();

        } else if (ac.equals("first")) {
            current = index;
        }
    }

    /**
     * 活动发来换歌事件
     *
     * @param e
     */
    public void onEvent(NextMusicEvent e) {
        if (e.getAction().equals("next")) {
            doPlayNext();
        } else if (e.getAction().equals("pre")) {
            doPlayPrevious();
        }
    }


    /**
     * 活动发来改变播放模式事件
     *
     * @param e
     */
    public void onEvent(PlayModeEvent e) {
        action = e.getPlayMode();
        if (action.equals("随机播放")) {
            current = musicListShuffled.indexOf(music);
        } else {
            current = musicList.indexOf(music);
        }
//        EventBus.getDefault().postSticky(new ChangeMusicEvent(music, mediaPlayer.isPlaying(), isFirst, action));
        EventBus.getDefault().postSticky(new MusicListEvent(musicList, musicListShuffled, albums));
    }


    public void doPlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
        //获得通知管理
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //切歌
        nm.notify(1, getNotification());
        EventBus.getDefault().postSticky(new ChangeMusicEvent(music, mediaPlayer.isPlaying(), isFirst, action));
//        Log.d("DetailActivity","doPlay");

    }

    public void doPlayPrevious() {
        if (current == 0) {
            current = musicList.size() - 1;
        } else {
            current--;
        }
        play(current);
    }

    public void doPlayNext() {
        if (current == musicList.size() - 1) {
            current = 0;
        } else {
            current++;
        }
        play(current);
    }

    public void play(int position) {
        this.current = position;
        switchMode(position);
        justPlay();
    }

    private void justPlay() {
        mediaPlayer.reset();
        if (!hasMusicList) {
            startForeground(1, getNotification());
            hasMusicList = true;
        }
        try {
            mediaPlayer.setDataSource(music.getData());
            mediaPlayer.prepare();
            mediaPlayer.start();
            Log.d("DetailActivity", "发送黏着消息");
            isFirst = false;
            current = musicList.indexOf(music);
            EventBus.getDefault().postSticky(new ChangeMusicEvent(music, mediaPlayer.isPlaying(), isFirst, action));
            handler.post(callback);
            //获得通知管理
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //切歌
            nm.notify(1, getNotification());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchMode(int position) {
        switch (action) {
            case "顺序播放":
                music = musicList.get(position);
                break;
            case "随机播放":
                music = musicListShuffled.get(position);
                break;
            default:
                music = musicList.get(position);
        }
    }

}
