package com.example.SoundNet;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.SoundNet.Fragment.FragmentList_Two;
import com.example.SoundNet.MusicControl.SoundManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.SoundNet.MainActivity.array;
import static com.example.SoundNet.MainActivity.isMaster;
import static com.example.SoundNet.MainActivity.isOnlyReceiveMode;

public class ReceiveProcess implements Runnable {
    private final static String TAG = "ReceiveProcess";
    public boolean isReceiving;

    int receiveTimes = 0;// 接收SYNC次數
    int resultCorrectTimes = 0;// 解調正確次數
    // 從 接收到正確值的時間
    long slaveCorrectTime = 0;

    long THRESHOLD = 3;

    Record mRecord;
    Context context;
    WavFileHandle mWavFileHandle;
    SoundManager mSoundManager;

    int numWavFile;

    public ReceiveProcess(Context context) {
        mRecord = new Record(20048, 20560);
        mWavFileHandle = new WavFileHandle();
        mSoundManager = new SoundManager(context);

        this.context = context;

        isReceiving = true;

        numWavFile = 0;
    }

    @Override
    public void run() {
        while (isReceiving) {

            boolean canSave = false;

            mRecord.onRecord();// 啟動record api
            mRecord.startRecord();

            long tStartRecord = System.currentTimeMillis();

            numWavFile++;
            // 修改raw檔名稱
            String tempFilename = numWavFile + "_" + Common.AUDIO_RECORDER_FILENAME_RAW;
            String rawFile = mWavFileHandle.getRawFilename(context, tempFilename);
            // wav檔資料夾
            String folderName = mWavFileHandle.getFolderName(context);
            // wav檔路徑加上檔名
            String wavFile = folderName + numWavFile + "_" + Common.AUDIO_RECORDER_FILENAME_WAV;

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(rawFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            while (mRecord.isRecording) {
                mRecord.writeAudioData();// 收音

                if (mRecord.isSync) {
                    try {
                        assert fileOutputStream != null;
                        fileOutputStream.write(mRecord.bufferBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // 判斷是否滿足資料大小
                if (mRecord.canDemodulate()) {
                    receiveTimes++;
                    canSave = true;
                    Log.i(TAG, "run: is end and size is full");
                    break;
                }

                long tRecordSpend = (System.currentTimeMillis() - tStartRecord) / 1000;
                // 判斷經過多少時間

                // 收音超過2秒都沒有sync頻率出現
                if (!isOnlyReceiveMode) {
                    if (isMaster) {
                        if (tRecordSpend > 2 && !mRecord.isSync) {
                            Log.i(TAG, "run: master spend more than 3s and no sync signal");
                            canSave = false;
                            break;
                        }
                    }
                }
            }

            try {
                assert fileOutputStream != null;
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mRecord.stopRecord();

            if (canSave) {
                // 將raw檔轉成wav檔
                mWavFileHandle.copyWaveFile(rawFile, wavFile);
            }

            if (numWavFile >= 10) numWavFile = 0;

            String result = null;

            if (mRecord.canDemodulate()) {
                result = mRecord.getDemodulationResult();
            }

            // 更新解調結果, 接收次數, 正確次數
            updateUi(context, result, receiveTimes, resultCorrectTimes);

            if (isOnlyReceiveMode) {
                // 純接收
                equalResultOnly(result);
            } else {
                // 判斷值是否正確
                equalResult(result);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void equalResultOnly(String result) {// 純接收模式
        boolean isCorrect = false;

        if (result != null) {
            for (String s : array) {
                if (result.equals(s)) {
                    isCorrect = true;
                    break;
                }
            }
        }

        String status;
        if (isCorrect) {
            status = "Slave received correct signal";

            resultCorrectTimes++;
            // 播放ACK
//            mSoundManager.playSound(0, 0);
        } else {
            status = "Slave received error signal";
        }
        updateUiStatus(context, status);
    }

    public void equalResult(String result) {
        boolean isCorrect = false;
        // 判斷模式
        // 主模式
        if (isMaster) {
            // 判斷是不是ACK
            if (result != null) {
                if (result.equals("ACK")) {
                    isCorrect = true;
                }
            }

            // 判斷解出的字串是否正確
            if (isCorrect) {
                resultCorrectTimes++;
                // 收到ack
                String status = "Master received ack signal";
                updateUiStatus(context, status);
                // 停止收音
//                isReceiving = false;
            } else {
                // 不是正確的字串
                String status = "Master received error signal";
                updateUiStatus(context, status);
                // 播放資訊
                mSoundManager.playSound(1, 0);
            }
        } else {
            // 從模式
            // 跟預設的值做比較
            if (result != null) {
                for (String s : array) {
                    if (result.equals(s)) {
                        isCorrect = true;
                        break;
                    }
                }
            }
            // 比較字串
            if (isCorrect) {
                // 紀錄接收到正確的時間
                slaveCorrectTime = System.currentTimeMillis();
                resultCorrectTimes++;
                // 接收到正確的值
                String status = "Slave received correct signal";
                updateUiStatus(context, status);
                // 播放ACK
                mSoundManager.playSound(0, 0);
            } else {
//                isReceiving = false;
                // 沒有收到正確值
                String status = "Slave received error signal";
                updateUiStatus(context, status);

                /*long timeCheck = ((System.currentTimeMillis() - slaveCorrectTime) / 1000);
                // 切換mode
                if (slaveCorrectTime != 0) {
                    // 超過一定時間無正確值
                    if (timeCheck > 30) {
                        // 切換成master
                        String mode = "Slave2Master";
                        updateUiMode(context, mode);
                        Log.i(TAG, "run: change to master, time: " + timeCheck);
                        isMaster = true;
                    }
                }*/
            }
        }
    }


    public void updateUi(Context context, String result, int receiveTimes, int resultCorrectTimes) {
        // 主線程更新UI
        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) context).textResult.setText(result);
                Log.i(TAG, "run: set text" + result);

                ((MainActivity) context).textCountDemodulation.setText(String.valueOf(receiveTimes));

                ((MainActivity) context).textCountCorrect.setText(String.valueOf(resultCorrectTimes));
            }
        });
    }

    public void updateUiStatus(Context context, String status) {
        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) context).textStatus.setText(status);
            }
        });
    }

    public void updateUiMode(Context context, String mode) {
        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) context).textMode.setText(mode);
            }
        });
    }
}
