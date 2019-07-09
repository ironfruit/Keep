package com.irondigitalmedia.keep.Model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Ingredient {

    public String uid;
    public String ingredient;

    public Ingredient() {
    }

    public Ingredient(String uid, String ingredient) {
        this.uid = uid;
        this.ingredient = ingredient;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }
}
