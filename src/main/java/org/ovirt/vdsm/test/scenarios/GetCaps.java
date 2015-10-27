package org.ovirt.vdsm.test.scenarios;

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
    public JsonRpcRequest getRequest() {
        return new RequestBuilder("Host.getCapabilities").build();
    }

    @Override
    public JsonRpcRequest responseToRequest(JsonRpcResponse response) throws ScenarioException {
        return null;
    }

}
