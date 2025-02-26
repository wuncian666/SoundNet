package com.example.SoundNet.Utils;

import com.example.SoundNet.Config.AudioConfig;
import com.example.SoundNet.ProcessState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AudioProcessingUtils {
  // PCM 16-bit最大值，用於正規化
  private static final double PCM_16BIT_MAX = 32768.0;
  private static final long MILLIS_TO_SECONDS = 1000L;
  // 將目標字串陣列轉為Set，提升查找效率
  private static final Set<String> TARGET_STRINGS =
      AudioConfig.TARGET_STRING_ARRAY != null
          ? new HashSet<>(Arrays.asList(AudioConfig.TARGET_STRING_ARRAY))
          : Collections.emptySet();
  // 偏移量常量，假設5520是同步頭長度，需根據實際場景定義
  private static final int SYNC_OFFSET = 5520;

  /**
   * 將short型音頻數據正規化為[-1, 1]區間。
   *
   * @param data 輸入的short型音頻數據
   * @return 正規化後的double陣列
   */
  public static double[] normalization(short[] data) {
    if (data == null || data.length == 0) {
      throw new IllegalArgumentException("Input data cannot be null or empty");
    }

    double[] result = new double[data.length];
    Arrays.setAll(result, i -> data[i] / PCM_16BIT_MAX);
    return result;
  }

  /**
   * 檢查解調結果是否在目標字串集合中。
   *
   * @param result 解調得到的字串
   * @return 是否匹配目標字串
   */
  public static boolean checkResultInArray(String result) {
    if (result == null || TARGET_STRINGS.isEmpty()) {
      return false;
    }
    return TARGET_STRINGS.contains(result);
  }

  /**
   * 判斷Master模式是否超時。
   *
   * @param startTime 開始時間（毫秒）
   * @param state 當前處理狀態
   * @param isSync 是否已同步
   * @return 是否超時
   */
  public static boolean masterTimeout(long startTime, ProcessState state, boolean isSync) {
    if (state != ProcessState.MASTER) {
      return false;
    }
    long timeSpentSeconds = (System.currentTimeMillis() - startTime) / MILLIS_TO_SECONDS;
    return timeSpentSeconds > AudioConfig.RECORD_TIMEOUT && !isSync;
  }

  /**
   * 移除音頻數據中的偏移部分（例如同步頭）。
   *
   * @param rawData 原始音頻數據
   * @return 移除偏移後的數據
   */
  public static ArrayList<Double> getRemoveOffsetData(ArrayList<Double> rawData) {
    if (rawData == null || rawData.size() <= SYNC_OFFSET) {
      return new ArrayList<>(); // 返回空列表，避免異常
    }

    // 使用subList避免逐元素複製
    return new ArrayList<>(rawData.subList(SYNC_OFFSET, rawData.size()));
  }
}
