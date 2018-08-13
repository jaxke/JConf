package com.jconf.jconf;

import java.util.Arrays;
import java.util.HashMap;

public class Main {
    public static void main(String[] args){
        String confFile = "config/conf.jc";
        Jconf jc;
        try {
            jc = new Jconf(confFile);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        HashMap conf = jc.get();
        try {
            jc.set("General", "Width", String.valueOf(15));
            jc.set("General", "Height", String.valueOf(15));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
