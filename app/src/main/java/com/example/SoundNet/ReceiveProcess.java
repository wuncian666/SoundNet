package com.example.SoundNet;

import android.content.Context;
import android.util.Log;
import com.example.SoundNet.Config.AudioConfig;
import com.example.SoundNet.MusicControl.SoundManager;
import com.example.SoundNet.Utils.AudioProcessingUtils;
import com.example.SoundNet.Utils.Demodulate;
import com.example.SoundNet.WavFile.WavFileHandle;
import io.reactivex.subjects.PublishSubject;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiveProcess {
  private static final String TAG = ReceiveProcess.class.getSimpleName();
  private final Record record;
  private final WavFileHandle wavFileHandle;
  private final SoundManager soundManager;
  private final Context context;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  public PublishSubject<String> demodulationResult = PublishSubject.create();
  private volatile boolean isReceiving; // 使用volatile確保線程安全
  private ProcessState state = ProcessState.NONE;
  private int numWavFile = 0;

  public ReceiveProcess(Context context) throws FileNotFoundException {
    this.context = context;
    record = new Record(context);
    wavFileHandle = new WavFileHandle();
    soundManager = new SoundManager(context);
  }

  public void setState(ProcessState state) {
    this.state = state;
    Log.d(TAG, "State set to: " + state);
  }

  public void startReceive() {
    if (isReceiving) return;
    isReceiving = true;
    executorService.submit(this::process);
    Log.i(TAG, "Started receiving audio");
  }

  public void stopReceive() {
    isReceiving = false;
    record.stop();
    Log.i(TAG, "Stopped receiving audio");
  }

  private void process() {
    while (isReceiving) {
      try {
        record.create();
        record.start();
        numWavFile++;

        String rawFilename = wavFileHandle.getRawFilename(context, "temp_" + numWavFile + ".raw");
        String wavFilename = wavFileHandle.getFolderName(context) + "temp_" + numWavFile + ".wav";

        try (FileOutputStream fileOutputStream = new FileOutputStream(rawFilename)) {
          long startTime = System.currentTimeMillis();
          boolean canDemodulate = false;
          boolean isSync = false;

          while (record.isRecording() && isReceiving) {
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

          if (canDemodulate) {
            wavFileHandle.copyWaveFile(rawFilename, wavFilename);
            ArrayList<Double> data =
                AudioProcessingUtils.getRemoveOffsetData(record.getRecordData());
            String result = getDemodulationResult(data);
            demodulationResult.onNext(result);
            playMusic(result);

            if (numWavFile > 10) numWavFile = 0; // 重置計數
          }
        } catch (IOException e) {
          Log.e(TAG, "Error processing audio: " + e.getMessage());
        } finally {
          record.stop();
        }
      } catch (Exception e) {
        Log.e(TAG, "Unexpected error in process: " + e.getMessage());
      }
    }
  }

  private String getDemodulationResult(ArrayList<Double> data) {
    Demodulate demodulate = new Demodulate();
    String result = demodulate.getGoertzelDemodulate(data);
    Log.d(TAG, "Demodulation result: " + result);
    return result;
  }

  private void playMusic(String result) {
    if (state == ProcessState.MASTER && result.equals(AudioConfig.ACK)) {
      soundManager.playHelloSound();
    } else if (AudioProcessingUtils.checkResultInArray(result)) {
      soundManager.playAckSound();
    }
  }

  public void shutdown() {
    stopReceive();
    executorService.shutdown();
  }
}
