package com.example.myapplication;

public class Recipe {

    private String Recipe_title;
    private String ID;
    private int Level;
    private int[] cook_time;
    private String[] cook_context;
    private int Recipe_index_num;
    private String[] cook_tool;

    private String[] chs_INgredient_name;
    private String[] ncs_INgredient_name;
    private String[] source_INgredient_name;

    private String[] chs_INgredient_Choose;
    private String[] ncs_INgredient_Choose;
    private String[] source_INgredient_Choose;

    private int[] chs_Ingredient_num;
    private int[] ncs_Ingredient_num;
    private int[] source_Ingredient_num;

    private int Recipe_time;
    private String Recipe_IMG;
    private String[] Cook_IMG;


    public Recipe(){}


    public String getRecipe_IMG() {
        return Recipe_IMG;
    }

    public void setRecipe_IMG(String recipe_IMG) {
        Recipe_IMG = recipe_IMG;
    }

    public String[] getCook_IMG() {
        return Cook_IMG;
    }

    public void setCook_IMG(String[] cook_IMG) {
        Cook_IMG = cook_IMG;
    }






    public String getRecipe_title() {
        return Recipe_title;
    }

    public void setRecipe_title(String recipe_title) {
        Recipe_title = recipe_title;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        this.Level = level;
    }

    public int[] getCook_time() {
        return cook_time;
    }

    public void setCook_time(int[] cook_time) {
        this.cook_time = cook_time;
    }

    public String[] getCook_context() {
        return cook_context;
    }

    public void setCook_context(String[] cook_context) {
        this.cook_context = cook_context;
    }

    public int getRecipe_index_num() {
        return Recipe_index_num;
    }

    public void setRecipe_index_num(int recipe_index_num) {
        Recipe_index_num = recipe_index_num;
    }

    public String[] getCook_tool() {
        return cook_tool;
    }

    public void setCook_tool(String[] cook_tool) {
        this.cook_tool = cook_tool;
    }

    public String[] getChs_INgredient_name() {
        return chs_INgredient_name;
    }

    public void setChs_INgredient_name(String[] chs_INgredient_name) {
        this.chs_INgredient_name = chs_INgredient_name;
    }

    public String[] getNcs_INgredient_name() {
        return ncs_INgredient_name;
    }

    public void setNcs_INgredient_name(String[] ncs_INgredient_name) {
        this.ncs_INgredient_name = ncs_INgredient_name;
    }

    public String[] getSource_INgredient_name() {
        return source_INgredient_name;
    }

    public void setSource_INgredient_name(String[] source_INgredient_name) {
        this.source_INgredient_name = source_INgredient_name;
    }

    public String[] getChs_INgredient_Choose() {
        return chs_INgredient_Choose;
    }

    public void setChs_INgredient_Choose(String[] chs_INgredient_Choose) {
        this.chs_INgredient_Choose = chs_INgredient_Choose;
    }

    public String[] getNcs_INgredient_Choose() {
        return ncs_INgredient_Choose;
    }

    public void setNcs_INgredient_Choose(String[] ncs_INgredient_Choose) {
        this.ncs_INgredient_Choose = ncs_INgredient_Choose;
    }

    public String[] getSource_INgredient_Choose() {
        return source_INgredient_Choose;
    }

    public void setSource_INgredient_Choose(String[] source_INgredient_Choose) {
        this.source_INgredient_Choose = source_INgredient_Choose;
    }

    public int[] getChs_Ingredient_num() {
        return chs_Ingredient_num;
    }

    public void setChs_Ingredient_num(int[] chs_Ingredient_num) {
        this.chs_Ingredient_num = chs_Ingredient_num;
    }

    public int[] getNcs_Ingredient_num() {
        return ncs_Ingredient_num;
    }

    public void setNcs_Ingredient_num(int[] ncs_Ingredient_num) {
        this.ncs_Ingredient_num = ncs_Ingredient_num;
    }

    public int[] getSource_Ingredient_num() {
        return source_Ingredient_num;
    }

    public void setSource_Ingredient_num(int[] source_Ingredient_num) {
        this.source_Ingredient_num = source_Ingredient_num;
    }

    public int getRecipe_time() {
        return Recipe_time;
    }

    public void setRecipe_time(int recipe_time) {
        Recipe_time = recipe_time;
    }



}
