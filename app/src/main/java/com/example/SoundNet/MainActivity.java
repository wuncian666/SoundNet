package com.example.SoundNet;

import static com.example.SoundNet.ProcessState.MASTER;
import static com.example.SoundNet.ProcessState.NONE;
import static com.example.SoundNet.ProcessState.SLAVE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import com.example.SoundNet.AudioPlayer.SoundGenerator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();
  private static final int REQUEST_PERMISSIONS_CODE = 100;

  // UI元素
  private TextView textResult, textAppDuration, textCountCorrect, textCountDemodulation;
  private EditText editMessage;
  private SwitchCompat switchMaster, switchSlave, switchPTP;

  // 處理相關
  private ReceiveProcess receiveProcess;
  private Disposable resultDisposable;
  private boolean isFirstFinish = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    init();
  }

  private void init() {
    requestPermissions();
    initViews();
    setupSwitchListeners();
    setupReceiveProcess();
  }

  private void initViews() {
    editMessage = findViewById(R.id.edit_message);
    textResult = findViewById(R.id.tv_result);
    textAppDuration = findViewById(R.id.tv_app_duration);
    textCountCorrect = findViewById(R.id.tv_count_correct);
    textCountDemodulation = findViewById(R.id.tv_count_demodulation);
    switchMaster = findViewById(R.id.switch_master);
    switchSlave = findViewById(R.id.switch_slave);
    switchPTP = findViewById(R.id.switch_ptp);
  }

  private void setupSwitchListeners() {
    switchMaster.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          updateSwitchState(isChecked, MASTER, "Master mode enabled", switchSlave, switchPTP);
        });

    switchSlave.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          updateSwitchState(isChecked, SLAVE, "Slave mode enabled", switchMaster, switchPTP);
        });

    switchPTP.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          updateSwitchState(
              isChecked, NONE, "Point-to-Point mode enabled", switchMaster, switchSlave);
        });
  }

  private void updateSwitchState(
      boolean isChecked, ProcessState state, String toastMessage, SwitchCompat... others) {
    if (receiveProcess == null) return;

    if (isChecked) {
      for (SwitchCompat other : others) {
        other.setChecked(false); // 互斥關閉其他Switch
      }
      receiveProcess.setState(state);
      receiveProcess.startReceive();
      Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
    } else {
      receiveProcess.stopReceive();
      Toast.makeText(this, "Receiving stopped", Toast.LENGTH_SHORT).show();
    }
  }

  private void setupReceiveProcess() {
    try {
      receiveProcess = new ReceiveProcess(this);
      // 訂閱解調結果並更新UI
      resultDisposable =
          receiveProcess
              .demodulationResult
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(
                  result -> {
                    textResult.setText(result);
                    Log.d(TAG, "Demodulation result received: " + result);
                  },
                  error -> {
                    Log.e(TAG, "Error receiving demodulation result: " + error.getMessage());
                    Toast.makeText(this, "Error in audio processing", Toast.LENGTH_SHORT).show();
                  });
    } catch (FileNotFoundException e) {
      Log.e(TAG, "Failed to initialize ReceiveProcess: " + e.getMessage());
      Toast.makeText(this, "Initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  public void btn_generate(View v) {
    if (editMessage == null) {
      Toast.makeText(this, "Input field not initialized", Toast.LENGTH_SHORT).show();
      return;
    }

    String message = editMessage.getText().toString().trim();
    if (message.isEmpty()) {
      Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
      return;
    }

    // 禁用按鈕，避免重複點擊
    v.setEnabled(false);
    Toast.makeText(this, "Generating sound...", Toast.LENGTH_SHORT).show();

    // 在背景線程執行音頻生成
    new Thread(
            () -> {
              try {
                SoundGenerator soundGenerator = new SoundGenerator(message, MainActivity.this);
                soundGenerator.generateSound(); // 生成音頻檔案
                runOnUiThread(
                    () -> {
                      soundGenerator.playSound(); // 在主線程播放
                      Toast.makeText(
                              MainActivity.this,
                              "Sound generated and played successfully",
                              Toast.LENGTH_SHORT)
                          .show();
                    });
              } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid input: " + e.getMessage());
                runOnUiThread(
                    () ->
                        Toast.makeText(
                                MainActivity.this,
                                "Invalid input: " + e.getMessage(),
                                Toast.LENGTH_SHORT)
                            .show());
              } catch (IOException e) {
                Log.e(TAG, "Sound generation failed: " + e.getMessage());
                runOnUiThread(
                    () ->
                        Toast.makeText(
                                MainActivity.this,
                                "Failed to generate sound: " + e.getMessage(),
                                Toast.LENGTH_SHORT)
                            .show());
              } finally {
                runOnUiThread(() -> v.setEnabled(true)); // 恢復按鈕
              }
            })
        .start();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_one, menu);
    return true;
  }

  private void requestPermissions() {
    List<String> permissionsNeeded = new ArrayList<>();
    if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
      permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
    }

    if (!permissionsNeeded.isEmpty()) {
      ActivityCompat.requestPermissions(
          this, permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS_CODE);
    }
  }

  private boolean hasPermission(String permission) {
    return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
  }

  @Override
  protected void onDestroy() {
    if (resultDisposable != null && !resultDisposable.isDisposed()) {
      resultDisposable.dispose();
    }
    if (receiveProcess != null) {
      receiveProcess.shutdown(); // 清理ReceiveProcess資源
    }
    super.onDestroy();
  }

  @Override
  public void finish() {
    if (isFirstFinish) {
      isFirstFinish = false;
      Toast.makeText(this, R.string.click_twice_to_exit, Toast.LENGTH_SHORT).show();
      new Handler(Looper.getMainLooper()).postDelayed(() -> isFirstFinish = true, 2000);
    } else {
      super.finish();
    }
  }
}
