package com.example.SoundNet.Utils;

import android.util.Log;

import com.example.SoundNet.Algorithm.GoertzelDetect;
import com.example.SoundNet.Config.AudioConfig;

import java.util.ArrayList;

public class Demodulate {
    private final String TAG = this.getClass().getSimpleName();

    public Demodulate() {
    }

    public static boolean canDemodulate(int size) {
        return size > AudioConfig.MAX_DEMODULATE_SIZE;
    }

    /**取得sync頻率位置，arrRecordTemp: 錄音所有值
     * 重複find sync 資料損失*/
    // fac window = 1, fac shift = 10
    /*public int getSyncPosition (ArrayList<Double> data, int facWindow, int facShift) {
        // 將一個SYMBOL(6000)分為10個訊框，一訊框有600筆資料
        int part = Common.NUM_SYMBOL / (Common.NUM_SYMBOL / 10);

        int dataLen = data.size();
        double[] temp = new double[dataLen];
        for (int i = 0; i < dataLen; i++)
            temp[i] = data.get(i);

        // 對SYNC(最前面的6000筆資料)分為 10等分做 goertzel運算
        double[] arrDetVal = GoertzelDetect.getArrCarrier// 1, 10, 10
                (temp, SYNC_FREQUENCY, facWindow, facShift, part);
        Log.i(TAG, "getSyncPosition: " + Arrays.toString(arrDetVal));
        // 尋找最大的一筆
        int maxAmpSync = findMaxInDemodulation(arrDetVal, part);
        // 最大的那筆乘上600得到Sync位置
        return maxAmpSync * (Common.NUM_SYMBOL / 10);
    }*/

    /**
     * 將recSignal陣列的值做goertzel演算法計算，return 解調字串
     */
    public String getGoertzelDemodulate(ArrayList<Double> data) {
        ArrayList<Integer> decode = new ArrayList<>();
        int part = data.size() / AudioConfig.NUM_SYMBOL;

        double[] temp = new double[data.size()];

        for (int i = 0; i < data.size(); i++) {
            temp[i] = data.get(i);
        }

        double[][] s = new double[16][part];

        int[] fsk = new int[16];
        for (int i = 0; i < 16; i++) {
            fsk[i] = AudioConfig.MIN_FREQUENCY + 128 * i;
        }

        for (int j = 0; j < 16; j++) {
            s[j] = GoertzelDetect.getArrCarrier(temp, (fsk[j]), 1, 1, part);
        }

        double[] symbolAmp = new double[16];

        for (int i = 0; i < part; i++) {
            for (int j = 0; j < 16; j++) {
                symbolAmp[j] = s[j][i];
                Log.i(TAG, "getGoertzelDemodulate: symbol Amp[" + j + "] = " + s[j][i]);
            }

            int maxIndex = findMaxInDemodulation(symbolAmp, 16);

            if (maxIndex != -1) {
                decode.add(maxIndex);
                Log.i(TAG, "getGoertzelDemodulate: arrDecode[" + i + "]= " + maxIndex);
            }
        }

        StringBuilder strDecode = new StringBuilder();

        for (int i = 0; i < part; i = i + 2) {

            if (decode.size() - i < 2) {
                break;
            } else {
                char val = (char) ((decode.get(i) << 4) + (decode.get(i + 1)));
                Log.i(TAG, "getGoertzelDemodulate: val" + val);
                strDecode.append(val);
            }
        }

        Log.i(TAG, "getGoertzelDemodulate: part: " + part);
        return strDecode.toString();
    }

    public int findMaxInDemodulation(double[] arr, int length) {
        int index = 0;
        double max = 0;
        for (int i = 0; i < length; i++) {
            if (arr[i] > max) {
                max = arr[i];
                index = i;
            }
        }
        if (max < AudioConfig.THRESHOLD) {
            index = -1;
        }
        return index;
    }
}
