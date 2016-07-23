package com.devinpetersohn.autoconfigbackgroundservice;

public class Daily extends CollectionFrequency implements CollectionFrequencies {
        private double milliseconds = 86400000;

        public Daily(){

        }

        public double frequencyInMilli(){
            return milliseconds;
        }
    }
