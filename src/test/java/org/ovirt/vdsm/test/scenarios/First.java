package org.ovirt.vdsm.test.scenarios;

import java.util.List;
import java.util.Map;

import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;

@SuppressWarnings("rawtypes")
public class First extends Scenario {

    public First(Map properties) {
        super(properties);
    }

    @Override
    public List<JsonRpcRequest> getRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<JsonRpcRequest> responsesToRequests(List<JsonRpcResponse> response) {
        // TODO Auto-generated method stub
        return null;
    }

}
