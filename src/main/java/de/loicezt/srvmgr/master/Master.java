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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The class that is instantiated if the running node is a master node. It will start up all of the child nodes
 */
public class Master {
    /**
     * Custom logging solution, prints a message with the current timestamp
     * @param message The message you want to log
     */
    public static void log(String message) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("[" + timestamp + "]\t[INFO]\t[master]:\t" + message);
    }

    /**
     * Custom logging solution, prints a message with the current timestamp and origin
     * @param message The message you want to log
     * @param user The origin of the message
     */
    public static void log(String message, String user) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("[" + timestamp + "]\t[INFO]\t[" + user + "]:\t" + message);
    }

    /**
     * Custom logging solution, prints an error message on System.err with the current timestamp
     * @param message The error message you want to log
     */
    public static void logErr(String message) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.err.println("[" + timestamp + "]\t[ERROR]\t[master]:\t" + message);
    }
    /**
     * Custom logging solution, prints an error message on System.err with the current timestamp and origin
     * @param message The message you want to log
     * @param user The origin of the message
     */
    public static void logErr(String message, String user) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.err.println("[" + timestamp + "]\t[ERROR]\t[" + user + "]:\t" + message);
    }

    /**
     * The constructor
     * Connects to the localhost MQTT server and starts all of the children
     */
    public Master() {
        log("Starting up as MASTER node");
        String publisherId = UUID.randomUUID().toString();
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient client = new MqttClient("tcp://localhost:1883", publisherId, persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options);
            Main.mqttMsgSend("log", "Master node started", client);
            for (WrapperInstance instance : Main.config.getServers()) {
                instance.startWrapper();
            }
            CountDownLatch receivedSignal = new CountDownLatch(10);
            client.subscribe("master", (topic, msg) -> {
                String payload = new String(msg.getPayload(), StandardCharsets.UTF_8);
                receivedSignal.countDown();
                switch (payload) {
                    case "stop master":
                        log("Stopping children...");
                        List<Thread> stopThreads = new ArrayList<>();
                        for (WrapperInstance wi : Main.config.getServers()) {
                            Thread t = new Thread(() -> {
                                wi.stop(client);
                            });
                            t.start();
                            stopThreads.add(t);
                        }
                        for (Thread t : stopThreads) {
                            while (t.isAlive()) {
                            }
                        }
                        log("Disconnecting...");
                        client.disconnect();
                        log("Closing connection...");
                        client.close();
                        File[] garbage = Main.getGarbage();
                        if (garbage.length > 0) {
                            log("Cleaning up leftover garbage");
                            Main.cleanup(garbage);
                        }
                        log("Good bye !");
                        System.exit(0);
                        break;
                    default:
                        logErr("Unrecognized instruction \"" + payload + "\"");
                }
            });
            receivedSignal.await(1, TimeUnit.MINUTES);
        } catch (
                MqttException e) {
            Main.handleException(e);
        } catch (
                InterruptedException e) {
            e.printStackTrace();
        }
    }
}
