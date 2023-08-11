package com.example.ClapClapToFindPhone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.IOException;

public class ActivityVocalSignal extends Activity implements OnCompletionListener, Callback {
    public static MediaPlayer mySong;
    private Camera camera;
    private boolean hasFlash;
    private boolean isFlashOn;
    private Context mCtx;
    SurfaceHolder mHolder;
    private RelativeLayout mProgressBar;
    private CountDownTimer mTimer = null;
    private ImageView home;
    private ImageView stop;
    Parameters params;
    SurfaceView preview;
    private boolean run;
    private TextView trendsetting;
    private Vibrator v;

    public void onPointerCaptureChanged(boolean z) {
    }

    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_vocal_signal);

        mCtx = this;
        boolean deviceHasCameraFlash = getPackageManager().hasSystemFeature("android.hardware.camera.flash");
        initialize();
        ClassesApp classesApp = new ClassesApp(this.mCtx);
        classesApp.save("StopService", "1");
        String flashbox = classesApp.read("flashbox", "1");
        String vibratebox = classesApp.read("vibratebox", "1");
        String soundbox = classesApp.read("soundbox", "1");
        classesApp.save("detectClap", "1");
        stopService(new Intent(getBaseContext(), VocalService.class));
        setVolume(Integer.parseInt(classesApp.read("seekBar", "50")));
        getWindow().addFlags(128);
        if (flashbox.equals("1") && deviceHasCameraFlash) {
            this.isFlashOn = false;
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
        this.run = z;
        Context context = this.mCtx;
        this.v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        new Thread(new Runnable() {
            public void run() {
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
            }
        }).start();
    }

    private void runSong() {
        mySong = MediaPlayer.create(this, R.raw.alarm);
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
            if (ActivityVocalSignal.mySong != null) {
                ActivityVocalSignal.mySong.release();
            }
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
        if (this.run) {
            this.mTimer = new CountDownTimer(j, 1000) {
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
        this.mHolder = surfaceHolder;
        try {
            this.camera.setPreviewDisplay(this.mHolder);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        try {
            this.camera.stopPreview();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        this.mHolder = null;
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
        if (this.hasFlash) {
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

    public void checkFlash() {
        this.hasFlash = getApplicationContext().getPackageManager().hasSystemFeature("android.hardware.camera.flash");
        if (this.hasFlash) {
            Log.d("FFF", "c'è il flash");
            return;
        }
        Builder builder = new Builder(this);
        builder.setTitle("Errore!");
        builder.setMessage("Il tuo telefono non ha il flash!");
        builder.setPositiveButton("OK, compro un Nexus", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityVocalSignal.this.finish();
            }
        });
    }

    private void getCamera() {
        this.preview = findViewById(R.id.PREVIEW);
        this.mHolder = this.preview.getHolder();
        this.mHolder.addCallback(this);
        this.mHolder.setType(3);
        if (this.camera == null) {
            try {
                this.camera = Camera.open();
                this.params = this.camera.getParameters();
                try {
                    this.camera.setPreviewDisplay(this.mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void turnOnFlash() {
        if (!this.isFlashOn) {
            Camera camera = this.camera;
            if (camera != null && this.params != null) {
                this.isFlashOn = true;
                try {
                    this.params = camera.getParameters();
                    this.params.setFlashMode("torch");
                    this.camera.setParameters(this.params);
                    this.camera.startPreview();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void turnOffFlash() {
        if (this.isFlashOn) {
            Camera camera = this.camera;
            if (camera != null && this.params != null) {
                this.isFlashOn = false;
                try {
                    this.params = camera.getParameters();
                    this.params.setFlashMode("off");
                    this.camera.setParameters(this.params);
                    this.camera.stopPreview();
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
        this.run = false;
        runVibrate(false);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
