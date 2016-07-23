package com.devinpetersohn.autoconfigbackgroundservice;

public class Weekly extends CollectionFrequency implements CollectionFrequencies {
        private double milliseconds = 604800000;

        public Weekly(){

        }

        public double frequencyInMilli(){
            return milliseconds;
        }
    }
