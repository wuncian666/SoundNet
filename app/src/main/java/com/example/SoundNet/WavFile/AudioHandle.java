package com.example.SoundNet.WavFile;

import android.os.Environment;
import android.util.Log;

import com.example.SoundNet.WavFile.WavFile;

import java.io.File;
import java.util.ArrayList;

public class AudioHandle {
    private final String TAG = this.getClass().getSimpleName();
    private WavFile wavfile;

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
        ArrayList<Double> modulated = new ArrayList<>();
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
