package de.loicezt.srvmgr.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.loicezt.srvmgr.ExtensionMethods;
import de.loicezt.srvmgr.ServerType;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


/**
 * The Wrapper class is instantiated when the node doesn't start as a {@link de.loicezt.srvmgr.master.Master Master} node
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class Wrapper {
    private static final String wrapperConfigFile = "./wrapper.yml";
    private WrapperConfigurationHolder config;
    private Logger logger;

    /**
     * The constructor <p>Connects to the localhost MQTT server and sets itself up to listen for instructions</p>
     */
    public Wrapper() {

        System.out.println("Starting up...");
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            config = mapper.readValue(new File(wrapperConfigFile), WrapperConfigurationHolder.class);

        } catch (JsonProcessingException e) {
            logger.severe("Something went wrong while reading the configuration file, stopping immediately");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger = Logger.getLogger("Wrapper @"+config.getServerID());
        try {
            ExtensionMethods.setupLogging(logger);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            IMqttClient client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            client.publish("log", new MqttMessage(("Wrapper node " + config.getServerID() + " started").getBytes(StandardCharsets.UTF_8)));
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    logger.finer("topic message " + topic +" "+ new String(message.getPayload(), StandardCharsets.UTF_8));
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    String[] args = payload.split(" ");
                    switch (args[0]) {
                        case "stop":
                            if (args[1].equals("wrapper")) {
                                new Thread(() -> {
                                    logger.info("Stopping wrapper !");
                                    try {
                                        client.publish(config.getServerID(), new MqttMessage("wrapper stopping".getBytes(StandardCharsets.UTF_8)));
                                        logger.fine("Unsubscribing from topic");
                                        client.unsubscribe(config.getServerID());
                                        logger.fine("Disconnecting Mqtt client");
                                        client.disconnect();
                                        logger.fine("Closing connection");
                                        client.close();
                                        logger.fine("Connection closed");
                                        logger.info("Finished execution");
                                        System.exit(0);
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            }
                            break;
                        case "start":
                            if (args[1].equals("server")) {
                                if (config.getServerType() == ServerType.FIXED) {
                                    Runtime.getRuntime().exec("./" + config.getServerID() + "/start.sh");
                                } else if (config.getServerType() == ServerType.MINIGAME) {
                                    logger.info("Preparing server...");
                                    new File("server/mods").mkdirs();
                                    new File("server/plugins").mkdirs();
                                    File srvDir = new File("server");
                                    try {
                                        logger.info("Copying plugins...");
                                        for (String url : config.getPlugins()) {
                                            File f = new File(url);
                                            logger.fine(f.getName());
                                            ExtensionMethods.copyFile(f, new File("server/plugins/" + f.getName()));
                                        }
                                    } catch (NullPointerException e) {
                                        logger.fine("Didn't find any plugin to copy");
                                    }
                                    try {
                                        logger.info("Copying mods...");

                                        for (String url : config.getMods()) {
                                            File f = new File(url);
                                            logger.fine(f.getName());
                                            ExtensionMethods.copyFile(f, new File("server/mods/" + f.getName()));
                                        }
                                    } catch (NullPointerException e) {
                                        logger.fine("Didn't find any mods to copy");
                                    }
                                    try {
                                        logger.info("Copying Additional server files...");

                                        for (String url : config.getAdditionalFiles()) {
                                            File f = new File(url);
                                            logger.fine(f.getName());
                                            ExtensionMethods.copyFile(f, new File("server/" + f.getName()));
                                        }
                                    } catch (NullPointerException e) {
                                        logger.fine("Didn't find any additional files to copy");
                                    }
                                    ExtensionMethods.copyFile(new File(config.getServerJar()), new File("server/server.jar"));
                                    logger.info("Starting server...");
                                    ProcessBuilder pb = new ProcessBuilder("sh", "start.sh");
                                    pb.directory(new File("server"));
                                    pb.inheritIO();
                                    pb.start();
                                    logger.fine("Started");
                                }
                            }
                            break;
                        default:
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                }
            });
            client.subscribe(config.getServerID(), 1);
            client.publish(config.getServerID(), new
                    MqttMessage("wrapper online".getBytes(StandardCharsets.UTF_8)));
        } catch (MqttException e) {
            ExtensionMethods.handleException(e);
        }
        logger.info("Running!");
    }
}
