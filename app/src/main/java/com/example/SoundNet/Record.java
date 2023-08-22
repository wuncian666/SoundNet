package com.example.SoundNet;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;

import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Record {
    private final String TAG = this.getClass().getSimpleName();

    private AudioRecord audioRecord = null;

    private final Context context;

    private final short[] buffer;

    public final byte[] bufferBytes;

    private ArrayList<Double> rawData;

    public boolean isRecording, isSync, isEnd;

    private final Frequency frequency;

    public Record(Context context, Frequency frequency) {
        this.context = context;
        this.frequency = frequency;

        int bufferSize = 4800;
        buffer = new short[bufferSize];
        bufferBytes = new byte[bufferSize * 2];
    }

    public void onRecord() throws IllegalStateException {
        isRecording = false;
        isSync = false;
        isEnd = false;

        if (audioRecord == null) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            int RECORD_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
            int RECORD_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT,
                    Common.DEFAULT_SAMPLE_RATE, RECORD_CHANNELS, RECORD_AUDIO_ENCODING,
                    Common.DEFAULT_SAMPLE_RATE);
        }

        if (AutomaticGainControl.isAvailable()) {
            AutomaticGainControl automaticGain = AutomaticGainControl.create(audioRecord.getAudioSessionId());
            automaticGain.setEnabled(false);
        } else {
            System.out.println("AUTOMATIC GAIN CONTROL NOT AVAILABLE");
        }
    }

    public void writeAudioData() {
        audioRecord.read(buffer, 0, buffer.length);

        if (AudioRecord.ERROR_INVALID_OPERATION != 0) {
            double[] audioData = normalization(buffer);
            if (!isSync) {
                isSync = checkSync(audioData, frequency.getFreSync());
            } else {
                for (double i : audioData) {
                    rawData.add(i);
                }
                ByteBuffer.wrap(bufferBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
            }
        }
    }

    private double[] normalization(short[] data) {
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (data[i] / 32768.0);
        }
        return result;
    }

    private boolean checkSync(double[] data, double frequency) {
        boolean isSync = false;

        int detectFrame = 20;
        int frameLen = 240;// buffer長度4800，分20等份，每一等份容量240
        double[] temp = new double[frameLen];
        double[] ampTotal = new double[detectFrame];

        for (int i = 0; i < detectFrame; i++) {
            if (data.length - i * frameLen >= frameLen) {
                System.arraycopy(data, i * frameLen, temp, 0, frameLen);
                double result = GoertzelDetect.getAmpCarrier(temp, frequency, frameLen);
                ampTotal[i] = result;

                if (isSync) {
                    moveData(temp, frameLen);
                } else {
                    if (i >= 2) {
                        if (averageEnergy(ampTotal, i) > Common.THRESHOLD) {
                            isSync = true;
                            moveData(temp, frameLen);
                        }
                    }
                }
            }
        }
        return isSync;
    }

    private void moveData(double[] data, int length) {
        for (int i = 0; i < length; i++) {
            rawData.add(data[i]);
            ByteBuffer.wrap(bufferBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put((short) data[i]);
        }
    }

    private double averageEnergy(double[] data, int index) {
        return (data[index] + data[index - 1] + data[index - 2]) / 3;
    }

    public boolean canDemodulate() {
        return rawData.size() > 86400;
    }

    public ArrayList<Double> getRemoveOffsetData() {
        ArrayList<Double> data = new ArrayList<>();
        int offset = 5520;
        for (int i = 0; i < rawData.size() - offset; i++) {
            data.add(rawData.get(offset + i));
        }
        return data;
    }

    public String getDemodulationResult(ArrayList<Double> data) {
        Demodulate mDemodulate = new Demodulate(frequency);
        return mDemodulate.getGoertzelDemodulate(data, frequency.getFreMin());
    }

    public void startRecord() {
        isRecording = true;
        audioRecord.startRecording();

        if (rawData == null) {
            rawData = new ArrayList<>();
        } else {
            rawData.clear();
        }
    }

    public void stopRecord() {
        if (null != audioRecord) {
            isRecording = false;

            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        }
    }
}
