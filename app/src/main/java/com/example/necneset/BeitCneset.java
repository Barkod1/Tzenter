package com.example.necneset;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class BeitCneset {
    public String name, details;
    public List<Prayer> prayers;
    public double latitude;
    public double longitude;
    public String admin;

    public BeitCneset(String name,String details, double latitude, double longitude, String admin){
        this.name = name;
        this.details = details;
        this.prayers = new ArrayList<>();
        this.latitude = latitude;
        this.longitude = longitude;
        this.admin = admin;

        }

    public BeitCneset(){
        this.prayers = new ArrayList<>();
    }

}
