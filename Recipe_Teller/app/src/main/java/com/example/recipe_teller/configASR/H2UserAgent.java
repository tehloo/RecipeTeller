package com.example.recipe_teller.configASR;

public class H2UserAgent {
    public String product;

    public String pcmSource;

    public String os;

    public String model;

    public String version;

    @Override
    public String toString() {
        return "H2UserAgent{" +
                "product='" + product + '\'' +
                ", pcmSource='" + pcmSource + '\'' +
                ", os='" + os + '\'' +
                ", model='" + model + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
