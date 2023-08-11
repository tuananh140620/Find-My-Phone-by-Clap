package com.example.ClapClapToFindPhone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends Activity implements View.OnClickListener{
    public static Activity mainSetting;
    private ImageView btnStart;
    private CheckBox checkBoxSound;
    private ImageView stopLinear;
    private CheckBox checkBoxVibrate;
    private ImageView buttonStop;
    private ClassesApp classesApp;
    private int intOnTick;
    public Boolean mPermCAm;
    private SeekBar seekBar;
    private LinearLayout txtStart;
    private ImageView vocalButton;
    private CheckBox checkBoxFlash;
    public static boolean MainIsRun;
    public static Activity activityMain;

    public MainActivity() {
    }
    @SuppressLint({"WrongConstant", "CutPasteId", "MissingInflatedId"})
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        stopLinear = (ImageView) findViewById(R.id.btn_start_service);
        btnStart = (ImageView) findViewById(R.id.btn_start_service);
        txtStart = (LinearLayout) findViewById(R.id.txt_feild);
        ImageView stopLinear = (ImageView) findViewById(R.id.btn_start_service);
        checkBoxFlash = (CheckBox) findViewById(R.id.flashbox);
        checkBoxVibrate = (CheckBox) findViewById(R.id.vibratebox);
        checkBoxSound = (CheckBox) findViewById(R.id.soundbox);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        vocalButton = (ImageView) findViewById(R.id.btn_start_service);
        buttonStop = (ImageView) findViewById(R.id.btn_stop);

        isMyServiceRunning();

        if (isMyServiceRunning()) {
            txtStart.setVisibility(View.VISIBLE);
            stopLinear.setVisibility(View.VISIBLE);
        }
        checkBoxFlash.setChecked(false);
        checkBoxVibrate.setChecked(false);
        checkBoxSound.setChecked(false);
        classesApp = new ClassesApp(this);

//        final Window win = getWindow();
//        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
//        mCtx = this;
//        boolean deviceHasCameraFlash = getPackageManager().hasSystemFeature("android.hardware.camera.flash");
//        initialize();
//        ClassesApp classesApp = new ClassesApp(mCtx);
//        classesApp.save("StopService", "1");

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

//    public void onCreate(Bundle bundle) {
//        super.onCreate(bundle);
//        setContentView(R.layout.activity_main);
//
//        activityMain = this;
//
//        MainIsRun = true;
////        start = (ImageView) findViewById(R.id.setting);
//        classesApp = new ClassesApp(this);
//        isMyServiceRunning(VocalService.class);
////        start.setOnClickListener(new OnClickListener() {
////            public void onClick(View view) {
////                    Intent intent = new Intent(MainActivity.this, ActivitySetting.class);
////                    intent.setFlags(268435456);
////                    MainActivity.this.startActivity(intent);
////            }
////        });
//
//    }
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
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("");
                stringBuilder.append(i);
                MainActivity.this.classesApp.save("seekBar", stringBuilder.toString());
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
        } else if (this.mPermCAm.booleanValue()) {
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
    private void initvolume() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.getStreamVolume(3);
        audioManager.setStreamVolume(3, (int) (((float) audioManager.getStreamMaxVolume(3)) * (Float.parseFloat(this.classesApp.read(NotificationCompat.CATEGORY_PROGRESS, "50")) / 100.0f)), 0);
    }

    private void setvolume(int i) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(3, (int) (((float) audioManager.getStreamMaxVolume(3)) * (((float) i) / 100.0f)), 0);
    }

    private void initialization() {
        vocalButton.setOnClickListener((OnClickListener) this);
        buttonStop.setOnClickListener((OnClickListener) this);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_start_service) {
            check();
        } else if (id == R.id.stop) {
            stopService(new Intent(this, VocalService.class));
            txtStart.setVisibility(View.INVISIBLE);
            stopLinear.setVisibility(View.INVISIBLE);
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
        this.mPermCAm = Boolean.valueOf(true);
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
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 123) {
            if (iArr.length > 0 && iArr[0] == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    initializePlayerAndStartRecording();
                }
            }
        } else if (i == 223) {
            if (iArr.length <= 0 || iArr[0] != 0) {
                this.mPermCAm = Boolean.valueOf(false);
            } else {
                this.mPermCAm = Boolean.valueOf(true);
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
            stopLinear.setVisibility(View.VISIBLE);
        }
        if (MainActivity.MainIsRun) {
            try {
                MainActivity.activityMain.finish();
            } catch (Exception unused) {
            }
        }
    }
}
