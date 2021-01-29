package de.loicezt.srvmgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.loicezt.srvmgr.master.Master;
import de.loicezt.srvmgr.wrapper.Wrapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static de.loicezt.srvmgr.master.Master.logErr;

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
     * @see ConfigurationHolder The configuration holder class
     */
    public static ConfigurationHolder config;


    /**
     * Method to call when an exception is thrown
     * @param e The exception that occurred
     */
    public static void handleException(Exception e) {
        logErr("An error occurred!");
        logErr(e.getLocalizedMessage());
        System.exit(1);
    }

    /**
     * Method to easily send a Message on the desired topic
     * @param topic The MQTT topic
     * @param message The message to send
     * @param client The MQTT client
     * @throws MqttException
     */
    public static void mqttMsgSend(String topic, String message, MqttClient client) throws MqttException {
        MqttMessage m = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
        m.setQos(2);
        client.publish(topic, m);
    }

    /**
     * @param args the command line arguments
     * Main method called at the start of the program
     */
    public static void main(String[] args) {


        String topic = "MQTT Examples";
        String content = "Message from MqttPublishSample";
        int qos = 2;
        String broker = "tcp://localhost:1883";
        String clientId = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }


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

    /**
     * Removes all the files in the array, called at the start and end of program to clean up stuff
     * @param garbage files to be removed
     */
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

    /**
     * Get the temporary garbage files that should be deleted and are no longer required
     * @return The temporary garbage files that should be deleted and are no longer required
     */
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

    /**
     * Deletes a directory
     * @param dir The directory to be deleted
     * @return Wether the deletion was successful
     * @throws IOException
     */
    public static boolean deleteDirectory(Path dir) throws IOException {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        return !dir.toFile().exists();
    }
}
