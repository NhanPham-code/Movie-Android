package com.example.ojtbadaassignment14.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CastOfMovie {
    @SerializedName("id")
    private int id;
    @SerializedName("cast")
    private List<Cast> castList;

    public CastOfMovie() {
    }

    public CastOfMovie(int id, List<Cast> castList) {
        this.id = id;
        this.castList = castList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Cast> getCastList() {
        return castList;
    }

    public void setCastList(List<Cast> castList) {
        this.castList = castList;
    }
}
