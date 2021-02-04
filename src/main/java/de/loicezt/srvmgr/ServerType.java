package de.loicezt.srvmgr;

/**
 * Server types (if the server should be persistent or if it can be used for mini-games, which would implicate resetting the map at each startup)
 */
public enum ServerType {
    MINIGAME,
    LOBBY,
    FIXED
}
