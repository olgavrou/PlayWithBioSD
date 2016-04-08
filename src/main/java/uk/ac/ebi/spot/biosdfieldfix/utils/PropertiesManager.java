package uk.ac.ebi.spot.biosdfieldfix.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesManager {

    private static String initFilePath = "solr.properties";
    private static Properties prop;

    public PropertiesManager(String filePath) {
        if (filePath != null){
            initFilePath = filePath;
        }
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream input = classLoader.getResourceAsStream(initFilePath);
                prop = new Properties();
                if (input != null) {
                    prop.load(input);
                }

                if (System.getProperty("smoother.config.location") != null) {
                    InputStream overideInput = new FileInputStream(new File(System.getProperty("smoother.config.location")));
                    Properties override = new Properties();
                    override.load(overideInput);
                    prop.putAll(override);
                }
            } catch (IOException ex) {
                Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public String getPropertyValue(String key) {
        return prop.getProperty(key);
    }

    public Properties getProperties() {
        return prop;
    }
}

