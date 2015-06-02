package com.wenqing.music;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wenqing.music.Event.ChangeMusicEvent;
import com.wenqing.music.Event.ChangeProgressEvent;
import com.wenqing.music.Event.MusicListEvent;
import com.wenqing.music.Event.NextMusicEvent;
import com.wenqing.music.Event.PlayMusicEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private ListView listView;
    private MusicAdapter adapter;
    private List<Music> musicList;
    private int position = -1;
    private RelativeLayout relativeLayout;
    private ImageView littleAlbum;
    private ImageButton littlePlay, littleNext;
    private TextView littleArtist, littleDisplayName;
    private ProgressBar progressBar;
    private boolean isFirst = true;
    private Music music;
    private boolean isPlaying;

    public int getPosition() {
        return position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        EventBus.getDefault().registerSticky(this);
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView);
        relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        littleAlbum = (ImageView) findViewById(R.id.imageView_little_album);
        littlePlay = (ImageButton) findViewById(R.id.imageButton_little_play);
        littleNext = (ImageButton) findViewById(R.id.imageButton_little_next);
        littleArtist = (TextView) findViewById(R.id.textView_little_artist);
        littleDisplayName = (TextView) findViewById(R.id.textView_little_display_name);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_little);
        relativeLayout.setOnClickListener(this);
        littlePlay.setOnClickListener(this);
        littleNext.setOnClickListener(this);
        if (isFirst) {
            try {
                restoreMusic();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void restoreMusic() throws IOException, ClassNotFoundException {
        InputStream in = openFileInput("currentMusic");
        ObjectInputStream inputStream = new ObjectInputStream(in);
        music = (Music) inputStream.readObject();
        Log.d("MusicService", music.toString());
        if (music != null) {
            initBottomView();
        }
    }



    private void loadMusicResource() {
        progressBar.setMax(music.getDuration());
        position = musicList.indexOf(music);
        adapter.showPlay(position);
        adapter.notifyDataSetChanged();
        littlePlay.setImageResource(
                (isPlaying ? R.drawable.note_btn_pause_white : R.drawable.note_btn_play_white));
        initBottomView();
    }

    private void initBottomView() {
        String artist = music.getTitle();
        littleDisplayName.setText(music.getDisplayName());
        littleArtist.setText(artist.substring(0, artist.lastIndexOf("-")));
        Bitmap bitmap = null;
        String albumArtPath = music.getAlbumArt();
        if (albumArtPath == null) {
            littleAlbum.setBackgroundResource(R.drawable.about_logo);
        } else {
            bitmap = BitmapFactory.decodeFile(albumArtPath);
            BitmapDrawable bmpDraw = new BitmapDrawable(bitmap);
            littleAlbum.setImageDrawable(bmpDraw);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ChangeMusicEvent e = EventBus.getDefault().getStickyEvent(ChangeMusicEvent.class);
        if (e != null) {
            isPlaying = e.isPlaying();
            music = e.getMusic();
            isFirst = e.isFirst();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.position = position;
        EventBus.getDefault().post(new PlayMusicEvent(musicList.get(position), "other"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_layout:
                Intent intent = new Intent(this, DetailActivity.class);
                //发空动作，只是把music传给Service
                EventBus.getDefault().postSticky(new PlayMusicEvent(music, "first"));
                startActivity(intent);
                break;
            case R.id.imageButton_little_next:
                EventBus.getDefault().post(new NextMusicEvent("next"));
                break;
            case R.id.imageButton_little_play:
                if (!isPlaying && isFirst) {
                    EventBus.getDefault().post(new PlayMusicEvent(music, "change"));
                    Log.d("DetailActivity", "发事件给服务了");
                    isFirst = false;
                } else {
                    EventBus.getDefault().post(new PlayMusicEvent(music, "play"));
                }
                loadMusicResource();
                break;
        }
    }

    interface ShowPlayState {
        void showPlay(int position);
    }

    /**
     * 接收服务发来的音乐列表
     * @param event
     */
    public void onEvent(MusicListEvent event) {
        musicList = event.getList();
        adapter = new MusicAdapter(this, musicList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }
//    public void onEventBackgroundThread(MusicListEvent event) {
//        musicList = event.getList();
//        adapter = new MusicAdapter(this, musicList);
//        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(this);
//    }
    /**
     * 接收服务发送来的音乐切换事件
     * @param event
     */
    public void onEvent(ChangeMusicEvent event) {
        music = event.getMusic();
        isPlaying = event.isPlaying();
        loadMusicResource();

    }

    /**
     * 服务发送进度改变事件
     * @param event
     */
    public void onEvent(ChangeProgressEvent event) {
        progressBar.setProgress(event.getProgress());
    }

}
