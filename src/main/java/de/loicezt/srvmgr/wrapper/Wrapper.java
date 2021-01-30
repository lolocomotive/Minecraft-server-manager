package de.loicezt.srvmgr.wrapper;

import de.loicezt.srvmgr.Main;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The Wrapper class is instantiated when the node doesn't start as a master node
 */
public class Wrapper {
    public boolean stop = false;

    /**
     * The constructor Connects to the localhost MQTT server and sets itself up to listen for instructions
     */
    public Wrapper() {
        System.out.println("Starting up as WRAPPER node");
        try {
            IMqttClient client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            client.publish("log", new MqttMessage(("Wrapper node " + Main.config.getServerID() + " started").getBytes(StandardCharsets.UTF_8)));
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    //receivedSignal.countDown();
                    switch (payload) {
                        case "stop wrapper":
                            new Thread(() -> {
                                System.out.println("Stopping wrapper !");
                                try {
                                    client.publish(Main.config.getServerID(), new MqttMessage("wrapper stopping".getBytes(StandardCharsets.UTF_8)));
                                    System.out.println("Unsubscribing from topic");
                                    client.unsubscribe(Main.config.getServerID());
                                    System.out.println("Disconnecting Mqtt client");
                                    client.disconnect();
                                    System.out.println("Closing connection");
                                    client.close();
                                    System.out.println("Connection closed");
                                    System.exit(0);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                            break;
                        default:

                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                }
            });
            client.subscribe(Main.config.getServerID(), 1);
            client.publish(Main.config.getServerID(), new
                    MqttMessage("wrapper online".getBytes(StandardCharsets.UTF_8)));
        } catch (MqttException e) {
            Main.handleException(e);
        } //catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }
}
