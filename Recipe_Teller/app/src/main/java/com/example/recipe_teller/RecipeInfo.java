package com.example.recipe_teller;

import android.widget.ImageView;

public class RecipeInfo {
    private String recipeName;
    private String documentName;

    public RecipeInfo(String recipeName, String documentName) {
        this.recipeName = recipeName;
        this.documentName = documentName;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}
