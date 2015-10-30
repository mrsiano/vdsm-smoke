package org.ovirt.vdsm.test.scenarios;

import static org.ovirt.vdsm.test.scenarios.Utils.getInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;
import org.ovirt.vdsm.jsonrpc.client.RequestBuilder;
import org.ovirt.vdsm.jsonrpc.client.ResponseDecomposer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GetVmStats extends Scenario {

    private static final String RATIO = "ratio";
    private Integer ratio;

    public GetVmStats(Map properties) {
        super(properties);

        this.ratio = getInt(properties, RATIO);
    }

    @Override
    public List<JsonRpcRequest> getRequests() {
        return null;
    }

    @Override
    public List<JsonRpcRequest> responsesToRequests(List<JsonRpcResponse> responses) throws ScenarioException {
        List<JsonRpcRequest> list = new ArrayList<>();
        for (JsonRpcResponse response : responses) {
            if (response.getError() != null) {
                throw new IllegalStateException();
            }
            ResponseDecomposer decomposer = new ResponseDecomposer(response);
            Object[] vms = (Object[]) decomposer.decomposeResponse(Object[].class);

            for (Object vm : vms) {
                Map<String, Object> map = (Map<String, Object>) vm;
                if (ThreadLocalRandom.current().nextInt(0, this.ratio + 1) == 0) {
                    list.add(new RequestBuilder("VM.getStats").withParameter("vmID", map.get("vmId")).build());
                }
            }
        }
        return list;
    }

}
