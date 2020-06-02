package com.example.recipe_teller;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.lge.aip.engine.base.AudioInputData;
import com.lge.aip.engine.speech.AI_ASREngineAPI;
import com.lge.aip.engine.speech.AI_ASREngineListener;
import com.lge.aip.engine.speech.ResultBundleKey;
import com.lge.aip.engine.speech.config.Settings;

import java.io.File;
import java.io.FileOutputStream;

import static com.lge.aip.engine.base.AIEngineReturn.LGAI_ASR_ERROR_INVALID_ARGUMENT;
import static com.lge.aip.engine.base.AIEngineReturn.LGAI_ASR_SUCCESS;

/**
 * This class is seperated from Activity to manage AI Engine.
 * We made this class to solve the problem of difficult analysis.
 */
public class AsrManager {
    private static final String TAG = AsrManager.class.getSimpleName();

    /**
     * Flag for Thread termination processing
     */
    private boolean mEnded = false;

    /**
     * Thread for voice processing
     */
    private Thread mThread;

    /**
     * ASR + NLP Engine
     */
    private AI_ASREngineAPI mAsrEngine;

    /**
     * The size of buffer delivered to engine
     */
    private static final int BUFFER_SIZE = 800;

    /**
     * Listener to return result to Activity after Callback
     */
    private UpdateResultListener mUpdateResultListener;

    /**
     * Handler for processing event
     */
    private Handler sHandler = new Handler();

    AsrManager(Context context, UpdateResultListener updateResultListener) {
        mUpdateResultListener = updateResultListener;
        mAsrEngine = new AI_ASREngineAPI(context);
    }

    /**
     * Creates engine
     */
    void create() {
        mAsrEngine.create();
    }

    /**
     * Configures engine by json string
     * @param jsonConfig json string
     */
    void configure(String jsonConfig) {
        mAsrEngine.configure(jsonConfig);
        mAsrEngine.setListener(mAsrListener);
    }

    /**
     * Drives the engine using the specified audio source
     *
     * When audio data is transmitted in ASR mode,
     * the implementation conforming to IAudioSource is passed as an argument.
     * If the nlp signal mode or text mode does not require an audio source, pass it as null.
     *
     * @param audioSource Implementation that implements IAudioSource
     */
    int startListening(final IAudioSourceASR audioSource) {
        // If there is no audio source, terminate without listening thread operation.
        // Corresponds to NLP mode.
        if (audioSource == null) {
            return LGAI_ASR_ERROR_INVALID_ARGUMENT;
        }
        // Engine start, engine must be started and must be initialized by create in advance
        int ret = mAsrEngine.start();
        if (ret != LGAI_ASR_SUCCESS) {
            return ret;
        }

        Log.d(TAG, "startListening");
        // Thread end condition
        mEnded = false;
        // Sound data reading thread
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!audioSource.prepare()) {
                    audioSource.release();
                    return;
                }

                byte[][] buffer = new byte[audioSource.getChannel()][BUFFER_SIZE];
                while (audioSource.read(buffer) == BUFFER_SIZE && !mEnded) {
                    AudioInputData audioData = new AudioInputData(buffer);

                    // Delivering data to the engine
                    mAsrEngine.process(audioData, null);

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "startListening:InterruptedException: " + e.getMessage());
                    }
                }

                audioSource.release();
                Log.d(TAG, "startListening : loop end.");
            }
        });
        mThread.start();
        return LGAI_ASR_SUCCESS;
    }

    /**
     * Invoked when the engine is no longer in use.
     * Call it mainly when Activity finish
     */
    void destroy() {
        if (mAsrEngine != null) {
            Log.d(TAG, "Engine: destroy");

            // Destroy the engine when exiting the app.
            mAsrEngine.destroy();
        }
    }

    /**
     * Listener to receive results from ASR/NLP engine
     */
    private AI_ASREngineListener mAsrListener = new AI_ASREngineListener() {
        /**
         * Speech engine ready state callback
         */
        @Override
        public void onReadyForSpeech() {
            Log.v(TAG, "onReadyForSpeech");
        }

        /**
         * Speech engine speech recognition start point detection callback
         */
        @Override
        public void onBeginningOfSpeech() {
            Log.v(TAG, "onBeginningOfSpeech");
        }

        /**
         * Speech engine speech recognition endpoint detection callback
         */
        @Override
        public void onEndOfSpeech() {
            Log.v(TAG, "onEndOfSpeech");
        }

        /**
         * Speech engine result callback
         * @param results Receive result as true/false to {@link Settings#RESULT_BOOLEAN} from Bundle object
         * Receive result text with {@link ResultBundleKey#RESULT_STRING} as a key
         */
        @Override
        public void onResults(final Bundle results) {

            mEnded = results.getBoolean(ResultBundleKey.RESULT_BOOLEAN);
            sHandler.post(new Runnable() {
                public void run() {
                    String resultString = results.getString(ResultBundleKey.RESULT_STRING);
                    Log.e(TAG, "onResults:" + resultString);
                    if (mEnded) {
                        mUpdateResultListener.updateResult("onResults:" + resultString);
                        stopListening();
                    } else {
                        mUpdateResultListener.updateKeyword(resultString);
                    }
                }
            });
        }

        /**
         * Speech engine ASR result callback
         * @param results Receive result text with {@link ResultBundleKey#RESULT_STRING} as a key from Bundle object
         */
        @Override
        public void onAsrResults(final Bundle results) {
            Log.e("ASRManager", "onASRResults: called");
            sHandler.post(new Runnable() {
                public void run() {
                    String resultString = results.getString(ResultBundleKey.RESULT_STRING);
                    mUpdateResultListener.updateResult("\nonAsrResults: " + resultString);
                    Log.e("ASRManager", "onASRResults:" + resultString);
                }
            });
        }

        /**
         * Keyword detection result callback via Speech engine
         * @param results Receive result text with {@link ResultBundleKey#RESULT_STRING} as a key from Bundle object
         */
        @Override
        public void onKeywordResults(final Bundle results) {
            sHandler.post(new Runnable() {
                public void run() {
                    String resultString = results.getString(ResultBundleKey.RESULT_STRING);
                    mUpdateResultListener.updateResult("onKeywordResults: " + resultString);
                    Log.d(TAG, "onKeywordResults:" + resultString);
                }
            });
        }

        /**
         * Speech engine result error callback
         * @param error Result of error code
         * @param errorText Result of error text
         */
        @Override
        public void onError(int error, final String errorText) {
            Log.v(TAG, "onError code=" + error + ": " + errorText);
            sHandler.post(new Runnable() {
                public void run() {
                    mUpdateResultListener.updateResult("onError: " + errorText);
                    stopListening();
                }
            });
        }

        /**
         * Speech engine voice recognition audio level callback
         * @param rmsdB The value for the size of the input voice
         */
        @Override
        public void onRmsChanged(final float rmsdB) {
            Log.d(TAG, "onRmsChanged...(" + rmsdB + ")");
            sHandler.post(new Runnable() {
                public void run() {
                    // This sample does not handle UI processing.
                    if (!mEnded) {
                        return;
                    }

                    if (rmsdB >= 88) {
                    } else {
                    }
                }
            });
        }

        /**
         * Callback for storing voice data
         * @param data Voice data
         */
        @Override
        public void onBufferReceived(byte[] data) {
            Log.d(TAG, "onBufferReceived...");
            String dirPath = Environment.getExternalStorageDirectory().getPath() + "/Download";
            File file = new File(dirPath);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.d(TAG, "Failed to create a directory.");
                }
            }

            try {
                FileOutputStream pcmFile = new FileOutputStream(dirPath + "/dumpPcm.pcm", true);
                pcmFile.write(data);
                pcmFile.close();
            } catch (RuntimeException re) {
                System.out.println("[Runtime Exception] Check FileOutputStream");
            } catch (Exception e) {
                System.out.println("[danby] dump error");
            }
        }
    };

    /**
     * Stop engine and voice input thread.
     */
    void stopListening() {
        mAsrEngine.stop();
        mEnded = true;

        if (mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "stopListening: ", e);
            }
        }
    }

    public interface UpdateResultListener {
        /**
         * Update the results in a text view to display the results.
         *
         * @param str      String to update.
         */
        void updateResult(String str);

        void updateKeyword(String str);
    }
}
