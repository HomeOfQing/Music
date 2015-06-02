package com.wenqing.music;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by DELL on 2015/4/24.
 */
public class MusicAdapter extends BaseAdapter implements MainActivity.ShowPlayState {
    private List<Music> data;
    private LayoutInflater inflater;
    private int position;

    public MusicAdapter(Context context, List<Music> musicList) {
        this.data = musicList;
        inflater = LayoutInflater.from(context);
        MainActivity activity = (MainActivity) context;
        position = activity.getPosition();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Music getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.music_item, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.textView_title);
            holder.displayName = (TextView) convertView.findViewById(R.id.textView_display_name);
            holder.duration = (TextView) convertView.findViewById(R.id.duration);
            holder.isPlaying = (ImageView) convertView.findViewById(R.id.imageView_is_playing);
            holder.count = (TextView) convertView.findViewById(R.id.textView_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Music music = data.get(position);
        holder.displayName.setText(music.getDisplayName());
        holder.title.setText(music.getTitle().substring(0,music.getTitle().lastIndexOf("-"))+" - "+music.getAlbum());
        holder.duration.setText(music.getFormatDuration());
        holder.count.setText(String.valueOf(position+1));
//        Log.d("MainActivity",String.valueOf(this.position));
        if (this.position == position) {
            holder.isPlaying.setVisibility(View.VISIBLE);
            holder.count.setVisibility(View.GONE);
        } else {
            holder.isPlaying.setVisibility(View.GONE);
            holder.count.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    @Override
    public void showPlay(int position) {
        this.position = position;
    }

    static class ViewHolder {
        TextView title;
        TextView displayName;
        TextView duration;
        ImageView isPlaying;
        TextView count;
    }
}
