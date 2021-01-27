package de.loicezt.srvmgr.master;

import de.loicezt.srvmgr.Main;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.UUID;

public class Master {
    public Master(){
        System.out.println("Starting up as MASTER node");
        String publisherId = UUID.randomUUID().toString();
        try {
            IMqttClient publisher = new MqttClient("localhost:1883",publisherId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            publisher.connect(options);

        } catch (MqttException e) {
            Main.handleException(e);
        }
    }
}
