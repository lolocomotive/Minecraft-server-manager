package de.loicezt.srvmgr.wrapper;

import de.loicezt.srvmgr.ServerType;

import java.util.List;
@SuppressWarnings({"Unused"})
public class WrapperConfigurationHolder {
    private String serverID;
    private ServerType serverType;
    private String serverJar;
    private List<String> plugins;
    private List<String> mods;
    private List<String> worlds;
    private List<String> additionalFiles;

    /**
     * Sets the plugins of the server
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
     *
     * @param plugins The list of plugins that are added to the server plugins server before it starts
     */
    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    /**
     * Gets the path of the server executable jar file
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     *
     * @return The path of the server executable (like spigot.jar)
     */
    public String getServerJar() {
        return serverJar;
    }

    /**
     * Sets the path of the server executable jar file
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     *
     * @param serverJar The path of the server jar file (like spigot.jar)
     */
    public void setServerJar(String serverJar) {
        this.serverJar = serverJar;
    }

    /**
     * Gets a list of the paths of the mods to add
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     *
     * @return A {@link List}&lt;{@link String}&gt; containing the paths of the mods to add
     */
    public List<String> getMods() {
        return mods;
    }

    /**
     * Sets a list of the paths of the mods to add
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     *
     * @param mods A {@link List}&lt;{@link String}&gt; containing the paths of the mods to add
     */
    public void setMods(List<String> mods) {
        this.mods = mods;
    }

    /**
     * Gets a list of the paths of the worlds to add
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     *
     * @return A {@link List}&lt;{@link String}&gt; containing the paths of the worlds to add
     */
    public List<String> getWorlds() {
        return worlds;
    }

    /**
     * Sets a list of the paths of the mods to add
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     *
     * @param worlds A {@link List}&lt;{@link String}&gt; containing the paths of the mods to add
     */
    public void setWorlds(List<String> worlds) {
        this.worlds = worlds;
    }
    /**
     * Gets a list of the paths of the additional files to add
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     *
     * @return A {@link List}&lt;{@link String}&gt; containing the paths of the additional files such as server.properties,start.sh or eula.txt to add
     */
    public List<String> getAdditionalFiles() {
        return additionalFiles;
    }

    /**
     * Sets a list of the paths of the additional files to add
     * <p>Will only work if the server is a {@link ServerType#MINIGAME minigame} server</p>
     *
     * @param additionalFiles A {@link List}&lt;{@link String}&gt; containing the paths of the additional files such as server.properties,start.sh or eula.txt to add
     */
    public void setAdditionalFiles(List<String> additionalFiles) {
        this.additionalFiles = additionalFiles;
    }
    /**
     * Sets the path of the server
     *
     * @return The path of the server
     */
    public String getServerID() {
        return serverID;
    }

    /**
     * Sets the path of the server
     *
     * @param serverID The path of the server
     */
    public void setServerID(String serverID) {
        this.serverID = serverID;
    }
    /**
     * Gets the server Type
     *
     * @return The {@link ServerType server type} that the server is going to be ({@link ServerType#MINIGAME minigame},{@link ServerType#FIXED fixed} or {@link ServerType#LOBBY lobby})
     */
    public ServerType getServerType() {
        return serverType;
    }

    /**
     * Sets the server Type
     *
     * @param serverType The {@link ServerType server type} that the server is going to be ({@link ServerType#MINIGAME minigame},{@link ServerType#FIXED fixed} or {@link ServerType#LOBBY lobby})
     */
    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }
}
