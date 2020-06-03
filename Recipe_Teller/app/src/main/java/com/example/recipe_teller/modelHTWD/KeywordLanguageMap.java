package com.example.recipe_teller.modelHTWD;

import com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineAPI;

public final class KeywordLanguageMap {

    public static final String KEYWORD_MODEL_PATH = "keyword_model";

    public static final String DEFAULT_MODEL_KEY = AI_HybridTWDEngineAPI.AI_VA_KEYWORD_HI_LG +
            "_" + AI_HybridTWDEngineAPI.AI_LANG_KO_KR;

    public static final String[] keywordMap = {"airstar", "hilg", "heycloi"};

    public static final String[] languageMap = {"kr", "en", "cn", "jp"};

}
