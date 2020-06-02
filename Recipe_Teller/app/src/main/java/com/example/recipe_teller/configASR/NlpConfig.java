/*
 * Copyright (c) 2018 LG Electronics Inc.
 */
package com.example.recipe_teller.configASR;

/**
 * For reference server
 */
public class NlpConfig {

    String function;

    String language;

    String country;

    String inputType;

    String deviceId;

    String deviceTime;

    String timeZone;

    Config config;

    public NlpConfig(String function, String language,
                     String country, String inputType, String inputText, String deviceId,
                     String deviceTime, String timeZone, String nlpVersion) {

        this.config = new Config(nlpVersion);
        this.function = function;
        this.language = language;
        this.country = country;
        this.inputType = inputType;
        this.deviceId = deviceId;
        this.deviceTime = deviceTime;
        this.timeZone = timeZone;
    }

    public void setDeviceTime(Long currentTime) {
        this.deviceTime = Long.toString(currentTime);
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    class Config {
        String nlpVersion;
        Config(String version) {
            this.nlpVersion = version;
        }
    }
}
