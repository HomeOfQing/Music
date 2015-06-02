package com.wenqing.music;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wenqing.music.Event.ChangeMusicEvent;
import com.wenqing.music.Event.ChangeProgressEvent;
import com.wenqing.music.Event.ChangeServiceProgressEvent;
import com.wenqing.music.Event.MusicListEvent;
import com.wenqing.music.Event.NextMusicEvent;
import com.wenqing.music.Event.PlayModeEvent;
import com.wenqing.music.Event.PlayMusicEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;


public class DetailActivity extends ActionBarActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "DetailActivity";
    private Music music;
    private ArrayList<Music> musics, shuffledList, normalList;
    private TextView seek, duration;
    private ImageButton playMode, previous, next, play, list;
    private ImageView needle;
    private SeekBar seekBar;
    private boolean isPlaying, isFirst = true;
    private ActionBar actionBar;
    private int[] modeIcns;
    private String progress = "", action;
    private ArrayList<String> modes;
    private int i;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private int position;
    private RotateAnimation animation, animation2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        EventBus.getDefault().registerSticky(this);
        modeIcns = new int[]{R.drawable.play_icn_one_prs, R.drawable.play_icn_shuffle_prs, R.drawable.play_icn_loop_prs};
        modes = new ArrayList<>();
        modes.add("单曲循环");
        modes.add("随机播放");
        modes.add("顺序播放");
        MusicListEvent event = EventBus.getDefault().getStickyEvent(MusicListEvent.class);
        normalList = event.getList();
        shuffledList = event.getShuffled();
        PlayMusicEvent event1 = EventBus.getDefault().getStickyEvent(PlayMusicEvent.class);
        music = event1.getMusic();
        initView();
//        Log.d(TAG, music.toString());
        loadMusic();
        SharedPreferences preferences = getSharedPreferences("playMode", MODE_PRIVATE);
        action = preferences.getString("mode", "顺序播放");
        switchList();
        i = modes.indexOf(action);
//        Log.d(TAG, "init" + i);
        playMode.setImageResource(modeIcns[i]);
        viewPager.setCurrentItem(musics.indexOf(music));
    }

    private void switchList() {
        if (action.equals("随机播放")) {
            musics = shuffledList;
        } else {
            musics = normalList;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        adapter = new ViewPagerAdapter(fragmentManager, musics);
        viewPager.setAdapter(adapter);
        this.position = musics.indexOf(music);
        viewPager.setCurrentItem(position);
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
        adapter.notifyDataSetChanged();


    }

    private void initView() {
        actionBar = getSupportActionBar();
        seek = (TextView) findViewById(R.id.textView_seek);
        duration = (TextView) findViewById(R.id.duration);
        playMode = (ImageButton) findViewById(R.id.imageButton_play_mode);
        previous = (ImageButton) findViewById(R.id.imageButton_previous);
        next = (ImageButton) findViewById(R.id.imageButton_next);
        play = (ImageButton) findViewById(R.id.imageButton_play);
        list = (ImageButton) findViewById(R.id.imageButton_list);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        needle = (ImageView) findViewById(R.id.imageView_needle);
        seekBar.setOnSeekBarChangeListener(this);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        playMode.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        play.setOnClickListener(this);
        list.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getInfo();
    }

    private void getInfo() {
        ChangeMusicEvent e = EventBus.getDefault().getStickyEvent(ChangeMusicEvent.class);
        if (e != null) {
            isPlaying = e.isPlaying();
            music = e.getMusic();
            isFirst = e.isFirst();
            Log.d(TAG, isFirst + "");
            i = modes.indexOf(e.getPlayMode());
            playMode.setImageResource(modeIcns[i]);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void loadMusic() {
        actionBar.setLogo(R.drawable.desk_logo);
        String artist = music.getArtist();
        String album = music.getAlbum();
        actionBar.setTitle(album);
        actionBar.setSubtitle(artist);
        if (progress.length() == 0) {
            seek.setText("00:00");
        } else {
            seek.setText(progress);
        }
        seekBar.setMax(music.getDuration());
        duration.setText(music.getFormatDuration());
        play.setImageResource(isPlaying ?
                R.drawable.play_btn_pause_prs : R.drawable.play_btn_play_prs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton_play:
                if (isFirst && !isPlaying) {
                    Log.d(TAG, music.toString());
                    isFirst = false;
                    EventBus.getDefault().post(new PlayMusicEvent(music, "other"));
                } else {
                    Log.d(TAG, "coming");
                    EventBus.getDefault().post(new PlayMusicEvent(music, "play"));
                }
                play.setImageResource(isPlaying ?
                        R.drawable.play_btn_pause_prs : R.drawable.play_btn_play_prs);
                break;
            case R.id.imageButton_next:
                EventBus.getDefault().post(new NextMusicEvent("next"));
                break;
            case R.id.imageButton_previous:
                EventBus.getDefault().post(new NextMusicEvent("pre"));
                break;
            case R.id.imageButton_play_mode:
                EventBus.getDefault().post(new PlayModeEvent(doToast()));
                break;
            case R.id.imageButton_list:
                finish();
                break;
        }
    }


    private String doToast() {
        i++;
        if (i == 3) {
            i = 0;
        }
        Log.d(TAG, "doToast" + String.valueOf(i));
        action = modes.get(i);
        playMode.setImageResource(modeIcns[i]);
        Toast.makeText(this, action, Toast.LENGTH_SHORT).show();
        return action;
    }

    //seekBar 进度改变监听器
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            EventBus.getDefault().post(new ChangeServiceProgressEvent(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void onEvent(MusicListEvent event) {
//        if (action.equals("随机播放")){
//            musics = shuffledList;
//        }else {
//            musics = normalList;
//        }
//        if(adapter != null){
//            adapter.notifyDataSetChanged();
//        }
        switchList();
    }

    public void onEvent(ChangeMusicEvent event) {
        Music changedMusic = event.getMusic();
        music = changedMusic;
        isPlaying = event.isPlaying();
        action = event.getPlayMode();
        loadMusic();
        switchList();
    }

    public void onEvent(ChangeProgressEvent event) {
        int p = event.getProgress();
        seekBar.setProgress(p);
        int m = p / 1000 / 60;
        int s = p / 1000 % 60;
        progress = String.format("%2d:%02d", m, s);
        seek.setText(progress);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Music> data;
        private FragmentManager fm;

        public ViewPagerAdapter(FragmentManager fm, ArrayList<Music> data) {
            super(fm);
            this.data = data;
            this.fm = fm;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return MusicFragment.newInstance(data.get(position));
        }

        @Override
        public int getCount() {
            return data.size();
        }
    }
//    FragmentPagerAdapter
//    class ViewPagerAdapter extends PagerAdapter {
//        List<Music> data;
//        private final FragmentManager mFragmentManager;
//        private FragmentTransaction mCurTransaction = null;
//        private Fragment mCurrentPrimaryItem = null;
//
//        public ViewPagerAdapter(FragmentManager fragmentManager, List<Music> musics) {
//            this.mFragmentManager = fragmentManager;
//            this.data = musics;
//        }
//        public Fragment getItem(int position){
//            DetailActivity.this.position = position;
//            return MusicFragment.newInstance(data.get(position));
//        }
//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            if (mCurTransaction == null) {
//                mCurTransaction = mFragmentManager.beginTransaction();
//            }
//            final long itemId = getItemId(position);
//            // Do we already have this fragment?
//            String name = makeFragmentName(container.getId(), itemId);
//            Fragment fragment = mFragmentManager.findFragmentByTag(name);
//            if (fragment != null) {
//                mCurTransaction.attach(fragment);
//            } else {
//                fragment = getItem(position);
//                mCurTransaction.add(container.getId(), fragment,
//                        makeFragmentName(container.getId(), itemId));
//            }
//            if (fragment != mCurrentPrimaryItem) {
//                fragment.setMenuVisibility(false);
//                fragment.setUserVisibleHint(false);
//            }
//
//            return fragment;
//        }
//        public long getItemId(int position) {
//            return position;
//        }
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            if (mCurTransaction == null) {
//                mCurTransaction = mFragmentManager.beginTransaction();
//            }
//            mCurTransaction.detach((Fragment)object);
//        }
//
//        @Override
//        public void setPrimaryItem(ViewGroup container, int position, Object object) {
//            Fragment fragment = (Fragment)object;
//            if (fragment != mCurrentPrimaryItem) {
//                if (mCurrentPrimaryItem != null) {
//                    mCurrentPrimaryItem.setMenuVisibility(false);
//                    mCurrentPrimaryItem.setUserVisibleHint(false);
//                }
//                if (fragment != null) {
//                    fragment.setMenuVisibility(true);
//                    fragment.setUserVisibleHint(true);
//                }
//                mCurrentPrimaryItem = fragment;
//            }
//        }
//
//        @Override
//        public void finishUpdate(ViewGroup container) {
//            if (mCurTransaction != null) {
//                mCurTransaction.commitAllowingStateLoss();
//                mCurTransaction = null;
//                mFragmentManager.executePendingTransactions();
//            }
//        }
//
//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            return ((Fragment)object).getView() == view;
//        }
//        private  String makeFragmentName(int viewId, long id) {
//            return "android:switcher:" + viewId + ":" + id;
//        }
//        @Override
//        public int getCount() {
//            return data.size();
//        }
//    }

    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        float offset = 0;
        int counter = 0;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            RelativeLayout layout = new RelativeLayout(getApplicationContext());
//            layout.getWidth()
// EventBus.getDefault().post(new NextMusicEvent(""));
//            Log.d(TAG, "positionOffset " +positionOffset+"positionOffsetPixels"+positionOffsetPixels);
//            if(counter == 0) {
//                offset = positionOffset;
//                counter++;
//            }
//            if(positionOffset > 0.99 && positionOffset > offset){
//                EventBus.getDefault().post(new NextMusicEvent("pre"));
//                return;
//            }if(positionOffset < 0.1 && positionOffset < offset){
//                EventBus.getDefault().post(new NextMusicEvent("next"));
//                return;
//            }
//            int index = musics.indexOf(music);
//            position = viewPager.getCurrentItem();
//            if(position > index) {
//                EventBus.getDefault().post(new NextMusicEvent("next"));
//            }if(position < index){
//                EventBus.getDefault().post(new NextMusicEvent("pre"));
//            }
        }

        @Override
        public void onPageSelected(int position) {
            if (DetailActivity.this.position > position) {
                EventBus.getDefault().post(new NextMusicEvent("pre"));

            } else if (DetailActivity.this.position < position) {
                EventBus.getDefault().post(new NextMusicEvent("next"));
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
