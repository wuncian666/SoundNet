package com.example.SoundNet.MusicControl;

import android.content.Context;
import android.media.SoundPool;

import com.example.SoundNet.R;

public class SoundManager {
    private final String TAG = this.getClass().getSimpleName();

    private final SoundPool soundPool;

    private final Context context;

    public SoundManager(Context context) {
        this.context = context;
        int soundCount = 3;
        soundPool = new SoundPool.Builder().setMaxStreams(soundCount).build();
    }

    public void playSound(int raw, int loop) {
        int sound = soundPool.load(context, raw, 1);
        soundPool.play(sound, 1, 1, 1, loop, 1f);
    }

    public void playAckSound() {
        this.playSound(R.raw.ack_1time_have_end_20560, 0);
    }

    public void playHelloSound() {
        this.playSound(R.raw.hello_1time_have_end_20560, 0);
    }
}
