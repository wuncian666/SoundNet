package com.example.SoundNet.Utils;

import android.util.Log;
import com.example.SoundNet.Algorithm.GoertzelDetect;
import com.example.SoundNet.Config.AudioConfig;

public class AudioDetect {
  private static final int FRAME_LENGTH = AudioConfig.DEFAULT_SAMPLE_RATE;
  private static final int DETECT_FRAME = AudioConfig.DETECT_FRAME;

  public static boolean checkSync(double[] data) {
    if (data.length < FRAME_LENGTH * DETECT_FRAME) {
      return false;
    }

    double[] ampTotal = new double[DETECT_FRAME];
    double[] frameData = new double[FRAME_LENGTH];

    for (int i = 0; i < DETECT_FRAME; i++) {
      System.arraycopy(data, i * FRAME_LENGTH, frameData, 0, FRAME_LENGTH);
      ampTotal[i] =
          GoertzelDetect.getAmpCarrier(frameData, AudioConfig.TARGET_FREQUENCY, FRAME_LENGTH);

      if (i >= 2) {
        double avgPower = calculateAveragePower(ampTotal, i);
        if (avgPower > AudioConfig.THRESHOLD) {
          Log.d("AudioDetect", "Sync detected with avg power: " + avgPower);
          return true;
        }
      }
    }
    return false;
  }

  private static double calculateAveragePower(double[] data, int index) {
    return (data[index] + data[index - 1] + data[index - 2]) / 3.0;
  }
}
