package de.loicezt.srvmgr.master;

import de.loicezt.srvmgr.ExtensionMethods;
import de.loicezt.srvmgr.Main;
import de.loicezt.srvmgr.WrapperController;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The class that is instantiated if the running node is a master node. It will start up all of the child (aka {@link de.loicezt.srvmgr.wrapper.Wrapper Wrapper}) nodes
 */
public class Master {
    boolean stop = false;
    MqttClient client;
    Logger logger;


    /**
     * The constructor
     * Connects to the localhost MQTT server and starts all of the children (Which will then start up as {@link de.loicezt.srvmgr.wrapper.Wrapper Wrappers})
     */
    public Master() {
        logger = Logger.getLogger("Master");
        try {
            ExtensionMethods.setupLogging(logger);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Starting up as MASTER node");
        logger.config("config");
        try {
            client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            ExtensionMethods.mqttMsgSend("log", "Master node started", client);
            for (WrapperController controller : Main.config.getServers()) {
                controller.startWrapper();
            }
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker
                }

                @Override
                public void messageArrived(String topic, MqttMessage message){
                    logger.finer(topic + ": " + new String(message.getPayload(), UTF_8));
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    if ("stop master".equals(payload)) {
                        logger.info("Stopping master");
                        stop = true;
                        new Thread(() -> {
                            logger.fine("Stopping children...");
                            for (WrapperController wc : Main.config.getServers()) {
                                wc.stop(client);
                            }
                            logger.fine("Unsubscribing...");
                            try {
                                client.unsubscribe("master");
                                logger.fine("Disconnecting...");
                                client.disconnect();
                                logger.fine("Closing connection...");
                                client.close();
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                            File[] garbage = ExtensionMethods.getGarbage();
                            if (garbage.length > 0) {
                                logger.info("Cleaning up leftover garbage");
                                ExtensionMethods.cleanup(garbage);
                            }
                            logger.info("Finished execution, good bye !");
                        }).start();
                    } else {
                        logger.severe("Unrecognized instruction \"" + payload + "\"");
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                }
            });
            client.subscribe("master", 1);
        } catch (
                MqttException e) {
            ExtensionMethods.handleException(e);
        }
    }

}
