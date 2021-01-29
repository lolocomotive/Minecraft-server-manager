package de.loicezt.srvmgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.loicezt.srvmgr.master.Master;
import de.loicezt.srvmgr.wrapper.Wrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static String configURL = "./config.yml";
    public static String defaultConfiguration = "type: 0";
    public static int MASTER = 0;
    public static int WRAPPER = 1;
    public static ConfigurationHolder config;

    public static void handleException(Exception e) {
        System.out.println("An error occurred!");
        System.out.println(e.getLocalizedMessage());
        System.exit(1);
    }

    public static void main(String[] args) {
        File[] garbage = getGarbage();
        if (garbage.length > 0) {
            System.out.println("Cleaning up previous session");
            cleanup(garbage);
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File configFile = new File(configURL);
        if (!configFile.exists()) {
            try {
                System.out.println("First run detected - Welcome !");
                System.out.println("Writing default config file");
                configFile.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
                writer.write(defaultConfiguration);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                handleException(e);
            }
        }
        try {
            config = mapper.readValue(configFile, ConfigurationHolder.class);
            if (config.getType() == WRAPPER) {
                new Wrapper();
            } else if (config.getType() == MASTER) {
                new Master();
            }

        } catch (IOException e) {
            handleException(e);
        }
    }

    public static void cleanup(File[] garbage) {
        for (File file : garbage) {
            try {
                if (file.isDirectory()) {
                    deleteDirectory(file.toPath());
                } else {
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File[] getGarbage() {
        File[] files = new File(".").listFiles();
        List<File> r = new ArrayList<>();
        for (File file : files) {
            if (file.getName().endsWith("1883") || file.getName().startsWith("hs_err_pid")) {
                r.add(file);
            }
        }
        File[] re = new File[r.size()];
        for (int i = 0; i < r.size(); i++) {
            re[i] = r.get(i);
        }
        return re;
    }

    public static boolean deleteDirectory(Path dir) throws IOException {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        return !dir.toFile().exists();
    }
}
