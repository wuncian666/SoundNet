package com.example.SoundNet;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AudioHandle {
    // audio handle 負責讀取wav檔

    private final static String TAG = "AudioHandle";
    private WavFile wavfile;
    private ArrayList<Double> modulated;

    public AudioHandle(String filename) {
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            this.wavfile = WavFile.openWavFile(new File(root, "FSK_AUDIO/" + filename));
            wavfile.display();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Double> read() {
        modulated = new ArrayList<>();
        double[] buffer = new double[100];
        int framesRead;
        try {
            do {
                framesRead = wavfile.readFrames(buffer, 100);
                for (int s = 0; s < framesRead; s++) {
                    Double temp = (Double) buffer[s];
                    modulated.add(temp);
//                    Log.i(TAG, "read: temp: " + temp);
                }
                Log.i(TAG, "read: ");
            } while (framesRead != 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return modulated;
        }
    }

    public void close() {
        try {
            wavfile.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Audio_FSK", e.toString());
        }
    }

}
