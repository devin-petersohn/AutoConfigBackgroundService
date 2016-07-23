package com.devinpetersohn.autoconfigbackgroundservice;

public class QuarterHourly extends CollectionFrequency implements CollectionFrequencies {
        private double milliseconds = 900000;

        public QuarterHourly(){

        }

        public double frequencyInMilli(){
            return milliseconds;
        }
}

