package com.example.SoundNet.MusicControl;

import android.media.MediaPlayer;
import android.os.Binder;

public class MusicBinder extends Binder {
    private MediaPlayer mediaPlayer;

    public MusicBinder(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    /**
     * 是否播放中
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * 暫停
     */
    public void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /**
     * 播放
     */
    public void playMusic() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * 取得歌曲總長度
     */
    public int getProgress() {
        return mediaPlayer.getDuration();
    }

    /**
     * 將播到的位置傳出
     */
    public int getPlayPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 關閉播放器
     */
    public void closeMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
