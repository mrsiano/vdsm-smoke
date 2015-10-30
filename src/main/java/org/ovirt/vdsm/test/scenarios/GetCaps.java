package org.ovirt.vdsm.test.scenarios;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;
import org.ovirt.vdsm.jsonrpc.client.RequestBuilder;

@SuppressWarnings("rawtypes")
public class GetCaps extends Scenario {

    public GetCaps(Map properties) {
        super(properties);
    }

    @Override
    public List<JsonRpcRequest> getRequests() {
        return Arrays.asList(new RequestBuilder("Host.getCapabilities").build());
    }

    @Override
    public List<JsonRpcRequest> responsesToRequests(List<JsonRpcResponse> response) throws ScenarioException {
        return null;
    }

}
