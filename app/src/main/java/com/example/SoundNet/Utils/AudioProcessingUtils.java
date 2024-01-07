package com.example.SoundNet.Utils;

import com.example.SoundNet.Config.AudioConfig;
import com.example.SoundNet.ProcessState;

import java.util.ArrayList;

public class AudioProcessingUtils {
    public static double[] normalization(short[] data) {
        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = (data[i] / 32768.0);
        }

        return result;
    }

    public static boolean checkResultInArray(String result) {
        boolean isCorrect = false;
        if (result != null) {
            for (String s : AudioConfig.TARGET_STRING_ARRAY) {
                if (result.equals(s)) {
                    isCorrect = true;
                    break;
                }
            }
        }

        return isCorrect;
    }

    public static boolean masterTimeout(long tStart, ProcessState state, boolean isSync) {
        long timeSpent = (System.currentTimeMillis() - tStart) / 1000;
        // 狀態為 MASTER 且時間超過 2 秒且沒有同步過
        return (state == ProcessState.MASTER) && (timeSpent > AudioConfig.RECORD_TIMEOUT) && !isSync;
    }

    public static ArrayList<Double> getRemoveOffsetData(ArrayList<Double> rawData) {
        ArrayList<Double> data = new ArrayList<>();
        int offset = 5520;

        for (int i = 0; i < rawData.size() - offset; i++) {
            data.add(rawData.get(offset + i));
        }

        return data;
    }
}
