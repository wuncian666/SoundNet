package com.example.SoundNet.MusicControl;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import com.example.SoundNet.GoertzelDetect;
import com.example.SoundNet.MainActivity;
import com.example.SoundNet.R;

public class SoundManager {
    private final String TAG = this.getClass().getSimpleName();

    private final SoundPool mSoundPool;

    private final Context context;

    private static final int SOUND_COUNT = 3;

    public SoundManager(Context context) {
        this.context = context;
        mSoundPool = new SoundPool.Builder().setMaxStreams(SOUND_COUNT).build();
    }

    public void playSound(int raw, int loop) {
        int sound = mSoundPool.load(context, raw, 1);
        mSoundPool.play(sound, 1, 1, 1, loop, 1f);
    }
}
