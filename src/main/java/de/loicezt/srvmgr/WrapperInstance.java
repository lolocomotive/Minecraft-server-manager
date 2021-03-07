package de.loicezt.srvmgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.loicezt.srvmgr.master.Master;
import de.loicezt.srvmgr.wrapper.Wrapper;
import de.loicezt.srvmgr.wrapper.WrapperConfigurationHolder;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.*;
import java.util.logging.Logger;

/**
 * The class that allows communication between the {@link Master Master} and {@link Wrapper Wrapper} nodes
 */
public class WrapperInstance {

    private String path;
    private Status status;
    private Status srvStatus;
    private Process process;
    private ServerType type;
    private WrapperConfigurationHolder wConfig;
    private Logger logger = Logger.getLogger(WrapperInstance.class.getName());

    public WrapperConfigurationHolder getwConfig() {
        return wConfig;
    }

    public void setwConfig(WrapperConfigurationHolder wConfig) {
        this.wConfig = wConfig;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ServerType getType() {
        return type;
    }

    public void setType(ServerType type) {
        this.type = type;
    }

    public Status getSrvStatus() {
        return srvStatus;
    }

    public void setSrvStatus(Status srvStatus) {
        this.srvStatus = srvStatus;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Starts the wrapper associated with this instance
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void startWrapper() {
        try {
            File dir = new File("./" + path);
            dir.mkdirs();
            File thisJar = new File("./" + Main.config.getJarfile());
            ExtensionMethods.copyFile(thisJar, new File(dir.getAbsolutePath() + "/wrapper.jar"));
            File startupScript = new File(dir.getAbsolutePath() + "/start.sh");
            try {
                logger.info("Writing default startup script for wrapper " + path);
                startupScript.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(startupScript));
                String startScript = "#!/bin/sh\n" +
                        "cd ./" + path + "\n" +
                        Main.config.getJava() + " -jar wrapper.jar #>> log.txt";
                writer.write(startScript);
                writer.flush();
                writer.close();
                startupScript.setExecutable(true);
            } catch (IOException e) {
                ExtensionMethods.handleException(e);
            }

            File config = new File(dir.getAbsolutePath() + "/config.yml");

            try {
                logger.info("Writing default config for wrapper " + path);
                config.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(config));
                String cfgContent = "type: 1";
                writer.write(cfgContent);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                ExtensionMethods.handleException(e);
            }
            File wConfig = new File(dir.getAbsolutePath() + "/wrapper.yml");

            try {
                logger.info("Writing default wConfig for wrapper " + path);
                config.createNewFile();
                new ObjectMapper(new YAMLFactory()).writeValue(wConfig, getwConfig());
            } catch (IOException e) {
                ExtensionMethods.handleException(e);
            }

            Master.log("Starting wrapper " + path);
            process = Runtime.getRuntime().exec("sh " + path + "/start.sh");
            logger = new Thread(() -> {
                try {
                    Master.log("Initializing logging", path);
                    BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(process.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(process.getErrorStream()));
                    String in = null, err = null;
                    while (process.isAlive() || ((in = stdInput.readLine()) != null) || ((err = stdError.readLine()) != null)) {
                        if (in != null)
                            Master.log(in, path);
                        if (err != null)
                            Master.logErr(err, path);
                    }
                    stdInput.close();
                    stdError.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Master.log("Logging terminated", path);
            });
            logger.start();
        } catch (IOException ex) {
            logger.severe("Error processing server at path " + path);
            ex.printStackTrace();
        }
    }

    /**
     * Stops the wrapper associated with this instance
     *
     * @param client The MqttClient which should be used for sending instructions
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public void stop(MqttClient client) {
        try {
            ExtensionMethods.mqttMsgSend(path, "stop wrapper", client);
            logger.info("Waiting for wrapper " + path + " to stop");
            while (process.isAlive()) {
            }
            client.unsubscribe(path);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
