package com.example.SoundNet;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import java.util.logging.Handler;

public class RunTime implements Runnable {
    TextView textView;
    long startTime;
    Context context;

    public RunTime(long startTime, Context context) {
        this.startTime = startTime;
        this.context = context;

        textView = (TextView) ((Activity) context).findViewById(R.id.tv_app_duration);
    }

    @Override
    public void run() {

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long spentTime = System.currentTimeMillis() - startTime;
            long min = (spentTime / 1000) / 60;
            long sec = (spentTime / 1000) % 60;

            textView.setText("執行時間: " + min + " 分 " + sec + " 秒");
        }
    }
}
