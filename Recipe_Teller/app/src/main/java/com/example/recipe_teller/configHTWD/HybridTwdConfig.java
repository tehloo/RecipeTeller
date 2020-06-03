/*
 * Copyright (c) 2018 LG Electronics Inc.
 */
package com.example.recipe_teller.configHTWD;

import com.google.gson.annotations.SerializedName;

/**
 * This class used for Gson serialization from Json config to use with TWD engine.
 * If you don't use Gson library, this class is unnecessary.
 */
public class HybridTwdConfig {
    /**
     * Set server operation status.
     */
    public boolean enableHybrid = true;

    /**
     * Sets the polling cycle value for data delay processing. (in milliseconds)
     * The engine periodically requests data, which takes a delay of this value.
     * <p>
     * Decreasing the value increases the CPU load because the Loop is called frequently,
     * and increasing the value can slow down the load instead of reducing the load.
     * <p>
     * When tested in V30 and V20,
     * we determined that the load was not large and that the results were unchanged.
     * <p>
     * Please make adjustments through testing as there may be differences depending on system performance.
     */
    public long bufferPollingCycle = 10L;

    /**
     * Operates the buffer for Hybrid speech recognition inside the engine.
     * You can specify the size of this buffer in seconds.
     * <p>
     * The internal buffer size is determined like below
     * 60 secs for pcm data (32000Bytes per 1sec = 16bits * 16000Hz) * 60 = 1920000
     */
    public int internalBufferLength = 60;

    /**
     * Keyword language setting, refer to AI_TWDEngineAPI constants
     *
     * @see com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineAPI
     */
    public String language;

    @SerializedName("embedded_config")
    public EmbeddedConfig embeddedConfig;

    public static class EmbeddedConfig {
        /**
         * Set type of keyword, AI_VA_KEYWORD_AIR_STAR, AI_VA_KEYWORD_HI_LG
         *
         * @see com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineAPI
         */
        public String triggerWord;

        /**
         * Set Sensitivity value given in advance, Sensitivity adjustment
         */
        public int sensitivity;

        /**
         * Set cm value given in advance
         */
        public float cm;

        /**
         * Set weight value given in advance
         */
        public int weight;

        /**
         * Set Threshold of engine recognition performance
         */
        public int noiseThreshold;

        /**
         * AM file path to use for keyword detection
         */
        public String amModelFile;

        /**
         * NET file path to use for keyword detection
         */
        public String netModelFile;

        /**
         * Flag for dump audio
         */
        public boolean enablePcmDump = false;

        /**
         * Debugging information on / off function
         */
        public boolean enableDebug = false;

        /**
         * Flag for dump audio
         */
        public String pcmDumpPath = "";
    }

    @SerializedName("server_config")
    public ServerConfig serverConfig;

    public static class ServerConfig {
        /**
         * The pre-issued server ip
         */
        public String serverIp;

        /**
         * The pre-issued server port number
         */
        public String serverPort;

        /**
         * The pre-issued app name
         */
        public String applicationName;

        /**
         * unique id for the device
         */
        public String deviceId;

        /**
         * The pre-issued API key
         */
        public String customKey;

        /**
         * Flag for dump on server
         */
        public boolean enableServerPcmDump = false;

        /**
         * Deliver model information to differentiate your device model
         */
        public String modelName;

        /**
         * Key value used for AES encryption of ASR input data (note that it is not exposed to the outside!)
         */
        public String encryptionKey;

        /**
         * Flag for connection(socket/http2) on server
         */
        public boolean enableHttp2;
    }
}
