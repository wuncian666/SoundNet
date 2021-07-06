package com.example.SoundNet;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class Demodulate {
    private static final String TAG = "Demodulate";

    int SYNC_FREQUENCY, END_FREQUENCY, MIN_FREQUENCY;

    public Demodulate(int SYNC_FREQUENCY, int END_FREQUENCY, int MIN_FREQUENCY) {
        this.SYNC_FREQUENCY = SYNC_FREQUENCY;
        this.END_FREQUENCY = END_FREQUENCY;
        this.MIN_FREQUENCY = MIN_FREQUENCY;

        Log.i(TAG, "Demodulate: new class demodulate");
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
    public String getGoertzelDemodulate(ArrayList<Double> data, int freMin) {
        ArrayList<Integer> decode = new ArrayList<>();
        // 一個symbol符號6000，總共有多少symbol
        int part = data.size() / Common.NUM_SYMBOL;

        double[] temp = new double[data.size()];

        for (int i = 0; i < data.size(); i++) {
            temp[i] = data.get(i);
        }

        // 16 * 總錄音長度 / 6000 筆資料
        double[][] s = new double[16][part];

        // FSK頻率16進制
        int[] fsk = new int[16];
        for (int i = 0; i < 16; i++) {
            fsk[i] = freMin + 128 * i;
        }

        for (int j = 0; j < 16; j++) {
            // 取得goertzel 振幅
            s[j] = GoertzelDetect.getArrCarrier(temp, (fsk[j]), 1, 1, part);
//            Log.i(TAG, "getGoertzelDemodulate: " + "s[" + j + "] = " + Arrays.toString(s[j]));
        }

        double[] symbolAmp = new double[16];

        // 尋找每個symbol 0,1 第一個symbol
        for (int i = 0; i < part; i++) {
            // 每個symbol的fsk振幅
            for (int j = 0; j < 16; j++) {
                symbolAmp[j] = s[j][i];
                Log.i(TAG, "getGoertzelDemodulate: symbol Amp[" + j + "] = " + s[j][i]);
            }

            // 尋找最大值
            int maxIndex = findMaxInDemodulation(symbolAmp, 16);

            if (maxIndex != -1) {
                // 每個頻率所對應最大值存入decode

                decode.add(maxIndex);
                Log.i(TAG, "getGoertzelDemodulate: arrDecode[" + i + "]= " + maxIndex);
            } else {
//                break;
                // 沒有資料
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
            // 計算每個頻率的最大振幅
            // arr[i] = s[j][ii]
            if (arr[i] > max) {
                max = arr[i];
                index = i;
            }
        }
        // 最大值小於門檻
        if (max < Common.THRESHOLD) index = -1;
        return index;
    }
}