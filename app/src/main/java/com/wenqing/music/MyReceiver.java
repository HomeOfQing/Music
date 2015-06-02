package com.wenqing.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.wenqing.music.Event.ChangeMusicEvent;
import com.wenqing.music.Event.NextMusicEvent;
import com.wenqing.music.Event.PlayMusicEvent;

import de.greenrobot.event.EventBus;

public class MyReceiver extends BroadcastReceiver {


    public MyReceiver() {
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int requestCode = intent.getIntExtra("REQCODE", -1);
        switch (requestCode){
            case MusicService.REQ_CANCEL:

                break;
            case MusicService.REQ_NEXT:
                EventBus.getDefault().post(new NextMusicEvent("next"));
                break;
            case MusicService.REQ_PRE:
                EventBus.getDefault().post(new NextMusicEvent("pre"));
                break;
            case MusicService.REQ_PLAY:
                Music music = (Music) intent.getSerializableExtra("music");
                EventBus.getDefault().post(new PlayMusicEvent(music, "play"));
                break;
        }
    }


}
