package com.example.ClapClapToFindPhone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioRecord;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioFormat;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector;


public class DetectClapClap implements OnsetHandler {

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


    @SuppressLint("MissingPermission")
    DetectClapClap(Context context) {
        classesApp = new ClassesApp(context);
        SAMPLE_RATE = getValidSampleRates();
        mContext = context;
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, 16, 2);
        buffer = new byte[minBufferSize];
        recorder = new AudioRecord(1, SAMPLE_RATE, 16, 2, minBufferSize);
        mPercussionOnsetDetector = new PercussionOnsetDetector((float) SAMPLE_RATE, minBufferSize / 2, this, 24.0d, 5.0d);
        clap = 0;
        mIsRecording = true;
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
            Intent intent = new Intent(mContext, ActivityVocalSignal.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            mContext.stopService(new Intent(mContext, VocalService.class));
        }
    }

    public void listen() {
        recorder.startRecording();
        torsosFormat = new AudioFormat((float) SAMPLE_RATE, 16, 1, true, false);
        new Thread(new Runnable() {
            public void run() {
                while (DetectClapClap.this.mIsRecording) {
                    AudioEvent audioEvent = new AudioEvent(DetectClapClap.this.torsosFormat,
                            DetectClapClap.this.recorder.read(DetectClapClap.this.buffer, 0, DetectClapClap.this.buffer.length));
                    audioEvent.setFloatBufferWithByteBuffer(DetectClapClap.this.buffer);
                    DetectClapClap.this.mPercussionOnsetDetector.process(audioEvent);
                }
                DetectClapClap.this.recorder.stop();
            }
        }).start();
    }
}
