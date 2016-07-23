package com.devinpetersohn.autoconfigbackgroundservice;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Created by DevinPetersohn on 4/19/15.
 */
public abstract class CollectionFrequency {


    public abstract double frequencyInMilli();

    public static CollectionFrequencies getFrequency(String className) {

        CollectionFrequencies frequency = null;
        Class c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            frequency = c != null ? (CollectionFrequencies) c.newInstance() : null;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return frequency;
    }

    public CollectionFrequency() {

    }
}

