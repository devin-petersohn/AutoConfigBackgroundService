package com.devinpetersohn.autoconfigbackgroundservice;

public class TenSecondInterval extends CollectionFrequency implements CollectionFrequencies {
        private double milliseconds = 1000*10;

        public TenSecondInterval(){

        }

        public double frequencyInMilli(){
            return milliseconds;
        }
    }
