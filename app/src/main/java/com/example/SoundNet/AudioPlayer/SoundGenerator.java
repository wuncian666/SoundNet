package com.example.SoundNet.AudioPlayer;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import com.example.SoundNet.Config.AudioConfig;
import com.example.SoundNet.WavFile.WavFileHandle;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SoundGenerator {
  private static final String TAG = SoundGenerator.class.getSimpleName();

  // 常量，從AudioConfig提取
  private static final double DURATION = 0.125; // 每個符號的持續時間（秒）
  private static final int SAMPLE_RATE = AudioConfig.DEFAULT_SAMPLE_RATE;
  private static final double SAMPLING_PERIOD = 1.0 / SAMPLE_RATE;
  private static final double SYNC_FREQUENCY = 20048.0; // 同步訊號頻率（Hz）
  private static final int[] FSK_FREQUENCIES = new int[16]; // FSK頻率表

  static {
    for (int i = 0; i < 16; i++) {
      FSK_FREQUENCIES[i] = AudioConfig.MIN_FREQUENCY + 128 * i;
    }
  }

  private final String message;
  private final Context context;
  private final WavFileHandle wavFileHandle;
  private final int numSamples; // 每個符號的樣本數
  private final byte[] sound; // 最終音頻數據

  // 重用緩衝區
  private final short[] buffer;
  private final byte[] tempByte;

  public SoundGenerator(String message, Context context) {
    if (message == null || message.isEmpty()) {
      throw new IllegalArgumentException("Message cannot be null or empty");
    }
    if (context == null) {
      throw new IllegalArgumentException("Context cannot be null");
    }

    this.message = message;
    this.context = context;
    this.wavFileHandle = new WavFileHandle();
    this.numSamples = (int) (DURATION * SAMPLE_RATE); // 6000
    // 總符號數（同步+消息）
    int totalSymbol = message.length() * 2 + 1; // 1個同步 + 每個字元2個符號
    this.sound = new byte[2 * totalSymbol * numSamples + 4088]; // 音頻數據總長度
    this.buffer = new short[numSamples];
    this.tempByte = new byte[numSamples * 2];
  }

  /** 生成並保存FSK編碼的音頻訊號。 */
  public void generateSound() throws IOException {
    String rawFile =
        wavFileHandle.getRawFilename(context, AudioConfig.AUDIO_GENERATOR_FILENAME_RAW);
    String wavFile =
        wavFileHandle.getFolderName(context) + AudioConfig.AUDIO_GENERATOR_FILENAME_WAV;

    try (FileOutputStream fileOutputStream = new FileOutputStream(rawFile)) {
      // 生成同步訊號
      generateSyncSignal();
      System.arraycopy(tempByte, 0, sound, 0, numSamples * 2);

      // 生成FSK訊號
      int[] encodedSymbols = encode();
      generateFskSignals(encodedSymbols);

      // 寫入檔案
      fileOutputStream.write(sound);
      Log.i(TAG, "Sound generated, length: " + sound.length);
    }

    wavFileHandle.copyWaveFile(rawFile, wavFile);
  }

  /** 生成同步訊號。 */
  private void generateSyncSignal() {
    for (int i = 0; i < numSamples; i++) {
      double signal = Math.cos(2 * Math.PI * i * SYNC_FREQUENCY * SAMPLING_PERIOD);
      buffer[i] = (short) (signal * 32767.0);
    }
    applyHanningWindow(buffer);
    ByteBuffer.wrap(tempByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
  }

  /** 生成FSK編碼訊號。 */
  private void generateFskSignals(int[] encodedSymbols) {
    for (int j = 0; j < encodedSymbols.length; j++) {
      int frequency = FSK_FREQUENCIES[encodedSymbols[j]];
      for (int i = 0; i < numSamples; i++) {
        double signal = Math.cos(2 * Math.PI * i * frequency * SAMPLING_PERIOD);
        buffer[i] = (short) (signal * 32767.0);
      }
      applyHanningWindow(buffer);
      ByteBuffer.wrap(tempByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
      System.arraycopy(tempByte, 0, sound, (j + 1) * 2 * numSamples, numSamples * 2);
    }
  }

  /** 應用Hanning窗減少頻譜洩漏。 */
  private void applyHanningWindow(short[] signal) {
    for (int i = 0; i < numSamples; i++) {
      double window = 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / numSamples));
      signal[i] = (short) (signal[i] * window);
    }
  }

  /** 將輸入字串編碼為FSK符號序列。 */
  private int[] encode() {
    char[] chars = message.toCharArray();
    int[] encoded = new int[chars.length * 2];

    for (int i = 0; i < chars.length; i++) {
      int j = i * 2;
      byte b = (byte) chars[i];
      encoded[j] = (b & 0xf0) >> 4; // 高4位
      encoded[j + 1] = b & 0x0f; // 低4位
      Log.d(TAG, "Encoded char " + chars[i] + " to " + encoded[j] + ", " + encoded[j + 1]);
    }
    return encoded;
  }

  /** 播放生成的音頻訊號。 */
  public void playSound() {
    AudioTrack audioTrack =
        new AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            sound.length,
            AudioTrack.MODE_STATIC);

    int result = audioTrack.write(sound, 0, sound.length);
    if (result < 0) {
      Log.e(TAG, "Failed to write audio data: " + result);
      return;
    }

    audioTrack.setVolume(AudioTrack.getMaxVolume());
    audioTrack.play();
    Log.i(TAG, "Playing sound, length: " + sound.length);
  }
}
