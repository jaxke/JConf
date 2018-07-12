// This is a dummy class until the library is finished

import java.util.Arrays;
import java.util.HashMap;

public class Main {
    public static void main(String[] args){
        String confFile = "config/conf.jc";
        Jconf jc = new Jconf(confFile);
        HashMap conf = jc.get();
        System.out.println(Arrays.asList(jc.get()));
        jc.set("General", "FileIO", "0");
    }
}
