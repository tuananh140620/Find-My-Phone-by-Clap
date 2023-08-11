package com.example.ClapClapToFindPhone;

import android.annotation.SuppressLint;
import android.media.AudioRecord;

public class RecorderThread extends Thread {
    private final int audioEncoding = 2;
    @SuppressLint("MissingPermission")
    private final AudioRecord audioRecord = new AudioRecord(1, this.sampleRate, this.channelConfiguration, this.audioEncoding, AudioRecord.getMinBufferSize(this.sampleRate, this.channelConfiguration, this.audioEncoding));
    byte[] buffer = new byte[this.frameByteSize];
    private final int channelConfiguration = 16;
    private final int frameByteSize = 2048;
    private boolean isRecording;
    private int rateSupported;
    private boolean rate_send;
    private final int sampleRate = getValidSampleRates();

    public int getValidSampleRates() {
        for (int i : new int[]{44100, 22050, 16000, 11025, 8000}) {
            if (AudioRecord.getMinBufferSize(i, 1, 2) > 0 && !rate_send) {
                rateSupported = i;
                rate_send = true;
            }
        }
        return this.rateSupported;
    }

    public void startRecording() {
        try {
            audioRecord.startRecording();
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            audioRecord.stop();
            audioRecord.release();
            isRecording = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        startRecording();
    }
}
