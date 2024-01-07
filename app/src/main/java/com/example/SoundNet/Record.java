package com.example.SoundNet;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.SoundNet.Config.AudioConfig;
import com.example.SoundNet.Utils.AudioDetect;
import com.example.SoundNet.Utils.AudioProcessingUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Record {
    private final String TAG = this.getClass().getSimpleName();

    private AudioRecord audioRecord = null;

    private final Context context;

    private final short[] buffer;

    public final byte[] bufferBytes;

    private ArrayList<Double> rawData;

    private boolean isRecording, isSync;

    public Record(Context context) {
        this.context = context;

        int bufferSize = AudioConfig.BUFFER_SIZE;
        buffer = new short[bufferSize];
        bufferBytes = new byte[bufferSize * 2];
    }

    public ArrayList<Double> getRecordData() {
        return rawData;
    }

    public int getRecordSize() {
        return rawData.size();
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isSync() {
        return isSync;
    }

    public void create() throws IllegalStateException {
        isRecording = false;
        isSync = false;

        if (audioRecord == null) {
            if (ActivityCompat.
                    checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT,
                    AudioConfig.DEFAULT_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioConfig.DEFAULT_SAMPLE_RATE);
        }

        if (AutomaticGainControl.isAvailable()) {
            AutomaticGainControl automaticGain = AutomaticGainControl.create(audioRecord.getAudioSessionId());
            automaticGain.setEnabled(false);
        } else {
            Log.e(TAG, "create: AUTOMATIC GAIN CONTROL NOT AVAILABLE");
        }
    }

    public void writeAudioData() {
        audioRecord.read(buffer, 0, buffer.length);

        double[] audioData = AudioProcessingUtils.normalization(buffer);

        if (!isSync) {
            isSync = AudioDetect.checkSync(audioData);
        } else {
            for (double i : audioData) {
                rawData.add(i);
            }

            ByteBuffer.wrap(bufferBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
        }
    }

    public void start() {
        isRecording = true;
        audioRecord.startRecording();

        if (rawData == null) {
            rawData = new ArrayList<>();
        } else {
            rawData.clear();
        }
    }

    public void stop() {
        if (null != audioRecord) {
            isRecording = false;

            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        }
    }
}
