package de.loicezt.srvmgr.logging;

import java.util.logging.*;

public class ConsoleHandler extends java.util.logging.ConsoleHandler {

    public ConsoleHandler() {
        super();
        this.setOutputStream(System.out);
    }
}

