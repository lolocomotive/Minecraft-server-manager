package de.loicezt.srvmgr;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * The class that holds all of the configuration information read at the beginning of the Main class
 */
@SuppressWarnings({"unused"})
public class ConfigurationHolder {
    private int type;
    private String java;
    private String jarfile;
    private String logLevel;
    private List<WrapperController> servers = new ArrayList<>();

    /**
     * Get the Java executable to use to start the wrapper and the minecraft server
     *
     * @return The java executable path
     */
    public String getJava() {
        return java;
    }

    /**
     * Set the Java executable to use to start the wrapper and the minecraft server
     *
     * @param java the java executable path
     */
    public void setJava(String java) {
        this.java = java;
    }

    /**
     * Gets the file path of the wrapper jar
     *
     * @return The path of the jar
     */
    public String getJarfile() {
        return jarfile;
    }

    /**
     * Sets the jar file path of the wrapper
     *
     * @param jarfile tha path of the wrapper jar
     */
    public void setJarfile(String jarfile) {
        this.jarfile = jarfile;
    }

    /**
     * Get the servers that the server manager is managing
     *
     * @return A {@link List List}&lt;{@link WrapperController WrapperInstance}&gt; containing all the servers that the server manager is managing
     */
    public List<WrapperController> getServers() {
        return servers;
    }

    /**
     * Sets the servers that the server manager is managing
     *
     * @param servers A {@link List List}&lt;{@link WrapperController WrapperInstance}&gt;  containing all the servers that the server manager is managing
     */
    public void setServers(List<WrapperController> servers) {
        this.servers = servers;
    }

    /**
     * Gets the type of the node (0 = {@link de.loicezt.srvmgr.master.Master Master},1 = {@link de.loicezt.srvmgr.wrapper.Wrapper Wrapper})
     *
     * @return The type of the node
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type of the node (0 = {@link de.loicezt.srvmgr.master.Master Master},1 = {@link de.loicezt.srvmgr.wrapper.Wrapper Wrapper})
     *
     * @param type The type of the node
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Get the {@link Level log level} of the program (will apply to wrappers too)
     * @return the log level
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the {@link Level log level} of the program (will apply to wrappers too)
     * @param logLevel the log level
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}
