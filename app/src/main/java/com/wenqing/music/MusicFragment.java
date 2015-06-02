package com.wenqing.music;


import android.app.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.wenqing.music.Event.ChangeMusicEvent;
import com.wenqing.music.Event.MusicListEvent;

import java.util.Date;
import java.util.HashMap;

import de.greenrobot.event.EventBus;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment {
    private static final String TAG = "MusicFragment";
    private Music music;
    private ImageView imageView;
    private boolean isPlaying;
    private Context context;
    private RotateAnimation animation;
    private HashMap<Long, Bitmap> albums;
    public MusicFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Music music){
        MusicFragment fragment = new MusicFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("music", music);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.music = (Music) getArguments().get("music");
//        Log.d("MusicFragment","music"+music.toString());
        EventBus.getDefault().registerSticky(this);
        MusicListEvent event = EventBus.getDefault().getStickyEvent(MusicListEvent.class);
        albums = event.getAlbums();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        imageView = (ImageView) view.findViewById(R.id.imageView_fragment);
        loadImage();
        return view;
    }

    private void loadImage() {
        Log.d(TAG, albums.toString());
        imageView.setImageBitmap(albums.get(music.getId()));
        setAnimation();
    }

    private void setAnimation() {
        animation = new RotateAnimation(0f,360f, Animation.RELATIVE_TO_SELF,
                0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        animation.setFillAfter(true);
        animation.setDuration(36000);
        animation.setRepeatCount(100);
        animation.setRepeatMode(Animation.RESTART);
        imageView.setAnimation(animation);
        if(isPlaying){
            animation.startNow();
            imageView.postInvalidateOnAnimation();
        }
        else{
            animation.cancel();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(ChangeMusicEvent event){
//        music = event.getMusic();
        isPlaying = event.isPlaying();
//        Log.d("MusicFragment","onEvent"+music.toString());
        loadImage();
    }


}
