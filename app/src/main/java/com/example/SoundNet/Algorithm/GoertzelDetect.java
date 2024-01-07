package com.example.SoundNet.Algorithm;

import com.example.SoundNet.Config.AudioConfig;

public class GoertzelDetect {
    public static double getAmpCarrier(double[] arrX, double freTarget, int lenX) {

        int k = (int) (0.5 + lenX * freTarget / AudioConfig.DEFAULT_SAMPLE_RATE);
        double w = 2 * Math.PI * k / lenX;
        double cosine = Math.cos(w);
        double coe = 2 * cosine;
        double Q0;
        double Q1 = 0;
        double Q2 = 0;

        for (int i = 0; i < lenX; i++) {
            Q0 = coe * Q1 - Q2 + arrX[i];
            Q2 = Q1;
            Q1 = Q0;
        }

        return Math.sqrt(Q1 * Q1 + Q2 * Q2 - coe * Q1 * Q2);
    }

    public static double[] getArrCarrier(double[] rawData, double freTarget, int facWindow, int facShift, int part) {
        int nWindow = AudioConfig.NUM_SYMBOL / facWindow;
        int nShift = AudioConfig.NUM_SYMBOL / facShift;

        double[] temp = new double[nWindow];

        double[] result = new double[part];

        for (int i = 0; i < part; i++) {
            if (rawData.length - i * nShift >= nWindow) {
                System.arraycopy(rawData, i * nShift, temp, 0, nWindow);
                result[i] = getAmpCarrier(temp, freTarget, nWindow);
            }
        }
        return result;
    }
}
