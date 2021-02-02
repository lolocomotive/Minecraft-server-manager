package de.loicezt.srvmgr;

import de.loicezt.srvmgr.master.Master;
import de.loicezt.srvmgr.wrapper.Wrapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.*;

/**
 * The class that allows communication between the {@link Master Master} and {@link Wrapper Wrapper} nodes
 */
public class WrapperInstance {

    private String path;
    private String serverID;
    private Status status;
    private Status srvStatus;
    private Process process;
    private long timeout = 1000;
    private Thread logger;

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

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    private Types type;

    public Types getType() {
        return type;
    }

    public void setType(Types type) {
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
     * Copy a file
     *
     * @param from The source file
     * @param to   The destination File
     * @throws IOException
     */
    public static void copyFile(File from, File to)
            throws IOException {
        try (
                InputStream in = new BufferedInputStream(
                        new FileInputStream(from));
                OutputStream out = new BufferedOutputStream(
                        new FileOutputStream(to))) {

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        }
    }

    /**
     * Starts the wrapper associated with this instance
     */
    public void startWrapper() {
        try {
            File dir = new File("./" + path);
            dir.mkdirs();
            File thisJar = new File("./" + Main.config.getJarfile());
            copyFile(thisJar, new File(dir.getAbsolutePath() + "/wrapper.jar"));
            File startupScript = new File(dir.getAbsolutePath() + "/start.sh");
            if (!startupScript.exists()) {
                try {
                    Master.log("Writing default startup script for wrapper " + path);
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
                    Main.handleException(e);
                }
            }
            File config = new File(dir.getAbsolutePath() + "/config.yml");
            if (!config.exists()) {
                try {
                    Master.log("Writing default wrapper config for wrapper " + path);
                    config.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(config));
                    String startScript = "type: 1\nserverID: " + path;
                    writer.write(startScript);
                    writer.flush();
                    writer.close();
                    config.setExecutable(true);
                } catch (IOException e) {
                    Main.handleException(e);
                }
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
            Master.logErr("Error processing server at path " + path);
            ex.printStackTrace();
        }
    }

    /**
     * Stops the wrapper associated with this instance
     *
     * @param client The MqttClient which should be used for sending instructions
     */
    public void stop(MqttClient client) {
        try {
            Main.mqttMsgSend(path, "stop wrapper", client);
            Master.log("Waiting for wrapper " + path + " to stop");
            while (process.isAlive()) {
            }
            client.unsubscribe(path);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
