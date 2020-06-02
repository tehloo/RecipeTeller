/*
 * Copyright (c) 2018 LG Electronics Inc.
 */
package com.example.recipe_teller.configASR;

import com.google.gson.annotations.SerializedName;

/**
 * Apply the changed NLP linking protocol to use the reference server for structuring.
 */
public class SpeechConfig {
    public SpeechConfig(AsrConfig asrConfig, NlpConfig nlpConfig) {
        this.asrConfig = asrConfig;
        this.nlpConfig = nlpConfig;
    }

    public String opMode;

    // The asrConfig and nlpConfig are used to work with servers using the socket.
    // For the legacy DeepThinQ Reference servers.
    @SerializedName("asr_config")
    public AsrConfig asrConfig;

    /**
     * In case of ASR-NLP mode, set NLP Config.
     */
    @SerializedName("nlp_config")
    public NlpConfig nlpConfig;


    ////////////////////////////////////////////////////////////////////////////////
    // IMPORTANT!
    // The following configs are used to work with servers using the HTTP2 standard.
    public boolean enableHttp2 = false;

    public String serverIp;

    public int serverPort;

    public String voicePath;

    public String controlPath;

    public String authTokenPath;

    public String appName;

    public String authToken;

    public String customKey;

    public String deviceID;

    public String deviceType;

    public String locale;

    public String recognitionMode;

    public String userID;

    public boolean enablePcmDump;

    public String pcmDumpPath;

    public H2Asr asr;

    public H2Nlp nlp;

    public H2UserAgent userAgent;

    @Override
    public String toString() {
        return "SpeechConfig{" +
                "opMode='" + opMode + '\'' +
                ", asrConfig=" + asrConfig +
                ", nlpConfig=" + nlpConfig +
                ", enableHttp2=" + enableHttp2 +
                ", serverIp='" + serverIp + '\'' +
                ", serverPort=" + serverPort +
                ", voicePath='" + voicePath + '\'' +
                ", controlPath='" + controlPath + '\'' +
                ", authTokenPath='" + authTokenPath + '\'' +
                ", appName='" + appName + '\'' +
                ", authToken='" + authToken + '\'' +
                ", customKey='" + customKey + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", locale='" + locale + '\'' +
                ", recognitionMode='" + recognitionMode + '\'' +
                ", userID='" + userID + '\'' +
                ", asr=" + (asr != null? asr.toString() : "null") +
                ", nlp=" + (nlp != null? nlp.toString() : "null") +
                ", userAgent=" + (userAgent != null? userAgent.toString() : "null")+
                '}';
    }
}
