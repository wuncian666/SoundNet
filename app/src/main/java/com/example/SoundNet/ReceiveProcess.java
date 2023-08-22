package com.example.SoundNet;

import static com.example.SoundNet.MainActivity.array;

import android.content.Context;

import com.example.SoundNet.MusicControl.SoundManager;
import com.example.SoundNet.WavFile.WavFileHandle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.subjects.PublishSubject;

public class ReceiveProcess {
    private final String TAG = this.getClass().getSimpleName();
    private boolean isReceiving;

    private ProcessState state;

    int receiveTimes = 0;
    long slaveCorrectTime = 0;

    public PublishSubject<String> demodulationResult = PublishSubject.create();

    Record mRecord;
    Context context;
    WavFileHandle mWavFileHandle;
    SoundManager mSoundManager;

    int numWavFile;

    public ReceiveProcess(Context context) {
        this.context = context;

        Frequency frequency = new Frequency(18000, 20048, 20560);
        mRecord = new Record(context, frequency);

        mWavFileHandle = new WavFileHandle();

        mSoundManager = new SoundManager(context);

        numWavFile = 0;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public void process() throws IOException {
        while (isReceiving) {
            mRecord.onRecord();
            mRecord.startRecord();

            numWavFile++;

            FileOutputStream fileOutputStream = new FileOutputStream(getRawFilename());

            long tStartRecord = System.currentTimeMillis();

            while (mRecord.isRecording) {
                mRecord.writeAudioData();

                if (mRecord.isSync) {
                    fileOutputStream.write(mRecord.bufferBytes);
                }

                if (mRecord.canDemodulate()) {
                    receiveTimes++;
                    break;
                }

                if (masterTimeout(tStartRecord)) {
                    break;
                }
            }

            fileOutputStream.close();
            mRecord.stopRecord();

            if (mRecord.canDemodulate()) {
                mWavFileHandle.copyWaveFile(getRawFilename(), getWavFileName());

                ArrayList<Double> data = mRecord.getRemoveOffsetData();
                String result = mRecord.getDemodulationResult(data);
                demodulationResult.onNext(result);
                playMusic(result);
                initFileNum();
            }
        }
    }

    public void stopReceive() {
        isReceiving = false;
    }

    public void startReceive() {
        isReceiving = true;
    }

    public boolean isReceiving() {
        return isReceiving;
    }

    private String getRawFilename() {
        String tempFilename = numWavFile + ".raw";
        return mWavFileHandle.getRawFilename(context, tempFilename);
    }

    private String getWavFileName() {
        String folderName = mWavFileHandle.getFolderName(context);
        return folderName + numWavFile + ".wav";
    }
    private boolean masterTimeout(long tStart) {
        int timeout = 2;
        long tSpend = (System.currentTimeMillis() - tStart) / 1000;
        return (state == ProcessState.MASTER) && tSpend > timeout && !mRecord.isSync;
    }

    private void initFileNum() {
        if (numWavFile >= 10) {
            numWavFile = 0;
        }
    }

    public void playMusic(String result) {
        if (result != null) {
            if (state == ProcessState.MASTER) {
                if (result.equals("60")) {// 60 is ACK
                    mSoundManager.playSound(R.raw.hello_1time_have_end_20560, 0);
                }
            } else {
                if (checkResultInArray(result)) {
                    slaveCorrectTime++;
                    mSoundManager.playSound(R.raw.ack_1time_have_end_20560, 0);
                }
            }
        }
    }

    private boolean checkResultInArray(String result) {
        boolean isCorrect = false;
        if (result != null) {
            for (String s : array) {
                if (result.equals(s)) {
                    isCorrect = true;
                    break;
                }
            }
        }
        return isCorrect;
    }

    enum ProcessState {
        MASTER,
        SLAVE,
        NONE
    }
}
