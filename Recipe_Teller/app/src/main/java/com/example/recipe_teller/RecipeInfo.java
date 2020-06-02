package com.example.recipe_teller;

import android.widget.ImageView;

public class RecipeInfo {
    private String recipeName;

    public RecipeInfo(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }
}
