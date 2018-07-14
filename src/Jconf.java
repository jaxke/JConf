import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Jconf {
    private HashMap configMap;  // Configuration object de-serialized from the file.
    private String confFile;    // Absolute path to configuration file.
    private String confBackupFile;  // Abs path to the backup file

    // TODO This method is not needed?
    HashMap get(){
        return configMap;
    }

    Object getVal(String category, String key) {
        try {
            return ((HashMap<String, Object>) this.configMap.get(category)).get(key);
        } catch(NullPointerException npe){
            throw new IllegalArgumentException("\"" + key + "\" not found under \"" + category + "\"");
        }
    }

    // Ambiguous "Exception" because the exc. type is resolved before hand.
    public Jconf(String confFile) throws Exception {
        // Parameter was abs path
        if (confFile.charAt(0) == '/')
            this.confFile = confFile;
        // Parameter is relative to user's home dir
        else if (confFile.charAt(0) == '~')
            this.confFile = System.getProperty("user.home") + confFile.substring(1, confFile.length());
        // Parameter was relative to cwd
        else
            this.confFile = System.getProperty("user.dir") + "/" + confFile;
        // Filename of the backup file
        String confBackupFileName = "conf_backup.jc";
        this.confBackupFile = confFile.substring(0, confFile.lastIndexOf("/")) + confBackupFileName;
        String conf;
        try {
            conf = readConfigFile(this.confFile);
        } catch (Exception e){
            logException(e);
            throw e;
        }
        this.configMap = parseConfig(conf);
    }
    /*
     * CATEGORY - Find lines that have curly braces(Ie. "{General}") and store their names
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
        if (categories.size() == 0){
            throw new NullPointerException("Configuration file is not of supported format.");
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
    Takes in lines from config file under ONE category.
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
            // Split and strip
            String arr[] = item.substring(1, item.length() - 1).split("\\s*,\\s*");
            return arr;

        }
        // TODO something else than try catch
        // Convert to Double
        try {
            if (item.contains("."))
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

    private boolean fileExists(String file){
        File f = new File(file);
        if(f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }

    // Reads file 'file' to a String and returns it.
    // 'file' must be an abspath.
    private String readConfigFile(String file) throws NullPointerException, FileNotFoundException, IOException {
        BufferedReader br = null;
        if (!fileExists(file))
            throw new FileNotFoundException("File " + file + " doesn't exist");
        try {
            // Read and write to temp file simultaneously
            br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine().trim();
            if (line == null){
                throw new NullPointerException("File is empty");
            }
            while (line != null){
                sb.append(line);
                sb.append(System.lineSeparator());  // '\n'
                line = br.readLine();
            }
            return sb.toString();
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    throw ioe;
                }
            }
        }
    }

    // Modify a configuration item's value
    // Return true if successful
    boolean set(String category, String element, String value) throws IOException {
        try {
            makeBackup();
        } catch(IOException ioe){
            throw ioe;
        }

        String brTmp = "config/br_tmp";
        BufferedReader br = null;
        BufferedWriter bw = null;

        if (!fileExists(confFile))
            throw new IOException("File disappeared during runtime!");

        try {
            br = new BufferedReader(new FileReader(this.confFile));
            bw = new BufferedWriter(new FileWriter(brTmp));
            String line;
            while ((line = br.readLine()) != null){
                if (line.contains("{" + category + "}")){
                    bw.write(line + "\n");
                    while((line = br.readLine()) != null) {
                        if (line.contains("{") && line.contains("}")) {
                            // Next category encountered, stop looking for key
                            break;
                        }
                        if (line.startsWith(element + " =") || line.startsWith(element + "=")) {
                            line = element + " = " + value;
                        }
                        bw.write(line + "\n");
                    }
                }
                bw.write(line + "\n");
            }
        } catch (IOException ioe) {
            revertToBackup();
            throw ioe;
        } finally {
            try {
                br.close();
                bw.close();
            } catch (IOException ioe) {
                revertToBackup();
                throw ioe;
            }
        }
        try {
            Files.copy(Paths.get(brTmp), Paths.get(this.confFile), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            revertToBackup();
            throw ioe;
        } finally {
            (new File(brTmp)).delete();
        }
        return true;
    }

    // Create a backup of the config file before writing to it.
    private int makeBackup() throws IOException {
        try {
            Files.copy(Paths.get(this.confFile), Paths.get(this.confBackupFile), StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException ioe){
            logException(ioe);
            throw ioe;
        }
        return 0;
    }

    // Overwrite conf file with the latest backup.
    private void revertToBackup() throws IOException{
        try {
            Files.copy(Paths.get(this.confBackupFile), Paths.get(this.confFile), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            logException(ioe);
            throw ioe;
        }

    }

    private void logException(Exception e){
        try {
            Handler handler = new FileHandler("JCONF_LOG", 100, 100);
            Logger.getAnonymousLogger().addHandler(handler);
        } catch (Exception e2){
        }
        Logger logger = Logger.getAnonymousLogger();
        logger.log(Level.FINEST, "exception thrown", e);
    }
}
