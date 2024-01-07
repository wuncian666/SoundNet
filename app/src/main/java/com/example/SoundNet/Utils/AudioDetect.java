package com.example.SoundNet.Utils;

import com.example.SoundNet.Algorithm.GoertzelDetect;
import com.example.SoundNet.Config.AudioConfig;

public class AudioDetect {

    public static boolean checkSync(double[] data) {
        boolean isSync = false;

        int frameLen = 240;// buffer長度4800，分20等份，每一等份容量240
        double[] temp = new double[frameLen];

        int detectFrame = 20;
        double[] ampTotal = new double[detectFrame];

        for (int i = 0; i < detectFrame; i++) {
            if (data.length - i * frameLen >= frameLen) {
                System.arraycopy(data, i * frameLen, temp, 0, frameLen);

                double power = GoertzelDetect.getAmpCarrier(temp, AudioConfig.TARGET_FREQUENCY, frameLen);
                ampTotal[i] = power;

                if (isSync) {
                    // 移動資料
                    //moveData(temp, frameLen);
                } else {
                    if (i >= 2) {
                        if (calculateAveragePower(ampTotal, i) > AudioConfig.THRESHOLD) {
                            isSync = true;
                            //moveData(temp, frameLen);
                        }
                    }
                }
            }
        }

        return isSync;
    }

    private static double calculateAveragePower(double[] data, int index) {
        return (data[index] + data[index - 1] + data[index - 2]) / 3;
    }
}
