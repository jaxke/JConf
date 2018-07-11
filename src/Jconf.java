import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Jconf {
    HashMap configMap;
    public Jconf(String confFile){
        String conf = readConfigFile(confFile);
        if (conf.equals(""))
            configMap = null;
        configMap = parseConfig(conf);
        System.out.println(Arrays.toString(configMap.entrySet().toArray()));
    }
    /*
     * CATEGORY - Find lines that have curly braces(Ie. "{General}") and take store their names
     * ELEMENT - Find elements under each category and save their names(keys) and values
     * Return a 2D HashMap of categories and their elements: {CAT1: {k1: v1, k2: v2}, CAT2: {k3: v3, k4: v4}}
     */
    private HashMap<String, HashMap<String, Object>> parseConfig(String fileContents){
        HashMap<String, HashMap<String, Object>> conf = new HashMap<>();
        String fc[] = fileContents.split("\n");
        ArrayList<String> categories = new ArrayList<>();
        for (String line : fc){
            if (!line.contains("{")) {
                continue;
            }
            // Note: { is a regex character and needs to be escaped while } is not. (Above, contains() method looks for
            // actual strings and not regex)
            String openingBrace[] = line.split("\\{");
            String cat = openingBrace[1].split("}")[0];
            categories.add(cat);
        }
        for (int i = 0; i < categories.size(); i++){
            String aCategory[];
            if (categories.size() != i+1) {     // If not last category
                aCategory = fileContents.split(String.format("\\{%s}", categories.get(i)))[1].split(String.format("\\{%s}", categories.get(i + 1)))[0].split("\n");
            } else {
                aCategory = fileContents.split(String.format("\\{%s}", categories.get(i)))[1].split("\n");
            }
            HashMap<String, Object> elements = parseKeyVal(aCategory);
            if (elements != null)
                conf.put(categories.get(i), elements);
        }
        return conf;
    }

    /*
    Takes in lines from config file under ONE category. Ie. "Colour = Black\nName = Jimi"
    Returns {Colour=Black, Name=Jimi}
     */
    private HashMap<String, Object> parseKeyVal(String[] elements){
        // Not a char because it's going to be used in split()
        String delimiter = "=";
        HashMap<String, Object> ret = new HashMap<>();
        for (String element : elements) {
            if (element.equals(""))
                continue;
            if (element.charAt(0) == '/' && element.charAt(1) == '/')
                continue;
            if (element.contains(delimiter)) {
                String key = element.split(delimiter)[0].trim();
                String val = element.split(delimiter)[1].trim();
                Object convertedVal = convertType(val);
                ret.put(key, convertedVal);
            }
        }
        if (ret.isEmpty())
            return null;
        else
            return ret;
    }

    private Object convertType(String item){
        // Convert to boolean
        if (item.equals("true") || item.equals("True")) {
            return true;
        }
        if (item.equals("false") || item.equals("False")) {
            return false;
        }
        // Convert to array. Format must be {item1, item2, item3} or {item}
        if (item.charAt(0) == '[' && item.charAt(item.length() - 1) == ']'){
            String arr[] = item.substring(1, item.length() - 1).split("\\s*,\\s*");
            return arr;

        }
        // TODO all numeric values are converted to Double
        // Convert to Double
        try {
            return Double.parseDouble(item);
        } catch (NumberFormatException nfe){
            ;
        }
        // Convert to int
        try {
            return Integer.parseInt(item);
        } catch (NumberFormatException nfe) {
            ;
        }

        // No suitable type found, return original string.
        return item;
    }

    // Reads file 'file' to a String and returns it.
    // 'file' must be an abspath.
    private static String readConfigFile(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null){
                sb.append(line);
                // '\n'
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } catch (FileNotFoundException fnfe){
            System.out.println("File " + file + " does not exist!");
            return "";
        } catch (IOException ioe) {
            System.out.println("IO Exception caught. Check your permissions on file " + file + ".");
            return "";
        }
    }
}
