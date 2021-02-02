package de.loicezt.srvmgr.master;

import de.loicezt.srvmgr.Main;
import de.loicezt.srvmgr.WrapperInstance;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The class that is instantiated if the running node is a master node. It will start up all of the child (aka {@link de.loicezt.srvmgr.wrapper.Wrapper Wrapper}) nodes
 */
public class Master {
    boolean stop = false;
    MqttClient client;

    /**
     * Custom logging solution, prints a message with the current timestamp
     *
     * @param message The message you want to log
     */
    public static void log(String message) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("[" + timestamp + "]\t[INFO]\t[master]:\t" + message);
    }

    /**
     * Custom logging solution, prints a message with the current timestamp and origin
     *
     * @param message The message you want to log
     * @param user    The origin of the message
     */
    public static void log(String message, String user) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("[" + timestamp + "]\t[INFO]\t[" + user + "]:\t" + message);
    }

    /**
     * Custom logging solution, prints an error message on System.err with the current timestamp
     *
     * @param message The error message you want to log
     */
    public static void logErr(String message) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.err.println("[" + timestamp + "]\t[ERROR]\t[master]:\t" + message);
    }

    /**
     * Custom logging solution, prints an error message on System.err with the current timestamp and origin
     *
     * @param message The message you want to log
     * @param user    The origin of the message
     */
    public static void logErr(String message, String user) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.err.println("[" + timestamp + "]\t[ERROR]\t[" + user + "]:\t" + message);
    }

    /**
     * The constructor
     * Connects to the localhost MQTT server and starts all of the children (Which will then start up as {@link de.loicezt.srvmgr.wrapper.Wrapper Wrappers})
     */
    public Master() {
        log("Starting up as MASTER node");

        try {
            MqttClient client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            Main.mqttMsgSend("log", "Master node started", client);
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
                            log("stopping");
                            stop = true;
                            new Thread(() -> {
                                log("Stopping children...");
                                for (WrapperInstance wi : Main.config.getServers()) {
                                        wi.stop(client);
                                }
                                log("Unsubscribing...");
                                try {
                                    client.unsubscribe("master");
                                    log("Disconnecting...");
                                    client.disconnect();
                                    log("Closing connection...");
                                    client.close();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                                File[] garbage = Main.getGarbage();
                                if (garbage.length > 0) {
                                    log("Cleaning up leftover garbage");
                                    Main.cleanup(garbage);
                                }
                                log("Good bye !");
                            }).start();
                            break;
                        default:
                            logErr("Unrecognized instruction \"" + payload + "\"");
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                }
            });
            client.subscribe("master", 1);
        } catch (
                MqttException e) {
            Main.handleException(e);
        }
    }
}
