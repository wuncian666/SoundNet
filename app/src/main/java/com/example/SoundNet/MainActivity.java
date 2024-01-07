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

import com.example.SoundNet.AudioPlayer.SoundGenerator;

import java.io.FileNotFoundException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    TextView textResult, textAppDuration, textCountCorrect, textCountDemodulation;

    EditText editMessage;

    private SwitchCompat switchMaster, switchSlave, switchPTP;

    private DisposableObserver<String> receiveObserver;

    ReceiveProcess receiveProcess = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.requestPermissions();
        this.findViewById();
        this.receiveProcess();
        this.switchListener();
    }

    private void receiveProcess() {
        try {
            receiveProcess = new ReceiveProcess(this);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Observable<String> receiveObservable = Observable.fromCallable(() -> {
            receiveProcess.process();
            return null;
        });

        receiveObserver = new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
            }

            @Override
            public void onComplete() {
            }
        };

        receiveObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(receiveObserver);
    }

    private void findViewById() {
        editMessage = findViewById(R.id.edit_message);

        textResult = findViewById(R.id.tv_result);
        textAppDuration = findViewById(R.id.tv_app_duration);
        textCountCorrect = findViewById(R.id.tv_count_correct);
        textCountDemodulation = findViewById(R.id.tv_count_demodulation);

        switchMaster = findViewById(R.id.switch_master);
        switchSlave = findViewById(R.id.switch_slave);
        switchPTP = findViewById(R.id.switch_ptp);
    }

    private void switchListener() {
        switchMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(switchSlave.isChecked() || switchPTP.isChecked())) {
                switchProcess(isChecked, MASTER);
            }
        });

        switchSlave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(switchMaster.isChecked() || switchPTP.isChecked())) {
                switchProcess(isChecked, SLAVE);
            }
        });

        switchPTP.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(switchSlave.isChecked() || switchMaster.isChecked())) {
                switchProcess(isChecked, NONE);
            }
        });
    }

    private void switchProcess(boolean isChecked, ProcessState state) {
        if (isChecked) {
            receiveProcess.stopReceive();
        } else {
            receiveProcess.setState(state);
            receiveProcess.startReceive();
        }
    }

    public void btn_generate(View v) throws FileNotFoundException {
        String message = editMessage.getText().toString();

        SoundGenerator soundGenerator = new SoundGenerator(message, this);
        soundGenerator.generatorSound();
        soundGenerator.playSound();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_one, menu);
        return true;
    }

    private void requestPermissions() {
        boolean storageHasGone = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        boolean recordHasGone = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;

        String[] permissions;
        if (!storageHasGone && !recordHasGone) {
            permissions = new String[2];
            permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            permissions[1] = Manifest.permission.RECORD_AUDIO;
        } else if (!storageHasGone) {
            permissions = new String[1];
            permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        } else if (!recordHasGone) {
            permissions = new String[1];
            permissions[0] = Manifest.permission.RECORD_AUDIO;
        } else {
            return;
        }
        requestPermissions(permissions, 100);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        receiveObserver.dispose();
        super.onDestroy();
    }

    private boolean isFirstFinish = true;

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