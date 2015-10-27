package com.quickblox.sample.videochatwebrtcnew.util;

import android.content.Context;
import android.media.MediaPlayer;

import com.quickblox.sample.videochatwebrtcnew.R;

/**
 * Created by vadim on 10/27/15.
 */
public class RingtonePlayer {


    private MediaPlayer mediaPlayer;
    private Context context;

    public RingtonePlayer(Context context){
        this.context = context;
    }

    public void startOutBeep() {
        mediaPlayer = android.media.MediaPlayer.create(context, R.raw.beep);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

    }

    public synchronized void stopOutBeep() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
