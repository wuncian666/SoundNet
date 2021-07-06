package com.example.SoundNet;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.SoundNet.Fragment.FragmentList_One;
import com.example.SoundNet.Fragment.FragmentList_Three;
import com.example.SoundNet.Fragment.FragmentList_Two;
import com.example.SoundNet.Fragment.ViewPagerFragmentAdapter;
import com.example.SoundNet.MusicControl.SoundManager;

import com.google.android.material.tabs.TabLayout;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Handler mHandler;

    TextView textStatus, textResult, textMode, textPlayMusicTimes, textAppDuration, textCountCorrect, textCountDemodulation;

    SoundManager mSoundManager;

    public static boolean isMaster, isOnlyReceiveMode;

    public static int timesCorrect, countDemodulation;

    private long slaveCorrectTime;

    private final int[] IconResID = {R.drawable.selector_one};

    private final int[] ToolBarTitle = {R.string.app_name};

    public static final String[] array = {"HELLO!", "IOTLAB"};

    private ViewPager myViewPager;

    private TabLayout tabLayout;

    private Toolbar toolbar;

    PlayMusic mPlayMusic;

    SoundGenerator soundGenerator;

    EditText editMessage;

    Context context;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myViewPager = findViewById(R.id.myViewPager);
        tabLayout = findViewById(R.id.tabLayout);
        toolbar = findViewById(R.id.toolBar);

        toolbar.setTitle(ToolBarTitle[0]);
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

//        setViewPager();
        tabLayout.setupWithViewPager(myViewPager);
//        setTabLayoutIcon();

        editMessage = findViewById(R.id.edit_message);

        textStatus = findViewById(R.id.tv_status);
        textResult = findViewById(R.id.tv_result);
        textMode = findViewById(R.id.tv_mode);
        textPlayMusicTimes = findViewById(R.id.tv_app_play_music_times);
        textAppDuration = findViewById(R.id.tv_app_duration);
        textCountCorrect = findViewById(R.id.tv_count_correct);
        textCountDemodulation = findViewById(R.id.tv_count_demodulation);

        context = getApplicationContext();

        // 寫入與錄音權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            permissions();

        // main loop 可以操作ui
        mHandler = new Handler(Looper.myLooper());

        mSoundManager = new SoundManager(this);

        isOnlyReceiveMode = false;

        countDemodulation = 0;
        timesCorrect = 0;
    }

    public void btn_generate(View v) {
        String message = editMessage.getText().toString();

        soundGenerator = new SoundGenerator(message, context);
        soundGenerator.generatorSound();
        soundGenerator.playSound();
    }


    public void btn_receive(View v) {
        isOnlyReceiveMode = true;
        textMode.setText("Receive only");
        ReceiveProcess rec = new ReceiveProcess(this);
        Thread t = new Thread(rec);
        Log.i(TAG, "btn_receive: thread start");
        t.start();
    }

    public void btn_master(View v) {
        isOnlyReceiveMode = false;
        isMaster = true;
        textMode.setText("Master");
        ReceiveProcess rec = new ReceiveProcess(this);

        Thread t = new Thread(rec);
        Log.i(TAG, "btn_master: thread start");
        t.start();
    }

    public void btn_slave(View v) {
        isOnlyReceiveMode = false;
        isMaster = false;
        textMode.setText("Slave");
        ReceiveProcess rec = new ReceiveProcess(this);

        Thread t = new Thread(rec);
        Log.i(TAG, "btn_slave: thread start");
        t.start();
    }

    public void btn_play_music(View v) {

        mPlayMusic = new PlayMusic(this);

        Thread t = new Thread(mPlayMusic);
        Log.i(TAG, "btn_play_music: thread start");
        t.start();
    }

//    private void setViewPager() {
//        FragmentList_One myFragment1 = new FragmentList_One();
//        FragmentList_Two myFragment2 = new FragmentList_Two();
//        FragmentList_Three myFragment3 = new FragmentList_Three();
//
//        List<Fragment> fragmentList = new ArrayList<>();
//        fragmentList.add(myFragment1);
//        fragmentList.add(myFragment2);
//        fragmentList.add(myFragment3);
//
//        ViewPagerFragmentAdapter myFragmentAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
//
//        myViewPager.setAdapter(myFragmentAdapter);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_one, menu);
        return true;
    }

//    public void setTabLayoutIcon() {
//        for (int i = 0; i < IconResID.length; i++)
//            tabLayout.getTabAt(i).setIcon(IconResID[i]);
//
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                toolbar.getMenu().clear();
//                switch (tab.getPosition()) {
//                    case 0:
//                        toolbar.inflateMenu(R.menu.menu_one);
//                        toolbar.setTitle(ToolBarTitle[0]);
//                        break;
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
//    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permissions() {
        boolean storageHasGone = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        boolean recordHasGone = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String[] permissions;
            if (!storageHasGone && !recordHasGone) {// 如果兩個權限都未取得
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
                textMode.setText("儲存權限已取得\n錄音權限已取得");
                return;
            }
            requestPermissions(permissions, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        StringBuffer word = new StringBuffer();
        switch (permissions.length) {
            case 1:
                if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    word.append("儲存權限");
                else word.append("錄音權限");
                if (grantResults[0] == 0) word.append("已取得");
                else word.append("未取得");
                word.append("\n");
                if (permissions[0].equals(Manifest.permission.RECORD_AUDIO)) word.append("儲存權限");
                else word.append("錄音權限");
                word.append("已取得");

                break;
            case 2:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        word.append("儲存權限");
                    else word.append("錄音權限");
                    if (grantResults[i] == 0) word.append("已取得");
                    else word.append("未取得");
                    if (i < permissions.length - 1) word.append("\n");
                }
                break;
        }
        textMode.setText(word.toString());
    }

//    @SuppressLint("NewApi")
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
//    }

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
    }

    private boolean isFirstFinish = true;

    @Override
    public void finish() {
        if (isFirstFinish) {
            isFirstFinish = false;
            Toast.makeText(this, "再點擊一次返回鍵離開程式", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> isFirstFinish = true, 2000);
        } else {
            super.finish();
        }
    }

}