// This is a dummy class until the library is finished

import java.util.Arrays;
import java.util.HashMap;

public class Main {
    public static void main(String[] args){
        String confFile = "aconfig/conf.jc";
        Jconf jc;
        try {
            jc = new Jconf(confFile);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        HashMap conf = jc.get();
        try {
            jc.set("General", "Limit", "6");
        } catch (Exception e){
            ;
        }
        System.out.println(jc.getVal("Not so general", "Balance"));
    }
}
