package com.example.SoundNet.Algorithm;

import com.example.SoundNet.Config.AudioConfig;

import java.util.Arrays;

public class GoertzelDetect {
  /**
   * 計算指定頻率的幅度，使用Goertzel演算法。
   *
   * @param inputData 輸入音頻數據
   * @param targetFrequency 目標頻率（Hz）
   * @param dataLength 數據長度
   * @return 指定頻率的幅度
   */
  public static double getAmpCarrier(double[] inputData, double targetFrequency, int dataLength) {
    if (inputData == null || inputData.length < dataLength || dataLength <= 0) {
      throw new IllegalArgumentException("Invalid input data or length");
    }

    // 提前計算常數
    int k = (int) (0.5 + dataLength * targetFrequency / AudioConfig.DEFAULT_SAMPLE_RATE);
    double w = 2 * Math.PI * k / dataLength;
    double cosine = Math.cos(w);
    double coefficient = 2 * cosine;

    double current;
    double prev1 = 0;
    double prev2 = 0;

    for (int i = 0; i < dataLength; i++) {
      current = coefficient * prev1 - prev2 + inputData[i];
      prev2 = prev1;
      prev1 = current;
    }

    // 返回幅度（可選：若只需比較，可返回平方和）
    double magnitudeSquared = prev1 * prev1 + prev2 * prev2 - coefficient * prev1 * prev2;
    return Math.sqrt(magnitudeSquared);
  }

  /**
   * 對音頻數據分段計算目標頻率的幅度陣列。
   *
   * @param rawData 原始音頻數據
   * @param targetFrequency 目標頻率（Hz）
   * @param windowSize 每個窗口的大小
   * @param shiftSize 窗口間的滑動步長
   * @param numSegments 要計算的分段數
   * @return 每個窗口的幅度陣列
   */
  public static double[] getArrCarrier(
      double[] rawData, double targetFrequency, int windowSize, int shiftSize, int numSegments) {
    if (rawData == null || windowSize <= 0 || shiftSize <= 0 || numSegments <= 0) {
      throw new IllegalArgumentException(
          "Invalid parameters: rawData, windowSize, shiftSize, or numSegments");
    }

    int requiredLength = (numSegments - 1) * shiftSize + windowSize;
    if (rawData.length < requiredLength) {
      throw new IllegalArgumentException(
          "Raw data length ("
              + rawData.length
              + ") is insufficient for "
              + numSegments
              + " segments");
    }

    double[] result = new double[numSegments];

    for (int i = 0; i < numSegments; i++) {
      int startIndex = i * shiftSize;
      // 直接在rawData上操作，避免額外分配內存
      result[i] =
          getAmpCarrier(
              Arrays.copyOfRange(rawData, startIndex, startIndex + windowSize),
              targetFrequency,
              windowSize);
    }

    return result;
  }
}
