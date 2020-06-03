/*
 * Copyright (c) 2018 LG Electronics Inc.
 */
package com.example.recipe_teller;

import android.content.Context;
import android.util.Log;

import com.lge.aip.engine.base.AudioInputData;
import com.lge.aip.engine.base.IEngineListener;
import com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineAPI;
import com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineListener;

/**
 * This class is seperated from Activity to manage AI Engine.
 * We made this class to solve the problem of difficult analysis.
 */
class TriggerWordDetectionManager {
    private static final String TAG = TriggerWordDetectionManager.class.getSimpleName();

    /**
     * Flag for Thread termination processing
     */
    private boolean mEnded = false;

    /**
     * Thread for voice processing
     */
    private Thread mThread;

    /**
     * TWD Engine, JNI
     */
    private AI_HybridTWDEngineAPI mHtwdEngine;

    /**
     * The size of buffer delivered to engine
     */
    private static final int BUFFER_SIZE = 800;

    /**
     * Listener to return result to Activity after Callback
     */
    private UpdateResultListener mUpdateResultListener;

    /**
     * Set status of server operation
     */
    private boolean enableASR = true;

    /**
     * The constructor of TriggerWordDetectionManager.
     * It receives an UpdateResultListener to display the results on the screen.
     *
     * @param updateResultListener Used to display the result on the screen
     */
    TriggerWordDetectionManager(UpdateResultListener updateResultListener) {
        mUpdateResultListener = updateResultListener;
    }

    /**
     * Create engine
     */
    void create(Context context) {
        mHtwdEngine = new AI_HybridTWDEngineAPI(context);
        mHtwdEngine.create();
    }

    /**
     * Configure engine with Json config
     *
     * @param jsonConfig json string
     */
    void configure(String jsonConfig) {
        mHtwdEngine.configure(jsonConfig);
        mHtwdEngine.setListener(mTwdListener);
    }

    /**
     * Set model data with byte array. Do not set amModelFile and netModelFile on config json, if want
     * to use this method.
     *
     * @param am byte array of am file
     * @param net byte array of net file
     */
    void injectModels(byte[] am, byte[] net) {
        mHtwdEngine.injectModels(am, net);
    }

    void enableHybrid(boolean enableASR) {
        this.enableASR = enableASR;
    }

    /**
     * Start engine using the specified audio source
     *
     * @param audioSource Implementation that implements IAudioSource
     */
    void startListening(final IAudioSourceHTWD audioSource) {
        Log.d(TAG, "startListening");

        // Condition for Thread termination
        mEnded = false;
        // Engine start, engine must be started and must be initialized by create in advance
        mHtwdEngine.start();

        // Sound data reading thread
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!audioSource.prepare()) {
                    audioSource.release();
                    return;
                }
                byte[] buffer = new byte[BUFFER_SIZE];
                while (audioSource.read(buffer) == BUFFER_SIZE && !mEnded) {
                    AudioInputData audioInput = new AudioInputData(buffer);

                    // Deliver data to the engine
                    mHtwdEngine.process(audioInput, null);

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "startListening:InterruptedException: " + e.getMessage());
                    }
                }

                audioSource.release();
                Log.d(TAG, "startListening : loop end.");
            }
        });
        mThread.start();
    }

    /**
     * Stop engine and voice input thread.
     */
    void stopListening() {
        // Stop thread
        mEnded = true;
        Log.d(TAG, "Twd Engine: stop");
        try {
            if (mThread != null) {
                mThread.join();
            }
        } catch (InterruptedException e) {
            Log.w(TAG, "stopListening:InterruptedException", e);
        }
        // Stop engine
        if (mHtwdEngine != null) {
            mHtwdEngine.stop();
        }
    }

    /**
     * Invoked when the engine is destroyed. Call when activity ends.
     */
    void destroy() {
        if (mHtwdEngine != null) {
            Log.d(TAG, "Twd Engine: destroy");

            // Destroy engine when user finish app
            mHtwdEngine.destroy();
            mHtwdEngine = null;
        }
    }

    /**
     * Listener for receiving results from the engine.
     */
    private IEngineListener mTwdListener = new AI_HybridTWDEngineListener() {
        @Override
        public void onDetected(final int id, final int length, String keyword) {
            // This is the result through the native engine without a server.
            // It is always received before the onResults Callback.
            // If you only use server results, there is nothing to handle in this callback.
            Log.e("HTWD", "HILG Dectected!");
            mUpdateResultListener.updateResult(keyword, true, false, !enableASR);
            if (!enableASR) {
                stopListening();
            }
        }

        @Override
        public void onError(int error, String errorText) {
            Log.d(TAG, "onError: " + error + " " + errorText);
            // Stop the engine
            mUpdateResultListener.updateResult(errorText + " [" + error + "]", false, false, true);
            stopListening();
        }

        @Override
        public void onResults(final boolean success, final String text) {
            // Receiving results through the server. The success parameter determines success.
            // Stop the engine
            mUpdateResultListener.updateResult(text, success, true, true);
            stopListening();
            Log.d(TAG, "onResults: " + text);
        }
    };

    interface UpdateResultListener {
        /**
         * Update the results in a text view to display the results.
         *
         * @param str      String to update
         * @param detected Detected or not
         */
        void updateResult(String str, boolean detected, boolean fromServer, boolean stopped);
    }
}
