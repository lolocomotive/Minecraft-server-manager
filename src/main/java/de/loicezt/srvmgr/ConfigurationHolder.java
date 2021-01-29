package de.loicezt.srvmgr;

import java.util.ArrayList;
import java.util.List;

/**
 * The class that holds all of the configuration information read at the beginning of the Main class
 */
public class ConfigurationHolder {
    private int type;
    private String java;
    private String jarfile;
    private List<WrapperInstance> servers = new ArrayList<>();
    private String serverID;

    public String getJava() {
        return java;
    }

    public void setJava(String java) {
        this.java = java;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getJarfile() {
        return jarfile;
    }

    public void setJarfile(String jarfile) {
        this.jarfile = jarfile;
    }

    public List<WrapperInstance> getServers() {
        return servers;
    }

    public void setServers(List<WrapperInstance> servers) {
        this.servers = servers;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
