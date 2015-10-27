package org.ovirt.vdsm.test.client;

import org.ovirt.vdsm.jsonrpc.client.reactors.ManagerProvider;

public class ProviderFactory {

    public static ManagerProvider getProvider(String type, String path) {
        if (EngineProvider.TYPE.equals(type)) {
            return new EngineProvider(path);
        } else if (VdsmProvider.TYPE.equals(type)) {
            return new VdsmProvider(path);
        }
        throw new IllegalArgumentException("Not recognized config type");
    }

}
