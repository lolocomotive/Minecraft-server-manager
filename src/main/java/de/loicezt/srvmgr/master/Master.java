package de.loicezt.srvmgr.master;

import de.loicezt.srvmgr.Main;
import de.loicezt.srvmgr.WrapperInstance;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Master {
    public Master() {
        System.out.println("Starting up as MASTER node");
        String publisherId = UUID.randomUUID().toString();
        try {
            IMqttClient publisher = new MqttClient("tcp://localhost:1883", publisherId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            publisher.connect(options);
            publisher.publish("main", new MqttMessage("Master node started".getBytes(StandardCharsets.UTF_8)));
            for(WrapperInstance instance : Main.config.getServers()){
                instance.startWrapper();
            }
        } catch (MqttException e) {
            Main.handleException(e);
        }
    }
}
