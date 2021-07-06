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
    private static final String TAG = "SoundManager";

    private final SoundPool mSoundPool;

    public static final int SOUND_COUNT = 3;

    private final int[] mSoundId;

    public SoundManager(Context context) {

        mSoundPool = new SoundPool.Builder().setMaxStreams(SOUND_COUNT).build();

        mSoundId = new int[SOUND_COUNT];

        mSoundId[0] = mSoundPool.load(context, R.raw.ack_1time_have_end_20560, 1);

        mSoundId[1] = mSoundPool.load(context, R.raw.hello_1time_have_end_20560, 1);

        mSoundId[2] = mSoundPool.load(context, R.raw.iotlab_1time_have_end_20560, 1);
    }

    public void playSound(int sound, int loop) {
        Log.i(TAG, "playSound: " + sound);
        mSoundPool.play(mSoundId[sound], 1, 1, 1, loop, 1f);
    }
}
