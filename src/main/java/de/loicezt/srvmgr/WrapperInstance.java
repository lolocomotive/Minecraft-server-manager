package de.loicezt.srvmgr;

import java.io.*;

public class WrapperInstance {

    private String path;
    private String serverID;
    private Status status;
    private Status srvStatus;
    private String startScript = "#!/bin/sh\n" +
            "java -jar wrapper.jar";

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

    public void startWrapper() {
        try {
            File dir = new File("./" + path);
            dir.mkdirs();
            File thisJar = new File("master.jar");
            copyFile(thisJar, new File(dir.getAbsolutePath() + "/wrapper.jar"));
            Runtime.getRuntime().exec(dir.getAbsolutePath() + "/start.sh");
        } catch (IOException ex) {
            System.out.println("Error processing server at path " + path);
        }
    }

}
