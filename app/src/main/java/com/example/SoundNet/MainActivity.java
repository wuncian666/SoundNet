package com.example.SoundNet;

import static com.example.SoundNet.ReceiveProcess.ProcessState.MASTER;
import static com.example.SoundNet.ReceiveProcess.ProcessState.NONE;
import static com.example.SoundNet.ReceiveProcess.ProcessState.SLAVE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    public static final String[] array = {"HELLO!", "IOTLAB"};

    TextView textResult, textPlayMusicTimes, textAppDuration, textCountCorrect, textCountDemodulation;

    EditText editMessage;

    private Switch switchMaster, switchSlave, switchPTP;

    private DisposableObserver<String> mObserver;

    ReceiveProcess rec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        editMessage = findViewById(R.id.edit_message);

        textResult = findViewById(R.id.tv_result);
        textAppDuration = findViewById(R.id.tv_app_duration);
        textCountCorrect = findViewById(R.id.tv_count_correct);
        textCountDemodulation = findViewById(R.id.tv_count_demodulation);

        switchMaster = findViewById(R.id.switch_master);
        switchSlave = findViewById(R.id.switch_slave);
        switchPTP = findViewById(R.id.switch_ptp);

        rec = new ReceiveProcess(this);

        Observable<String> mObservable = Observable.fromCallable(() -> {
            rec.process();
            return null;
        });

        mObserver = new DisposableObserver<String>() {
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

        mObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mObserver);

        switchMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(switchSlave.isChecked() ||switchPTP.isChecked())) {
                switchProcess(isChecked, MASTER);
            }
        });

        switchSlave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(switchMaster.isChecked() ||switchPTP.isChecked())) {
                switchProcess(isChecked, SLAVE);
            }
        });

        switchPTP.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(switchSlave.isChecked() ||switchMaster.isChecked())) {
                switchProcess(isChecked, NONE);
            }
        });
    }

    private void switchProcess(boolean isChecked, ReceiveProcess.ProcessState state) {
        if (isChecked) {
            rec.stopReceive();
        } else {
            rec.setState(state);
            rec.startReceive();
        }
    }

    public void btn_generate(View v) throws FileNotFoundException {
        String message = editMessage.getText().toString();

        SoundGenerator soundGenerator = new SoundGenerator(message, this);
        soundGenerator.generatorSound();
        soundGenerator.playSound();
    }

    private void processSwitch() {
        if (rec.isReceiving()) {
            rec.startReceive();
        } else {
            rec.stopReceive();
        }
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
        super.onDestroy();

        mObserver.dispose();
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