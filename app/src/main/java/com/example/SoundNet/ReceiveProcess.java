package com.example.SoundNet;


import android.content.Context;

import com.example.SoundNet.Config.AudioConfig;
import com.example.SoundNet.MusicControl.SoundManager;
import com.example.SoundNet.Utils.AudioProcessingUtils;
import com.example.SoundNet.Utils.Demodulate;
import com.example.SoundNet.WavFile.WavFileHandle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.subjects.PublishSubject;

public class ReceiveProcess {
    private final String TAG = this.getClass().getSimpleName();
    private boolean isReceiving;

    private ProcessState state;

    public PublishSubject<String> demodulationResult = PublishSubject.create();

    private final Record record;
    private final WavFileHandle wavFileHandle;
    private final SoundManager soundManger;
    private final FileOutputStream fileOutputStream;

    private int numWavFile;

    private final String rawFilename, wavFilename;

    public ReceiveProcess(Context context) throws FileNotFoundException {
        record = new Record(context);

        wavFileHandle = new WavFileHandle();

        soundManger = new SoundManager(context);

        rawFilename = wavFileHandle.getRawFilename(context, "temp.raw");
        wavFilename = wavFileHandle.getFolderName(context) + "temp.wav";

        fileOutputStream = new FileOutputStream(rawFilename);

        numWavFile = 0;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public void process() throws IOException {
        boolean canDemodulate = false;
        boolean isSync;

        while (isReceiving) {
            record.create();
            record.start();

            numWavFile++;

            long startTime = System.currentTimeMillis();

            while (record.isRecording()) {
                record.writeAudioData();

                isSync = record.isSync();

                if (isSync) {
                    fileOutputStream.write(record.bufferBytes);
                }

                canDemodulate = Demodulate.canDemodulate(record.getRecordSize());
                if (AudioProcessingUtils.masterTimeout(startTime, state, isSync) || canDemodulate) {
                    break;
                }
            }

            fileOutputStream.close();
            record.stop();

            if (canDemodulate) {
                wavFileHandle.copyWaveFile(rawFilename, wavFilename);

                ArrayList<Double> data = AudioProcessingUtils.getRemoveOffsetData(record.getRecordData());
                String result = this.getDemodulationResult(data);
                demodulationResult.onNext(result);

                this.playMusic(result);

                if (numWavFile > 10) numWavFile = 0;
            }
        }
    }

    private String getDemodulationResult(ArrayList<Double> data) {
        Demodulate demodulate = new Demodulate();
        return demodulate.getGoertzelDemodulate(data);
    }

    public void stopReceive() {
        isReceiving = false;
    }

    public void startReceive() {
        isReceiving = true;
    }

    private void playMusic(String result) {
        if (state == ProcessState.MASTER) {
            if (result.equals(AudioConfig.ACK)) {
                soundManger.playHelloSound();
            }
        } else {
            if (AudioProcessingUtils.checkResultInArray(result)) {
                soundManger.playAckSound();
            }
        }
    }
}