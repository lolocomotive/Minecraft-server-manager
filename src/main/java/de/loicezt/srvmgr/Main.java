package de.loicezt.srvmgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.loicezt.srvmgr.master.Master;
import de.loicezt.srvmgr.wrapper.Wrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static String configURL = "./config.yml";
    public static String defaultConfiguration = "type: 0";
    public static int MASTER = 0;
    public static int WRAPPER = 1;

    public static void handleException(Exception e) {
        System.out.println("An error occurred!");
        System.out.println(e.getLocalizedMessage());
        System.exit(1);
    }

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File config = new File(configURL);
        if (!config.exists()) {
            try {
                System.out.println("First run detected - Welcome !");
                System.out.println("Writing default config file");
                config.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(config));
                writer.write(defaultConfiguration);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                handleException(e);
            }
        }
        try {
            int type = mapper.readValue(config, ConfigurationHolder.class).getType();
            if(type == WRAPPER){
                new Wrapper();
            }else if(type == MASTER){
                new Master();
            }

        } catch (IOException e) {
            handleException(e);
        }
    }
}
