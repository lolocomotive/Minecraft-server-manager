package de.loicezt.srvmgr.master;

import de.loicezt.srvmgr.ExtensionMethods;
import de.loicezt.srvmgr.Main;
import de.loicezt.srvmgr.WrapperInstance;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The class that is instantiated if the running node is a master node. It will start up all of the child (aka {@link de.loicezt.srvmgr.wrapper.Wrapper Wrapper}) nodes
 */
public class Master {
    boolean stop = false;
    MqttClient client;
    Logger logger = Logger.getLogger(Master.class.getName());

    /**
     * The constructor
     * Connects to the localhost MQTT server and starts all of the children (Which will then start up as {@link de.loicezt.srvmgr.wrapper.Wrapper Wrappers})
     */
    public Master() {
        logger.info("Starting up as MASTER node");
        logger.config("config");
        try {
            client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            ExtensionMethods.mqttMsgSend("log", "Master node started", client);
            for (WrapperInstance instance : Main.config.getServers()) {
                instance.startWrapper();
            }
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println(topic + ": " + new String(message.getPayload(), UTF_8));
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    switch (payload) {
                        case "stop master":
                            logger.info("stopping");
                            stop = true;
                            new Thread(() -> {
                                logger.info("Stopping children...");
                                for (WrapperInstance wi : Main.config.getServers()) {
                                    wi.stop(client);
                                }
                                logger.info("Unsubscribing...");
                                try {
                                    client.unsubscribe("master");
                                    logger.info("Disconnecting...");
                                    client.disconnect();
                                    logger.info("Closing connection...");
                                    client.close();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                                File[] garbage = ExtensionMethods.getGarbage();
                                if (garbage.length > 0) {
                                    logger.info("Cleaning up leftover garbage");
                                    ExtensionMethods.cleanup(garbage);
                                }
                            }).start();
                            break;
                        default:
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
