package com.example.SoundNet.Utils;

import android.util.Log;
import com.example.SoundNet.Algorithm.GoertzelDetect;
import com.example.SoundNet.Config.AudioConfig;
import java.util.ArrayList;

public class Demodulate {
  private static final String TAG = Demodulate.class.getSimpleName();

  // 預定義的FSK頻率表（16個頻率，對應4位元）
  private static final int[] FSK_FREQUENCIES = new int[16];

  static {
    for (int i = 0; i < 16; i++) {
      FSK_FREQUENCIES[i] = AudioConfig.MIN_FREQUENCY + 128 * i;
    }
  }

  public Demodulate() {
    // 可以添加初始化邏輯，例如自訂頻率表
  }

  /**
   * 檢查是否可以解調（數據長度是否足夠）。
   *
   * @param size 數據大小
   * @return 是否可解調
   */
  public static boolean canDemodulate(int size) {
    return size > AudioConfig.MAX_DEMODULATE_SIZE && size >= AudioConfig.NUM_SYMBOL;
  }

  /**
   * 使用Goertzel演算法解調FSK訊號，將數據轉換為字元。
   *
   * @param data 輸入音頻數據
   * @return 解調後的字串
   */
  public String getGoertzelDemodulate(ArrayList<Double> data) {
    if (data == null || data.isEmpty()) {
      Log.w(TAG, "Input data is null or empty");
      return "";
    }

    int numSegments = data.size() / AudioConfig.NUM_SYMBOL;
    if (numSegments <= 0) {
      Log.w(TAG, "Data too short for demodulation");
      return "";
    }

    // 將ArrayList轉為陣列
    double[] inputData = data.stream().mapToDouble(Double::doubleValue).toArray();

    // 計算每個頻率在每個分段的幅度
    double[][] frequencyAmplitudes = computeFrequencyAmplitudes(inputData, numSegments);

    // 解碼每個分段的最大頻率索引
    ArrayList<Integer> decodedSymbols = decodeSymbols(frequencyAmplitudes, numSegments);

    // 將符號轉換為字元
    return symbolsToString(decodedSymbols);
  }

  /** 計算所有頻率的幅度。 */
  private double[][] computeFrequencyAmplitudes(double[] inputData, int numSegments) {
    double[][] amplitudes = new double[FSK_FREQUENCIES.length][numSegments];
    for (int j = 0; j < FSK_FREQUENCIES.length; j++) {
      amplitudes[j] =
          GoertzelDetect.getArrCarrier(inputData, FSK_FREQUENCIES[j], 1, 1, numSegments);
    }
    return amplitudes;
  }

  /** 從幅度陣列中解碼符號。 */
  private ArrayList<Integer> decodeSymbols(double[][] frequencyAmplitudes, int numSegments) {
    ArrayList<Integer> decodedSymbols = new ArrayList<>();
    double[] symbolAmplitudes = new double[FSK_FREQUENCIES.length];

    for (int i = 0; i < numSegments; i++) {
      for (int j = 0; j < FSK_FREQUENCIES.length; j++) {
        symbolAmplitudes[j] = frequencyAmplitudes[j][i];
      }
      int maxIndex = findMaxInDemodulation(symbolAmplitudes, FSK_FREQUENCIES.length);
      if (maxIndex != -1) {
        decodedSymbols.add(maxIndex);
        Log.d(TAG, "Decoded symbol at segment " + i + ": " + maxIndex);
      }
    }
    return decodedSymbols;
  }

  /** 將解碼的符號轉換為字串（每兩個符號組成一個字元）。 */
  private String symbolsToString(ArrayList<Integer> decodedSymbols) {
    StringBuilder decodedString = new StringBuilder();
    for (int i = 0; i < decodedSymbols.size() - 1; i += 2) {
      int highNibble = decodedSymbols.get(i);
      int lowNibble = decodedSymbols.get(i + 1);
      char character = (char) ((highNibble << 4) + lowNibble);
      decodedString.append(character);
      Log.d(TAG, "Decoded character: " + character);
    }
    return decodedString.toString();
  }

  /**
   * 尋找陣列中的最大值索引，若低於閾值則返回-1。
   *
   * @param amplitudes 幅度陣列
   * @param length 陣列長度
   * @return 最大值的索引，或-1（若無效）
   */
  private int findMaxInDemodulation(double[] amplitudes, int length) {
    if (amplitudes == null || length <= 0 || length > amplitudes.length) {
      return -1;
    }

    int maxIndex = 0;
    double maxAmplitude = amplitudes[0];
    for (int i = 1; i < length; i++) {
      if (amplitudes[i] > maxAmplitude) {
        maxAmplitude = amplitudes[i];
        maxIndex = i;
      }
    }

    return (maxAmplitude >= AudioConfig.THRESHOLD) ? maxIndex : -1;
  }
}
