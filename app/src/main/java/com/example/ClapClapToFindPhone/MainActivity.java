package com.example.ClapClapToFindPhone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends Activity implements View.OnClickListener {
    private ImageView btnStart;
    private CheckBox checkBoxSound;
    private CheckBox checkBoxVibrate;
    private ImageView buttonStop;
    private ClassesApp classesApp;
    private int intOnTick;
    public Boolean mPermCAm;
    private SeekBar seekBar;
    private LinearLayout txtStart;
    private CheckBox checkBoxFlash;
    public static boolean MainIsRun;
    @SuppressLint("StaticFieldLeak")
    public static Activity activityMain;

    public MainActivity() {
    }

    @SuppressLint({"WrongConstant", "CutPasteId", "MissingInflatedId"})
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.btn_start_service);
        txtStart = findViewById(R.id.txt_feild);
        checkBoxFlash = findViewById(R.id.flashbox);
        checkBoxVibrate = findViewById(R.id.vibratebox);
        checkBoxSound = findViewById(R.id.soundbox);
        seekBar = findViewById(R.id.seekbar);
        buttonStop = findViewById(R.id.btn_stop);

        isMyServiceRunning();

        if (isMyServiceRunning()) {
            txtStart.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.VISIBLE);
        }
        checkBoxFlash.setChecked(false);
        checkBoxVibrate.setChecked(false);
        checkBoxSound.setChecked(false);
        classesApp = new ClassesApp(this);

        classesApp.read("seekBar", "50");
        String flashbox = classesApp.read("flashbox", "1");
        String vibratebox = classesApp.read("vibratebox", "1");
        String soundbox = classesApp.read("soundbox", "1");

        classesApp.save("detectClap", "1");
        stopService(new Intent(getBaseContext(), VocalService.class));
        setVolume(Integer.parseInt(classesApp.read("seekBar", "50")));
        getWindow().addFlags(128);

        if (flashbox.equals("1")) {
            checkBoxFlash.setChecked(true);
        }
        if (vibratebox.equals("1")) {
            checkBoxVibrate.setChecked(true);
        }
        if (soundbox.equals("1")) {
            checkBoxSound.setChecked(true);
        }
        seekBarValue();
        initialization();
        settingSound();
        checkCamFlash();
    }

    private void initVolume() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.getStreamVolume(3);
        audioManager.setStreamVolume(3, (int) (((float) audioManager.getStreamMaxVolume(3)) * (Float.parseFloat(classesApp.read(NotificationCompat.CATEGORY_PROGRESS, "50")) / 100.0f)), 0);
    }

    private void settingSound() {
        seekBar.setProgress(Integer.parseInt(classesApp.read("seekBar", "50")));
        initVolume();
        new CountDownTimer(5000, 500) {
            public void onTick(long j) {
                MainActivity.this.intOnTick = MainActivity.this.intOnTick + 1;
            }

            public void onFinish() {
                MainActivity.this.intOnTick = 0;
            }
        }.start();
    }

    private void setVolume(int i) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(3, (int) (((float) audioManager.getStreamMaxVolume(3)) * (((float) i) / 100.0f)), 0);
    }

    private boolean isMyServiceRunning() {
        for (RunningServiceInfo runningServiceInfo : ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
            if (VocalService.class.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void onPause() {
        super.onPause();
    }


    public void onResume() {
        super.onResume();
    }


    public void onDestroy() {
        super.onDestroy();
    }

    private void seekBarValue() {
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                String stringBuilder = "" +
                        i;
                MainActivity.this.classesApp.save("seekBar", stringBuilder);
                MainActivity.this.setvolume(i);
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void checkbox() {
        if (!this.checkBoxFlash.isChecked()) {
            this.classesApp.save("flashbox", "0");
        } else if (this.mPermCAm) {
            this.classesApp.save("flashbox", "1");
        } else {
            this.classesApp.save("flashbox", "0");
        }
        if (this.checkBoxVibrate.isChecked()) {
            this.classesApp.save("vibratebox", "1");
        } else {
            this.classesApp.save("vibratebox", "0");
        }
        if (this.checkBoxSound.isChecked()) {
            this.classesApp.save("soundbox", "1");
        } else {
            this.classesApp.save("soundbox", "0");
        }
    }

    private void setvolume(int i) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(3, (int) (((float) audioManager.getStreamMaxVolume(3)) * (((float) i) / 100.0f)), 0);
    }

    private void initialization() {
        btnStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_start_service) {
            check();
        } else if (id == R.id.btn_stop) {
            stopService(new Intent(this, VocalService.class));
            txtStart.setVisibility(View.INVISIBLE);
            btnStart.setVisibility(View.INVISIBLE);
            btnStart.setVisibility(View.VISIBLE);
            if (ActivityVocalSignal.mySong != null) {
                ActivityVocalSignal.mySong.release();
            }
            Toast.makeText(this, "Detection stopped", Toast.LENGTH_LONG).show();
        }
    }

    public void checkCamFlash() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, 223);
            return;
        }
        this.mPermCAm = Boolean.TRUE;
    }

    public void check() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.RECORD_AUDIO"}, 123);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initializePlayerAndStartRecording();
        }
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        if (i == 123) {
            if (iArr.length > 0 && iArr[0] == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    initializePlayerAndStartRecording();
                }
            }
        } else if (i == 223) {
            if (iArr.length == 0 || iArr[0] != 0) {
                this.mPermCAm = Boolean.FALSE;
            } else {
                this.mPermCAm = Boolean.TRUE;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initializePlayerAndStartRecording() {
        checkbox();
        this.classesApp.save("StopService", "0");
        startForegroundService(new Intent(getBaseContext(), VocalService.class));
        Toast.makeText(this, "Detection started", Toast.LENGTH_LONG).show();
        if (this.txtStart.getVisibility() == View.INVISIBLE) {
            this.txtStart.setVisibility(View.VISIBLE);
        }
        if (this.btnStart.getVisibility() == View.VISIBLE) {
            this.btnStart.setVisibility(View.INVISIBLE);
            btnStart.setVisibility(View.VISIBLE);
        }
        if (MainActivity.MainIsRun) {
            try {
                MainActivity.activityMain.finish();
            } catch (Exception ignored) {
            }
        }
    }
}
