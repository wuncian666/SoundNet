package com.example.SoundNet.MusicControl;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.SoundNet.MainActivity;
import com.example.SoundNet.R;

public class MediaService extends Service {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static final String CHANNEL_ID = "MyMusicPlayer";
    private MusicBinder musicBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        /**將歌曲載入到MediaPlayer中*/
        mediaPlayer = MediaPlayer.create(this, R.raw.hello_1time_have_end_20560);
        /**載入mediaPlayer給Binder音樂工具包*/
        musicBinder = new MusicBinder(mediaPlayer);
        /**設置音樂播放屬性為Loop*/
        mediaPlayer.setLooping(true);
        /**將音樂歸0*/
        mediaPlayer.seekTo(0);
        /**有取用到本Service的話，則在通知欄顯示通知
         * notificationIntent的部分是使如果使用者點擊通知的話，便會跳回本APP中*/
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID )
                .setContentTitle("注意")
                .setContentText("音樂撥放中")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /**此處為取得Notification的使用權限*/
        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, "My Service"
                    , NotificationManager.IMPORTANCE_DEFAULT);
        }
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    /**綁定音樂處理的各種方法*/
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }
}
