package de.loicezt.srvmgr.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.loicezt.srvmgr.ExtensionMethods;
import de.loicezt.srvmgr.ServerType;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


/**
 * The Wrapper class is instantiated when the node doesn't start as a {@link de.loicezt.srvmgr.master.Master Master} node
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class Wrapper {
    private static final String wrapperConfigFile = "./wrapper.yml";
    private WrapperConfigurationHolder config;
    private Logger logger = Logger.getLogger(Wrapper.class.getName());

    /**
     * The constructor <p>Connects to the localhost MQTT server and sets itself up to listen for instructions</p>
     */
    public Wrapper() {
        logger.info("Starting up as WRAPPER node");
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
                    logger.info("topic message " + topic + new String(message.getPayload(), StandardCharsets.UTF_8));
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    String[] args = payload.split(" ");
                    switch (args[0]) {
                        case "stop":
                            if (args[1].equals("wrapper")) {
                                new Thread(() -> {
                                    logger.info("Stopping wrapper !");
                                    try {
                                        client.publish(config.getServerID(), new MqttMessage("wrapper stopping".getBytes(StandardCharsets.UTF_8)));
                                        logger.info("Unsubscribing from topic");
                                        client.unsubscribe(config.getServerID());
                                        logger.info("Disconnecting Mqtt client");
                                        client.disconnect();
                                        logger.info("Closing connection");
                                        client.close();
                                        logger.info("Connection closed");
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
                                            logger.info(f.getName());
                                            ExtensionMethods.copyFile(f, new File("server/plugins/" + f.getName()));
                                        }
                                    } catch (NullPointerException e) {
                                        logger.info("Didn't find any plugin to copy");
                                    }
                                    try {
                                        logger.info("Copying mods...");

                                        for (String url : config.getMods()) {
                                            File f = new File(url);
                                            logger.info(f.getName());
                                            ExtensionMethods.copyFile(f, new File("server/mods/" + f.getName()));
                                        }
                                    } catch (NullPointerException e) {
                                        logger.info("Didn't find any mods to copy");
                                    }
                                    try {
                                        logger.info("Copying Additional server files...");

                                        for (String url : config.getAdditionalFiles()) {
                                            File f = new File(url);
                                            logger.info(f.getName());
                                            ExtensionMethods.copyFile(f, new File("server/" + f.getName()));
                                        }
                                    } catch (NullPointerException e) {
                                        logger.info("Didn't find any additional files to copy");
                                    }
                                    ExtensionMethods.copyFile(new File(config.getServerJar()), new File("server/server.jar"));
                                    System.out.println("starting server...");
                                    Process p = Runtime.getRuntime().exec("./server/start.sh");
                                    System.out.println("Started");
                                    Thread logger = new Thread(() -> {
                                        try {
                                            System.out.println("Initializing server logging");
                                            BufferedReader stdInput = new BufferedReader(new
                                                    InputStreamReader(p.getInputStream()));
                                            BufferedReader stdError = new BufferedReader(new
                                                    InputStreamReader(p.getErrorStream()));
                                            String in = null, err = null;
                                            while (p.isAlive() || ((in = stdInput.readLine()) != null) || ((err = stdError.readLine()) != null)) {
                                                if (in != null)
                                                    System.out.println("[server]: " + in);
                                                if (err != null)
                                                    System.err.println("[server]: " + err);
                                            }
                                            stdInput.close();
                                            stdError.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        System.out.println("Server logging terminated");
                                    });
                                    logger.start();
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
