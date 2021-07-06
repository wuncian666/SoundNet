package com.example.SoundNet;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Record {
    private static final String TAG = "Record";

    private static final int RECORD_CHANNELS = AudioFormat.CHANNEL_IN_MONO;

    private static final int RECORD_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final int RECORD_BPP = 16;

    private AudioRecord audioRecord = null;

    private final short[] buffer;

    public final byte[] bufferBytes;

    private ArrayList<Double> rawData;

    public boolean isRecording, isSync, isEnd;

    private final int SYNC_FREQUENCY, END_FREQUENCY;

    /**
     * shift, window = 25, (4800 * 2 - 6000 / 25) / (6000 /25) + 1, len symbol = 40
     */

    public Record(int SYNC_FREQUENCY, int END_FREQUENCY) {
        this.SYNC_FREQUENCY = SYNC_FREQUENCY;
        this.END_FREQUENCY = END_FREQUENCY;

        buffer = new short[4800];
        bufferBytes = new byte[buffer.length * 2];
    }

    public void onRecord() throws IllegalStateException {
        // 初始化
        isRecording = false;
        isSync = false;
        isEnd = false;

        if (audioRecord == null) {
            System.out.println("new audio record");
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
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

    /**
     * 開始錄音時，偵測sync頻率以及end頻率
     */
    public void writeAudioData() {
        audioRecord.read(buffer, 0, buffer.length);

        double[] temp = new double[buffer.length];

        if (AudioRecord.ERROR_INVALID_OPERATION != 0) {
            for (int i = 0; i < buffer.length; i++) {// arrTemp用於偵測sync頻率振幅，除以32768.0為規一化
                temp[i] = (double) buffer[i] / 32768.0;
            }

            if (!isSync) {// 偵測sync頻率振幅
                isSync = checkSync(temp, SYNC_FREQUENCY);
            } else {
                // 儲存聲音
                for (double i : temp) {
                    rawData.add(i);
                }
                ByteBuffer.wrap(bufferBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
            }
        }
    }

    /*第一步檢測SYNC振幅*/
    private boolean checkSync(double[] data, double freTarget) {
        boolean isSync = false;
        // buffer長度4800，分20等份，每一等份容量240
        double[] temp = new double[240];
        // 儲存每等份的振幅
        double[] ampTotal = new double[20];

        for (int i = 0; i < 20; i++) {
            // 總資料量減去移動量大於檢視視窗
            if (data.length - i * 240 >= 240) {
                // 取得shift點的陣列
                System.arraycopy(data, i * 240, temp, 0, 240);
                // 計算出shift點的振幅
                double result = GoertzelDetect.getAmpCarrier(temp, freTarget, 240);
                // 每個i所求的240點的振幅
                ampTotal[i] = result;

                if (isSync) {
                    for (int j = 0; j < 240; j++) {
                        rawData.add(temp[j]);
                        ByteBuffer.wrap(bufferBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put((short) temp[j]);
                    }
                } else {
                    if (i >= 2) {
                        // 第一個部分與第二個部分的振幅相加平均大於門檻值
                        if ((ampTotal[i] + ampTotal[i - 1] + ampTotal[i - 2]) / 3 > Common.THRESHOLD) {
                            Log.i(TAG, "checkSync: amp" + ampTotal[i] + ", " + ampTotal[i - 1]+ ", " + ampTotal[i - 2]);

                            isSync = true;
                            // 將這part共240點加入raw data
                            for (int j = 0; j < 240; j++) {
                                rawData.add(temp[j]);
                                ByteBuffer.wrap(bufferBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put((short) temp[j]);
                            }
                        }
                    }
                }
            }
        }
        return isSync;
    }

    public boolean canDemodulate() {
        return rawData.size() > 86400;
    }

    public String getDemodulationResult() {
        ArrayList<Double> removeSyncData = new ArrayList<>();

        Demodulate mDemodulate = new Demodulate(20048, 20560, 18000);

//        int syncIndex = mDemodulate.getSyncPosition(rawData, 1, 10) + 5760;

//        for (int i = 0; i < rawData.size() - syncIndex; i++)
//            removeSyncData.add(rawData.get(syncIndex + i));

        for (int i = 0; i < rawData.size() - 5520; i++)
            removeSyncData.add(rawData.get(5520 + i));

        String result = mDemodulate.getGoertzelDemodulate(removeSyncData, 18000);

        Log.i(TAG, "getDemodulationResult: " +
                " raw data: " + rawData.size() +
                " filter sync data: " + removeSyncData.size() +
                " result: " + result);

        return result;
    }

    public void startRecord() {
        isRecording = true;

        audioRecord.startRecording();// audioRecord API開始錄音
        System.out.println("開始錄音");

        if (rawData == null) {
            rawData = new ArrayList<>();// 紀錄sync頻率後資訊
            Log.i(TAG, "onRecord: new array");
        } else {
            rawData.clear();
            Log.i(TAG, "onRecord: clear array size: " + rawData.size());
        }
    }

    public void stopRecord() {
        if (null != audioRecord) {
            isRecording = false;

            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                System.out.println("停止錄音");
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        }
    }
}
