package de.loicezt.srvmgr.master;

import de.loicezt.srvmgr.Main;
import de.loicezt.srvmgr.WrapperInstance;
import org.eclipse.paho.client.mqttv3.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Master {
    public static void log(String message) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("[" + timestamp + "]\t[INFO]\t[master]:\t" + message);
    }

    public static void logErr(String message) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.err.println("[" + timestamp + "]\t[ERROR]\t[master]:\t" + message);
    }

    public static void logErr(String message, String user) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.err.println("[" + timestamp + "]\t[ERROR]\t[" + user + "]:\t" + message);
    }

    public static void log(String message, String user) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("[" + timestamp + "]\t[INFO]\t[" + user + "]:\t" + message);
    }

    public Master() {
        log("Starting up as MASTER node");
        String publisherId = UUID.randomUUID().toString();
        try {
            IMqttClient client = new MqttClient("tcp://localhost:1883", publisherId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            client.connect(options);
            client.publish("log", new MqttMessage("Master node started".getBytes(StandardCharsets.UTF_8)));
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
                        for (WrapperInstance wi : Main.config.getServers()) {
                            wi.stop(client);
                        }
                        log("Disconnecting...");
                        client.disconnectForcibly(0);
                        log("Closing connection...");
                        client.close();
                        log("Cleaning up...");
                        File[] garbage = Main.getGarbage();
                        Main.cleanup(garbage);
                        log("Good bye !");
                        System.exit(0);
                        break;
                    default:
                        logErr("Unrecognized instruction \"" + payload + "\"");
                }
            });
            receivedSignal.await(1, TimeUnit.MINUTES);
        } catch (MqttException e) {
            Main.handleException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
