package org.ovirt.vdsm.test.scenarios;

import java.util.List;
import java.util.Map;

import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;

@SuppressWarnings("rawtypes")
public abstract class Scenario {

    protected Map properties;

    private Scenario next;

    public Scenario(Map properties) {
        this.properties = properties;
    }

    public void setNext(Scenario scenario) {
        this.next = scenario;
    }

    public Scenario getNext() {
        return this.next;
    }

    public boolean hasNext() {
        return this.next != null;
    }

    public abstract List<JsonRpcRequest> getRequests();

    public abstract List<JsonRpcRequest> responsesToRequests(List<JsonRpcResponse> response) throws ScenarioException;
}
