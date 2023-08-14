package com.example.ClapClapToFindPhone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


public class VocalService extends Service {

    public static final int DETECT_NONE = 0;
    public static final int DETECT_WHISTLE = 1;
    private static final int NOTIFICATION_Id = 1;

    public static int selectedDetection;
    private ClassesApp classesApp;
    private RecorderThread recorderThread;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public int onStartCommand(Intent intent, int flags, int startId) {
        startDetection();
        Notification notification = buildNotification();
        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    public void startDetection() {
        try {
            new DetectClapClap(getApplicationContext(), () -> {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                   vibrator.vibrate(4000);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    turnOnFlash(true);
                }
                new Handler(Looper.getMainLooper()).postDelayed(() -> handleOff(), 1000 * 10);
                playAudioAssets();
                Log.e("~~~", "onDetected: ");
            }).listen();
            this.classesApp = new ClassesApp(this);
            this.classesApp.save("detectClap", "0");
        } catch (Exception unused) {
            Toast.makeText(this, "Recorder not supported by this device", 1).show();
        }
    }

    MediaPlayer mediaPlayer;

    void handleOff(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            turnOnFlash(false);
        }
    }
    void playAudioAssets() {
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd("cat_meowing.mp3");
            mediaPlayer.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength()
            );
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void turnOnFlash(boolean isOn) {
        CameraManager manager = (CameraManager) this.getSystemService(AppCompatActivity.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = manager.getCameraIdList()[0];
            manager.setTorchMode(cameraId, isOn);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        RecorderThread recorderThread = this.recorderThread;
        if (recorderThread != null) {
            recorderThread.stopRecording();
            this.recorderThread = null;
        }
        selectedDetection = 0;
        Toast.makeText(this, "Detection stopped", Toast.LENGTH_LONG).show();
    }

    public void onWhistleDetected() {
        Intent intent = new Intent(this, ActivityVocalSignal.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Clap detected", Toast.LENGTH_LONG).show();
        stopSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Notification buildNotification() {
        Intent fullScreenIntent = new Intent(this, MainActivity.class);
        int flag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? (PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE) : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, flag);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.alarm)
                        .setContentTitle("Battery charging animation")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setCategory(Notification.CATEGORY_SERVICE);
//                        .setFullScreenIntent(fullScreenPendingIntent, true);
        notificationBuilder.setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("123", "123", NotificationManager.IMPORTANCE_HIGH));
            notificationBuilder.setChannelId("123");
        }
        return notificationBuilder.build();
    }
}
