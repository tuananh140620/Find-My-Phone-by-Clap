package com.example.ClapClapToFindPhone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.RequiresApi;


public class VocalService extends Service {

    public static int selectedDetection;
    private RecorderThread recorderThread;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        startDetection();
        Notification notification = buildNotification();
        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    public void startDetection() {
        try {
            new DetectClapClap(getApplicationContext()).listen();
            ClassesApp classesApp = new ClassesApp(this);
            classesApp.save("detectClap", "0");
        } catch (Exception unused) {
            Toast.makeText(this, "Recorder not supported by this device", Toast.LENGTH_LONG).show();
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

    private Notification buildNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.alarm)
                        .setContentTitle("Battery charging animation")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setCategory(Notification.CATEGORY_SERVICE);
        notificationBuilder.setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("123", "123", NotificationManager.IMPORTANCE_HIGH));
            notificationBuilder.setChannelId("123");
        }
        return notificationBuilder.build();
    }
}
