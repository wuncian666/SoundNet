package com.example.SoundNet.AudioPlayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.SoundNet.R;

import java.io.IOException;

public class PlayMusic implements Runnable {
  private static final String TAG = "PlayMusic";

  public boolean stopPlay = false;
  public int playMusicTimes = 0;
  MediaPlayer mMediaPlayer;
  Context context;

  public PlayMusic(Context context) {
    this.context = context;
    mMediaPlayer = new MediaPlayer();
    mMediaPlayer = MediaPlayer.create(context, R.raw.iotlab_1time_have_end_20560);
  }

  public void play() {
    if (!mMediaPlayer.isPlaying()) {
      Log.i(TAG, "play: ");

      mMediaPlayer.start();

      mMediaPlayer.setOnCompletionListener(
          new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
              playMusicTimes++;
              //                    mMediaPlayer.release();
              Log.i(TAG, "onCompletion: " + playMusicTimes);
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
}
