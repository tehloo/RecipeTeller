package com.example.recipe_teller.configASR;

public class H2Asr {
    public boolean support = false;

    public boolean verbose = false;

    public int version = 1;

    public AsrData data;

    static class AsrData {
        public int channels;

        public String codec;

        public String engine;

        public String epd;

        public boolean epd_eng_off;

        public int epd_max_pause;

        public int epd_skip_bytes;

        public boolean keyword_reject;

        public String language;

        public int n_best;

        public boolean need_kwd_result;

        public String nr;

        public boolean partial;

        public boolean pcm_dump;

        public int rate;

        public String triggerWord;

        public boolean developerOption;

        @Override
        public String toString() {
            return "AsrData{" +
                    "channels='" + channels + '\'' +
                    ", codec='" + codec + '\'' +
                    ", epd='" + epd + '\'' +
                    ", epd_eng_off=" + epd_eng_off +
                    ", epd_max_pause=" + epd_max_pause +
                    ", epd_skip_bytes=" + epd_skip_bytes +
                    ", keyword_reject=" + keyword_reject +
                    ", language='" + language + '\'' +
                    ", n_best=" + n_best +
                    ", need_kwd_result=" + need_kwd_result +
                    ", nr='" + nr + '\'' +
                    ", partial=" + partial +
                    ", pcm_dump=" + pcm_dump +
                    ", rate=" + rate +
                    ", triggerWord='" + triggerWord + '\'' +
                    ", developerOption=" + developerOption +
                    '}';
        }
    }

    // The following configs are for Android only.
    public String encryptionKey;

    @Override
    public String toString() {
        return "H2Asr{" +
                "support=" + support +
                ", verbose=" + verbose +
                ", version=" + version +
                ", data=" + (data != null? data.toString() : "null") +
                ", encryptionKey='" + encryptionKey + '\'' +
                '}';
    }
}
