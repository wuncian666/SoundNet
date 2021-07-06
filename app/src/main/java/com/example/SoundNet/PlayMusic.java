package com.example.SoundNet;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class PlayMusic implements Runnable {
    private static final String TAG = "PlayMusic";

    public boolean stopPlay = false;

    MediaPlayer mMediaPlayer;

    Context context;

    public int playMusicTimes = 0;

    public PlayMusic(Context context) {
        this.context = context;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(context, R.raw.iotlab_1time_have_end_20560);
    }


    public void play() {
        if (!mMediaPlayer.isPlaying()) {
            Log.i(TAG, "play: ");

            mMediaPlayer.start();

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playMusicTimes++;
//                    mMediaPlayer.release();
                    Log.i(TAG, "onCompletion: " + playMusicTimes);
                    updateUi(context);
                    // 播放

                    if (playMusicTimes > 300) {
                        stopPlay = true;
                    }
                }
            });
        }
    }


    @Override
    public void run() {
        while (!stopPlay) {
            // 播放音樂
            play();

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateUi(Context context) {
        // 主線程更新UI
        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) context).textPlayMusicTimes.setText(String.valueOf(playMusicTimes));
            }
        });
    }
}
