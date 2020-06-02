/*
 * Copyright (c) 2019 LG Electronics Inc.
 */
package com.example.recipe_teller;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Loads configs from a file.
 */
class ConfigLoaderTTS {
    private static final String TAG = ConfigLoaderTTS.class.getSimpleName();

    private static final String FILENAME_ASSET_CONFIG_JSON = "config.json";

    static final String KEY_USE_EXTERNAL = "use_external";
    static final String KEY_EXTERNAL_PATH = "external_path";
    static final String KEY_SHOW_CONFIG = "show_config";

    private Context mContext;
    private SharedPreferences mPref;
    private String mAssetFileName = FILENAME_ASSET_CONFIG_JSON;

    /**
     * Constructor for ConfigLoader. Use assets/config.json as the default config file.
     *
     * @param context Context
     */
    ConfigLoaderTTS(Context context) {
        mContext = context;
        mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /**
     * Constructor for ConfigLoader. Use assets/[assetFileName] as the default config file.
     *
     * @param context Context
     */
    ConfigLoaderTTS(Context context, String assetFileName) {
        mContext = context;
        mAssetFileName = assetFileName;
        mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /**
     * According to the preferences, create JsonReader instance.
     *
     * @return JsonReader instance to read the json config file.
     */
    public <T> T loadConfig(Type configClass) {
        try (InputStream stream = getInputStream();
             InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             JsonReader reader = new JsonReader(streamReader)) {
            return new Gson().fromJson(reader, configClass);
        } catch (JsonSyntaxException | IOException e) {
            Log.e(TAG, "loadConfig: " + e.getMessage());
            return null;
        }
    }

    /**
     * According to the preferences, create InputStream instance.
     *
     * @return InputStream instance to read the json config file.
     */
    public InputStream getInputStream() {
        InputStream stream = null;
        try {
            if (isUsingExternal()) {
                String path = mPref.getString(KEY_EXTERNAL_PATH, "");
                if (!"".equals(path)) {
                    Uri uri = Uri.parse(path);
                    try {
                        stream = mContext.getContentResolver().openInputStream(uri);
                        return stream;
                    } catch (SecurityException se) {
                        Toast.makeText(mContext, R.string.popup_msg_config_error, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "SecurityException: file permission problem - " + se.getMessage());
                    }
                }
            }
            stream = mContext.getAssets().open(mAssetFileName);
        } catch (IOException ioe) {
            Log.e(TAG, "initEngine: config file is not valid");
        }
        return stream;
    }

    /**
     * Read the contents of the json file as a String.
     *
     * @return the contents of the json.
     */
    String getJsonConfigString() {
        StringBuilder config = new StringBuilder();
        try (InputStream stream = getInputStream()) {
            Scanner scn = new Scanner(stream, StandardCharsets.UTF_8.name());
            while (scn.hasNext()) {
                config.append(scn.next());
            }
        } catch (IOException e) {
            Log.e(TAG, "getJsonConfigString: " + e.getMessage());
        }

        return config.toString();
    }

    /**
     * Gets the path to the json file.
     *
     * @param defaultString for path
     * @return the path to the json file.
     */
    String getExternalPath(String defaultString) {
        return mPref.getString(KEY_EXTERNAL_PATH, defaultString);
    }

    /**
     * Records the external path into the preferences.
     * @param path selected external path.
     */
    void putExternalPath(String path) {
        mPref.edit().putString(ConfigLoaderTTS.KEY_EXTERNAL_PATH, path).apply();
    }

    /**
     * Whether an external path is used.
     * @return true if the app using the external path.
     */
    boolean isUsingExternal() {
        return mPref.getBoolean(KEY_USE_EXTERNAL, false);
    }

    /**
     * Records whether an external path is used.
     * @param usingExternal true if the app using the external path.
     */
    void setUsingExternal(boolean usingExternal) {
        mPref.edit().putBoolean(KEY_USE_EXTERNAL, usingExternal).apply();
    }
}
