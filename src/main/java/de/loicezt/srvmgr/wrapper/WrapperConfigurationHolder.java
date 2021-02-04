package de.loicezt.srvmgr.wrapper;

import de.loicezt.srvmgr.ServerType;

import java.util.List;

public class WrapperConfigurationHolder {
    private String serverID;
    private ServerType serverType;
    private String serverJar;
    private List<String> plugins;
    private List<String> mods;
    private List<String> worlds;
    private List<String> additionalFiles;

    /**
     * Returns the plugins of the server
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     *
     * @return The list of plugins that are added to the server plugins server before it starts
     */
    public List<String> getPlugins() {
        return plugins;
    }

    /**
     * Sets list of plugins that are added to the server plugins server before it starts
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     */
    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    /**
     * Returns the executable of the server
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     * @return The executable of the server (like spigot.jar)
     */
    public String getServerJar() {
        return serverJar;
    }

    public void setServerJar(String serverJar) {
        this.serverJar = serverJar;
    }

    public List<String> getMods() {
        return mods;
    }

    public void setMods(List<String> mods) {
        this.mods = mods;
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public void setWorlds(List<String> worlds) {
        this.worlds = worlds;
    }

    public List<String> getAdditionalFiles() {
        return additionalFiles;
    }

    public void setAdditionalFiles(List<String> additionalFiles) {
        this.additionalFiles = additionalFiles;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }
}
