package com.example.necneset;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
public class Prayer {
    @PropertyName("prayer_name")
    public String name;
    @PropertyName("prayer_hour")
    public String hour;
    @PropertyName("coming")
    int coming;

    public Prayer(String name, String hour) {
        this.name = name;
        this.hour = hour;
    }

    public Prayer(String name, String hour, int coming) {
        this.name = name;
        this.hour = hour;
    }

    public Prayer(){

    }

    public String toString(){
        return this.name + ": " + this.hour + " באים: " + this.coming;
    }
}
