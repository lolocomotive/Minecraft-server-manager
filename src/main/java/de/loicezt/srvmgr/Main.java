package de.loicezt.srvmgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.loicezt.srvmgr.master.Master;
import de.loicezt.srvmgr.wrapper.Wrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * The main class
 */
public class Main {
    /**
     * The location of the configuration file
     */
    public static String configURL = "./config.yml";
    /**
     * The default configuration to write in an empty file when the configuration file does not exist
     */
    public static String defaultConfiguration = "type: 0";
    public static int MASTER = 0;
    public static int WRAPPER = 1;
    /**
     * The configuration of the program
     *
     * @see ConfigurationHolder The configuration holder class
     */
    public static ConfigurationHolder config;

    /**
     * Main method called at the start of the program
     *
     * @param args the command line arguments
     *             <p>It will instantiate a {@link Wrapper} or {@link Master} class depending on the configuration (0 = Master, 1 = Wrapper)</p>
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("Main");
        File[] garbage = ExtensionMethods.getGarbage();
        if (garbage.length > 0) {
            logger.info("Cleaning up previous session");
            ExtensionMethods.cleanup(garbage);
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File configFile = new File(configURL);
        if (!configFile.exists()) {
            try {
                logger.info("First run detected - Welcome !");
                logger.info("Writing default config file");
                configFile.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
                writer.write(defaultConfiguration);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                ExtensionMethods.handleException(e);
            }
        }
        try {
            config = mapper.readValue(configFile, ConfigurationHolder.class);
            try {
                ExtensionMethods.setupLogging(logger);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (config.getType() == WRAPPER) {
                new Wrapper();
            } else if (config.getType() == MASTER) {
                new Master();
            }

        } catch (IOException e) {
            ExtensionMethods.handleException(e);
        }
    }
}
