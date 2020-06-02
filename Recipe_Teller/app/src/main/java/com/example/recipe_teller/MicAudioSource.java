/*
 * Copyright (c) 2018 LG Electronics Inc.
 */
package com.example.recipe_teller;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * This class guides you how to use AudioRecord to send voice data to the engine. Please use it for testing.
 */
public class MicAudioSource implements IAudioSourceASR {
    private static final String TAG = MicAudioSource.class.getSimpleName();

    /**
     * Sampling rate
     */
    private static final int SAMPLE_RATE = 16000; // Hz

    /**
     * To calculate buffer size for mic
     */
    private static final int SUB_BUFFER = 15;

    /**
     * Gain the audio resource from Mic
     */
    private AudioRecord mRecorder;

    /**
     * Creates the AudioRecord instance and start recording from it.
     *
     * @return true, if the source is available.
     */
    @Override
    public boolean prepare() {
        // Acquired Mic Resource
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                SAMPLE_RATE * SUB_BUFFER);
        mRecorder.startRecording();
        // Microphone status check required. Not available when voice input is in use by another app.
        // For example, you can not use this feature while running a voice recording app.
        if (mRecorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            Log.e(TAG, "Cannot startRecording!!");
            return false;
        }
        return true;
    }

    /**
     * Read the audio data from source and write on the buffer given by parameter.
     *
     * @param buffer byte buffer to write the audio data.
     * @return the size of buffer. See the return part of the {@link AudioRecord#read(byte[], int, int).
     */
    @Override
    public int read(byte[] buffer) {
        return mRecorder.read(buffer, 0, buffer.length);
    }

    /**
     * Read the audio data from source and write on the buffer given by parameter.
     *
     * @param buffer multiple byte array buffer to write the audio data. Mic uses 1 channel only.
     * @return the size of buffer. See the return part of the {@link AudioRecord#read(byte[], int, int).
     */
    @Override
    public int read(byte[][] buffer) {
        return mRecorder.read(buffer[0], 0, buffer[0].length);
    }

    @Override
    public int getChannel() {
        return 1;
    }

    @Override
    public void release() {
        // release mic
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }
}
