package com.example.recipe_teller.configASR;

public class H2Nlp {
    public boolean support = false;

    public boolean verbose = false;

    public int version = 1;

    public NlpData data;

    static class NlpData {

        public String function;

        public String language;

        public String country;

        public String deviceId;

        public String timeZone;

        public String deviceTime;

        public String inputType;

        public Config config;

        public static class Config {
            public String nlpVersion;
        }
    }

    @Override
    public String toString() {
        return "H2Nlp{" +
                "support=" + support +
                ", verbose=" + verbose +
                ", version=" + version +
                ", data=" + (data != null? data.toString() : "null") +
                '}';
    }
}
