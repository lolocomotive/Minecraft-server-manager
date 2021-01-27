package de.loicezt.srvmgr;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationHolder {
    private int type;
    private List<WrapperInstance> servers = new ArrayList<>();

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
