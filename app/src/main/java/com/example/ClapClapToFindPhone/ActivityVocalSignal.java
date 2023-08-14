package com.example.ClapClapToFindPhone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.IOException;

public class ActivityVocalSignal extends Activity implements OnCompletionListener, Callback {
    public static MediaPlayer mySong;
    private Camera camera;
    private final boolean hasFlash;
    private boolean isFlashOn;
    SurfaceHolder mHolder;
    private CountDownTimer mTimer = null;
    private ImageView home;
    private ImageView stop;
    Parameters params;
    SurfaceView preview;
    private boolean run;
    private TextView trendsetting;
    private Vibrator v;
    private boolean isSoundPlaying = false;

    public ActivityVocalSignal() {

        hasFlash = false;
    }

    public void onPointerCaptureChanged(boolean z) {
    }

    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_vocal_signal);

        Context mCtx = this;
        boolean deviceHasCameraFlash = getPackageManager().hasSystemFeature("android.hardware.camera.flash");
        initialize();
        ClassesApp classesApp = new ClassesApp(mCtx);
        classesApp.save("StopService", "1");
        String flashbox = classesApp.read("flashbox", "1");
        String vibratebox = classesApp.read("vibratebox", "1");
        String soundbox = classesApp.read("soundbox", "1");
        classesApp.save("detectClap", "1");
        stopService(new Intent(getBaseContext(), VocalService.class));
        setVolume(Integer.parseInt(classesApp.read("seekBar", "50")));
        getWindow().addFlags(128);
        if (flashbox.equals("1") && deviceHasCameraFlash) {
            isFlashOn = false;
            getCamera();
            startTimer(1000, true);
        }
        if (vibratebox.equals("1")) {
            runVibrate(true);
        }
        if (soundbox.equals("1")) {
            runSong();
        }
    }

    private void setVolume(int i) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(3, (int) (((float) audioManager.getStreamMaxVolume(3)) * (((float) i) / 100.0f)), 0);
    }

    private void runVibrate(boolean z) {
        run = z;
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        new Thread(() -> {
            while (ActivityVocalSignal.this.run) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ActivityVocalSignal.this.v.vibrate(1000);
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    private void runSong() {
        mySong = MediaPlayer.create(this, R.raw.cat_meowing);
        mySong.setOnCompletionListener(this);
        mySong.start();
    }

    @SuppressLint("SetTextI18n")
    private void initialize() {
        trendsetting = findViewById(R.id.settingtext);
        stop = findViewById(R.id.stop);
        home = findViewById(R.id.home);
        stop.setOnClickListener(view -> {
            if (mTimer != null) {
                mTimer.cancel();
            }
            turnOffFlash();
            stopSound();
            run = false;
            runVibrate(false);
            stop.setVisibility(View.INVISIBLE);
            home.setVisibility(View.VISIBLE);
            trendsetting.setText("ALARM STOPPED");
            trendsetting.setTextColor(ActivityVocalSignal.this.getResources().getColor(R.color.green));
        });
        home.setOnClickListener(view -> {
            Intent intent = new Intent(ActivityVocalSignal.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityVocalSignal.this.startActivity(intent);
            ActivityVocalSignal.this.finish();
        });
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    /* Access modifiers changed, original: 0000 */
    public void startTimer(long j, boolean z) {
        this.run = z;
        if (run) {
            mTimer = new CountDownTimer(j, 1000) {
                public void onTick(long j) {
                }

                public void onFinish() {
                    if (ActivityVocalSignal.this.isFlashOn) {
                        ActivityVocalSignal.this.turnOffFlash();
                    } else {
                        ActivityVocalSignal.this.turnOnFlash();
                    }
                    ActivityVocalSignal activityVocalSignal = ActivityVocalSignal.this;
                    activityVocalSignal.startTimer(1000, activityVocalSignal.run);
                }
            };
            this.mTimer.start();
        }
    }

    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        mHolder = surfaceHolder;
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        try {
            camera.stopPreview();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        mHolder = null;
    }


    public void onDestroy() {
        try {
            MainActivity.activityMain.finish();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }


    public void onPause() {
        super.onPause();
        turnOffFlash();
    }


    public void onRestart() {
        super.onRestart();
    }


    public void onResume() {
        super.onResume();
        if (hasFlash) {
            turnOnFlash();
        }
    }


    public void onStart() {
        super.onStart();
        getCamera();
    }


    public void onStop() {
        super.onStop();
        Camera camera = this.camera;
        if (camera != null) {
            camera.release();
            this.camera = null;
        }
    }
    private void stopSound() {
        if (mySong != null && isSoundPlaying) {
            mySong.release();
            isSoundPlaying = false;
        }
    }

    private void getCamera() {
        preview = findViewById(R.id.PREVIEW);
        mHolder = preview.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(3);
        if (this.camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
                try {
                    camera.setPreviewDisplay(mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (RuntimeException ignored) {
            }
        }
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

    private void turnOffFlash() {
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

    public void onBackPressed() {
        CountDownTimer countDownTimer = this.mTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        turnOffFlash();
        MediaPlayer mediaPlayer = mySong;
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        run = false;
        runVibrate(false);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
