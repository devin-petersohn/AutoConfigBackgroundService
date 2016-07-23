package com.devinpetersohn.autoconfigbackgroundservice;

public class Hourly extends CollectionFrequency implements CollectionFrequencies {
        private double milliseconds = 3600000;

        public Hourly(){

        }

        public double frequencyInMilli(){
            return milliseconds;
        }
    }
