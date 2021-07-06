package com.example.SoundNet;

public class GoertzelDetect {
    public static double getAmpCarrier(double[] arrX, double freTarget, int lenX) {

        // find sync or end: len x = 240
        // int k = 0.5 + 240 * 20048 / 48000 = 100.74
        int k = (int) (0.5 + lenX * freTarget / Common.DEFAULT_SAMPLE_RATE);
        // find sync or end: 2 * pi * 100.74 / 240 = 2.637364805
        double w = 2 * Math.PI * k / lenX;
        // 0.99894077......
        double cosine = Math.cos(w);
        // 1.99788154
        double coe = 2 * cosine;

        double Q0;
        double Q1 = 0;
        double Q2 = 0;

        for (int i = 0; i < lenX; i++) {// 240
            // 1.99788154 * 0 - 0 + arrX[0]
            Q0 = coe * Q1 - Q2 + arrX[i];
            // Q2 = 0
            Q2 = Q1;
            // Q1 = arrX[0]
            Q1 = Q0;
        }

        return Math.sqrt(Q1 * Q1 + Q2 * Q2 - coe * Q1 * Q2);
    }


    // fac window = 1; fac shift = 1;
    public static double[] getArrCarrier(double[] rawData, double freTarget, int facWindow, int facShift, int part) {
        int nWindow = Common.NUM_SYMBOL / facWindow;
        int nShift = Common.NUM_SYMBOL / facShift;

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
