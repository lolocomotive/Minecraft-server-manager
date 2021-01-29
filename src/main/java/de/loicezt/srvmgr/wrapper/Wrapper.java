package de.loicezt.srvmgr.wrapper;

import de.loicezt.srvmgr.Main;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The Wrapper class is instantiated when the node doesn't start as a master node
 */
public class Wrapper {
    /**
     * The constructor Connects to the localhost MQTT server and sets itself up to listen for instructions
     */
    public Wrapper() {
        System.out.println("Starting up as WRAPPER node");
        String publisherId = UUID.randomUUID().toString();
        try {
            IMqttClient client = new MqttClient("tcp://localhost:1883", publisherId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            client.connect(options);
            client.publish("log", new MqttMessage(("Wrapper node " + Main.config.getServerID() + " started").getBytes(StandardCharsets.UTF_8)));
            //CountDownLatch receivedSignal = new CountDownLatch(10);
            client.subscribe(Main.config.getServerID(), (topic, msg) -> {
                String payload = new String(msg.getPayload(), StandardCharsets.UTF_8);
                //receivedSignal.countDown();
                switch (payload) {
                    case "stop wrapper":
                        System.out.println("Stopping wrapper !");
                        client.publish(Main.config.getServerID(), new MqttMessage("wrapper stopping".getBytes(StandardCharsets.UTF_8)));
                        client.disconnectForcibly(1);
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unrecognized instruction \"" + payload + "\"");
                }
            });
            //receivedSignal.await(1, TimeUnit.MINUTES);
            client.publish(Main.config.getServerID(), new MqttMessage("wrapper online".getBytes(StandardCharsets.UTF_8)));
        } catch (MqttException e) {
            Main.handleException(e);
        } //catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }
}
