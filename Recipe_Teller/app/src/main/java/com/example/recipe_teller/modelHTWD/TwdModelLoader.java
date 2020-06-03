/*
 * Copyright (c) 2018 LG Electronics Inc.
 */
package com.example.recipe_teller.modelHTWD;

import android.content.Context;
import android.util.Log;

import com.example.recipe_teller.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
 * This class makes it easy to load various model files for demonstration of the keyword engine.
 * It is not required for actual products.
 *
 * Read the contents of the config_ [keyword] _ [language] .json file under assets / keyword_model.
 * Actual products do not need to support various keywords or languages,
 * so hardcoded contents of the json file would be better into your code,
 * or use other Android resource management frameworks, in terms of structure and administration,
 * rather than reading in file I / O.
 */

/**
 * Load and cache json model file of TWD engine.
 */
public class TwdModelLoader {
    private static final String TAG = TwdModelLoader.class.getSimpleName();

    /**
     * Json model file, [keyword][language] format
     */
    private static final String[][] MODELS = {
            {"config_airstar_kr.json", "config_airstar_en.json", "config_airstar_cn.json", "config_airstar_jp.json"},
            {"config_hilg_kr.json", "config_hilg_en.json"},
            {"config_heycloi_kr.json", "config_heycloi_en.json", "config_heycloi_cn.json", "config_heycloi_jp.json"}
    }; // [keyword][language]

    private static final String KEYWORD_MODEL_PATH = "keyword_model/";

    /**
     * Cached model information
     */
    private Map<String, TwdModel> mModelMap = new HashMap<>();

    /**
     * Loaded model information
     */
    private TwdModel mModel = null;

    private Context mContext;

    /**
     * Creates a model loader to load information of model files.
     *
     * @param context Context to access assets
     */
    public TwdModelLoader(Context context) {
        mContext = context;
    }

    /**
     * Load a model information and store inside. Use another get methods to retrieve the information after invoking this.
     *
     * @param keyword  index of keyword, Airstart:0, HiLG:1, HeyCloi:2
     * @param language index of language, KO:0, EN:1, CN:2, JP:3
     */
    public void load(int keyword, int language) {
        String key = String.format(Locale.getDefault(), "%1$d_%2$d", keyword, language);
        if (mModelMap.containsKey(key)) {
            mModel = mModelMap.get(key);
        } else {
            mModel = loadModel(keyword, language, key);
        }
        if (mModel != null) {
            Log.d(TAG, "load: " + mModel.toString());
        }
    }

    /**
     * Load a model information and store inside. Use another get methods to retrieve the information after invoking this.
     *
     * @param keyword  Value of keyword : "HILG"
     * @param language Value of language : "KOR", "ENG"
     */
    public void load(String keyword, String language) {
        load(getKeywordIndex(keyword), getLanguageIndex(language));
    }

    /**
     * Load a model information from json
     *
     * @param keyword HiLG:0
     * @param language KOR:0, ENG:1
     * @param key key for cache map
     * @return TwdModel instance
     */
    private TwdModel loadModel(int keyword, int language, String key) {
        if (keyword >= MODELS.length || keyword < 0) {
            throw new IllegalArgumentException("Keyword is not valid.");
        }
        if (language >= MODELS[keyword].length || language < 0) {
            throw new IllegalArgumentException("Language is not valid.");
        }
        if (MODELS[keyword][language] == null) {
            throw new IllegalArgumentException("Model parameters are not valid.");
        }
        String jsonFileName = KeywordLanguageMap.keywordMap[keyword] + "/" +
                KeywordLanguageMap.languageMap[language] + "/" +
                MODELS[keyword][language];

        Gson gson = new Gson();
        try {
            InputStream inputStream = mContext.getAssets().open(KEYWORD_MODEL_PATH + jsonFileName);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            TwdModel model = gson.fromJson(reader, new TypeToken<TwdModel>() {
            }.getType());
            if (model == null) {
                return null;
            }
            mModelMap.put(key, model);
            return model;
        } catch (IOException e) {
            Log.w(TAG, "loadModel: " + e.getMessage());
        }
        return null;
    }

    /**
     * Return am file path
     * @return am file path
     */
    public String getAmFilePath() {
        if (mModel == null) {
            throw new IllegalStateException("Model is not loaded.");
        }
        return mModel.amModelFile;
    }

    /**
     * Return net file path
     * @return net file path
     */
    public String getNetFilePath() {
        if (mModel == null) {
            throw new IllegalStateException("Model is not loaded.");
        }
        return mModel.netModelFile;
    }

    /**
     * Return sensitivity value that is decimal number
     * @return Sensitivity value, decimal number
     */
    public int getSensitivity() {
        if (mModel == null) {
            throw new IllegalStateException("Model is not loaded.");
        }
        return mModel.sensitivity;
    }

    /**
     * Return cm value that is floating-point number
     * @return cm value, floating-point number
     */
    public float getCm() {
        if (mModel == null) {
            throw new IllegalStateException("Model is not loaded.");
        }
        return mModel.cm;
    }

    /**
     * Return weight value that is decimal number
     * @return Weight value, decimal number
     */
    public int getWeight() {
        if (mModel == null) {
            throw new IllegalStateException("Model is not loaded.");
        }
        return mModel.weight;
    }

    public byte[] readAmAsset() {
        return readAsset(getAmFilePath());
    }

    public byte[] readNetAsset() {
        return readAsset(getNetFilePath());
    }

    private byte[] readAsset(String fileName) {
        byte[] arr = new byte[0];
        String modelKeyData = getModelKeyData();
        String fullFilePath = KeywordLanguageMap.KEYWORD_MODEL_PATH + "/"
                + getModelKeyword(modelKeyData) + "/"
                + getModelLanguage(modelKeyData) + "/" + fileName;
        try (InputStream is = mContext.getAssets().open(fullFilePath)) {
            int size = is.available();
            arr = new byte[size];
            int result = is.read(arr);

            Log.d(TAG, "readAsset: result:" + (size == result));
        } catch (IOException e) {
            Log.w(TAG, "readAsset error", e);
        }

        return arr;
    }

    private String getModelKeyword(String modelKeyData) {
        String[] keyData = modelKeyData.split("_");

        return KeywordLanguageMap.keywordMap[Integer.parseInt(keyData[0])];
    }

    private String getModelLanguage(String modelKeyData) {
        String[] keyData = modelKeyData.split("_");

        return KeywordLanguageMap.languageMap[Integer.parseInt(keyData[1])];
    }

    public String getModelKeyData() {
        if (mModel == null) {
            throw new IllegalStateException("Model is not loaded.");
        }
        for (String key : mModelMap.keySet()) {
            if (mModelMap.get(key).equals(mModel)) {
                return key;
            }
        }
        Log.e(TAG, "Model key not found. change to default value");
        return KeywordLanguageMap.DEFAULT_MODEL_KEY;
    }

    public int getLanguageIndex(String language) {
        String[] _array = mContext.getResources().getStringArray(R.array.array_language);
        int index = _array.length - 1;
        for (;index > 0 && !(_array[index].equals(language)); index--) {
        }
        return index;
    }

    public int getKeywordIndex(String keyword) {
        String[] _array = mContext.getResources().getStringArray(R.array.array_keyword);
        int index = _array.length - 1;
        for (;index > 0 && !(_array[index].equals(keyword)); index--) {
        }
        return index;
    }

}
