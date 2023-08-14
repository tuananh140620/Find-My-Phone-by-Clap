package com.example.ClapClapToFindPhone;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.widget.RemoteViews;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioFormat;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector;


public class DetectClapClap implements OnsetHandler {
    public static MediaPlayer mySong;
    Camera.Parameters params;
    private boolean isFlashOn;
    private Camera camera;
    private boolean run;
    static int SAMPLE_RATE = 8000;
    private final byte[] buffer;
    private int clap;
    private final ClassesApp classesApp;
    private final Context mContext;
    private boolean mIsRecording;
    private final PercussionOnsetDetector mPercussionOnsetDetector;
    private int rateSupported;
    private boolean rate_send;
    private final AudioRecord recorder;
    private AudioFormat torsosFormat;
    private NotificationChannel notificationChannel;
    private Notification.Builder builder;
    private final String channelId = "i.apps.notifications";
    private final String description = "Test notification";
    private Vibrator v;
    private IDetect callback;
    @SuppressLint("MissingPermission")
    DetectClapClap(Context context, IDetect mCallback) {
        classesApp = new ClassesApp(context);
        SAMPLE_RATE = getValidSampleRates();
        mContext = context;
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, 16, 2);
        buffer = new byte[minBufferSize];
        recorder = new AudioRecord(1, SAMPLE_RATE, 16, 2, minBufferSize);
        mPercussionOnsetDetector = new PercussionOnsetDetector((float) SAMPLE_RATE, minBufferSize / 2, this, 24.0d, 5.0d);
        clap = 0;
        mIsRecording = true;
        this.callback = mCallback;
    }

    public int getValidSampleRates() {
        for (int i : new int[]{44100, 22050, 16000, 11025, 8000}) {
            if (AudioRecord.getMinBufferSize(i, 1, 2) > 0 && !rate_send) {
                rateSupported = i;
                rate_send = true;
            }
        }
        return rateSupported;
    }

    public void handleOnset(double d, double d2) {
        clap++;
        int nb_claps = 2;
        if (clap >= nb_claps) {
            classesApp.save("detectClap", "1");
            mIsRecording = false;
            showNotification();

            callback.onDetected();
        }
    }
    private void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1234);
    }

    public void listen() {
        recorder.startRecording();
        torsosFormat = new AudioFormat((float) SAMPLE_RATE, 16, 1, true, false);
        new Thread(() -> {
            while (DetectClapClap.this.mIsRecording) {
                AudioEvent audioEvent = new AudioEvent(DetectClapClap.this.torsosFormat,
                        DetectClapClap.this.recorder.read(DetectClapClap.this.buffer, 0, DetectClapClap.this.buffer.length));
                audioEvent.setFloatBufferWithByteBuffer(DetectClapClap.this.buffer);
                DetectClapClap.this.mPercussionOnsetDetector.process(audioEvent);
            }
            DetectClapClap.this.recorder.stop();
        }).start();
    }

    private void runVibrate() {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    v.vibrate(1000);
                } catch (Exception ignored) {
                }
            }
        }).start();
    }
private void showNotification() {
    NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
    Intent intent = new Intent(mContext.getApplicationContext(), MainActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(mContext.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.popup_notification);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationChannel = new NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationChannel.enableLights(true);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationChannel.setLightColor(Color.GREEN);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationChannel.enableVibration(false);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.createNotificationChannel(notificationChannel);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        builder = new Notification.Builder(mContext.getApplicationContext(), channelId)
                .setContent(contentView)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher_background))
                .setContentIntent(pendingIntent);
    }
    notificationManager.notify(1234, builder.build());
}
    private void runVibrate(boolean z) {
        run = z;
        v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    v.vibrate(1000);
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    private void runSong() {
        mySong = MediaPlayer.create(mContext, R.raw.cat_meowing);
        mySong.setOnCompletionListener(mediaPlayer -> runSong());
        mySong.start();
    }

    private void turnOnFlash() {
        if (!isFlashOn) {
            Camera camera = this.camera;
            if (camera != null && params != null) {
                isFlashOn = true;
                try {
                    params = camera.getParameters();
                    params.setFlashMode("torch");
                    camera.setParameters(params);
                    camera.startPreview();
                } catch (Exception ignored) {
                }
            }
        }
    }

     public void turnOffFlash() {
        if (isFlashOn) {
            Camera camera = this.camera;
            if (camera != null && params != null) {
                this.isFlashOn = false;
                try {
                    params = camera.getParameters();
                    params.setFlashMode("off");
                    camera.setParameters(params);
                    camera.stopPreview();
                } catch (Exception ignored) {
                }
            }
        }
    }



}
