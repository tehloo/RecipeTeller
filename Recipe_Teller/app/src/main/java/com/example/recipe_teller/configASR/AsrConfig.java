/*
 * Copyright (c) 2018 LG Electronics Inc.
 */
package com.example.recipe_teller.configASR;

/**
 * ASR engine json config container, used for gson serialization
 * <p>
 * This class is unnecessary if you create a config string in json format in a different way
 * without using the gson library.
 * For reference server
 */
public class AsrConfig {

    public String language;

    public String serverIp;

    public String serverPort;

    public String applicationName;

    public String deviceId;

    public String customKey;

    public String recognitionMode;

    public UserAgent userAgent;

    public int epdSkipBytes;

    public boolean enableCompleteMode;

    public boolean enableTriggerWordReject;

    public boolean enableServerPcmDump;

    public boolean enableTwdResult;

    public String encryptionKey;

    public boolean enablePcmDump;

    public String pcmDumpPath;

    public int channels;

    public static class UserAgent {
        public String os;

        public String model;

        public String pcmSource;
    }

}
