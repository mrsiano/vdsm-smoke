package org.ovirt.vdsm.test.scenarios;

import java.util.List;
import java.util.Map;

import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Different extends Scenario {

    public Different(Map properties) {
        super(properties);
    }

    @Override
    public List<JsonRpcRequest> getRequests() {
        return null;
    }

    @Override
    public List<JsonRpcRequest> responsesToRequests(List<JsonRpcResponse> response) {
        return null;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }
}
