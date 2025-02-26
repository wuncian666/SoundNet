package com.example.SoundNet;

import android.Manifest;
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
import java.util.Arrays;
import java.util.stream.Collectors;

public class Record {
  private static final String TAG = Record.class.getSimpleName();
  public final byte[] bufferBytes;
  private final Context context;
  private final short[] buffer;
  private final ArrayList<Double> rawData;
  private AudioRecord audioRecord;
  private boolean isRecording, isSync;

  public Record(Context context) {
    this.context = context;
    int bufferSize = AudioConfig.BUFFER_SIZE;
    buffer = new short[bufferSize];
    bufferBytes = new byte[bufferSize * 2];
    rawData = new ArrayList<>(); // 初始化，避免null
  }

  public ArrayList<Double> getRecordData() {
    return new ArrayList<>(rawData); // 返回副本，避免外部修改
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
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {
      Log.e(TAG, "Audio recording permission denied");
      throw new IllegalStateException("Audio recording permission required");
    }

    isRecording = false;
    isSync = false;

    if (audioRecord == null) {
      audioRecord =
          new AudioRecord(
              MediaRecorder.AudioSource.DEFAULT,
              AudioConfig.DEFAULT_SAMPLE_RATE,
              AudioFormat.CHANNEL_IN_MONO,
              AudioFormat.ENCODING_PCM_16BIT,
              AudioConfig.DEFAULT_SAMPLE_RATE);

      if (AutomaticGainControl.isAvailable()) {
        AutomaticGainControl agc = AutomaticGainControl.create(audioRecord.getAudioSessionId());
        agc.setEnabled(false);
      } else {
        Log.w(TAG, "Automatic Gain Control not available");
      }
    }
  }

  public void writeAudioData() {
    if (audioRecord == null || !isRecording) return;

    int read = audioRecord.read(buffer, 0, buffer.length);
    if (read <= 0) {
      Log.w(TAG, "No audio data read: " + read);
      return;
    }

    double[] audioData = AudioProcessingUtils.normalization(buffer);
    if (!isSync) {
      isSync = AudioDetect.checkSync(audioData);
    } else {
      rawData.addAll(Arrays.stream(audioData).boxed().collect(Collectors.toList()));
      ByteBuffer.wrap(bufferBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
    }
  }

  public void start() {
    if (audioRecord == null) {
      Log.e(TAG, "AudioRecord not initialized");
      return;
    }
    isRecording = true;
    audioRecord.startRecording();
    rawData.clear();
    Log.d(TAG, "Recording started");
  }

  public void stop() {
    if (audioRecord != null && isRecording) {
      isRecording = false;
      audioRecord.stop();
      audioRecord.release();
      audioRecord = null;
      Log.d(TAG, "Recording stopped and resources released");
    }
  }
}
