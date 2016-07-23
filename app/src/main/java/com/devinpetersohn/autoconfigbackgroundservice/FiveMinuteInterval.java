package com.devinpetersohn.autoconfigbackgroundservice;

public class FiveMinuteInterval extends CollectionFrequency implements CollectionFrequencies {
    private double milliseconds = 300000;

    public FiveMinuteInterval(){

    }

    public double frequencyInMilli(){
        return milliseconds;
    }

}
