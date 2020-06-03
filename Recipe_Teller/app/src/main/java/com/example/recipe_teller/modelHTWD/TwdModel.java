/*
 * Copyright (c) 2018 LG Electronics Inc.
 */
package com.example.recipe_teller.modelHTWD;

/*
 * This class makes it easy to load various model files for demonstration of the startup engine.
 * It is not required for actual products.
 */
/**
 * Model information are de-serialized from json files in asset/keyword_model for TWD config using Gson.
 */
public class TwdModel {
    /**
     * am file path
     */
    String amModelFile;
    /**
     * net file path
     */
    String netModelFile;
    /**
     * Sensitivity value, decimal number
     */
    int sensitivity;

    /**
     * cm value, floating-point number
     */
    float cm;
    /**
     * Weight value, decimal number
     */
    int weight;

    @Override
    public String toString() {
        return "TwdModel{" +
                "amModelFile='" + amModelFile + '\'' +
                ", netModelFile='" + netModelFile + '\'' +
                ", sensitivity=" + sensitivity +
                ", cm=" + cm +
                ", weight=" + weight +
                '}';
    }

}
