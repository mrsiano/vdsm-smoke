package org.ovirt.vdsm.test.scenarios;

import static org.ovirt.vdsm.test.scenarios.Utils.getInt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;
import org.ovirt.vdsm.jsonrpc.client.RequestBuilder;

@SuppressWarnings("rawtypes")
public class MonitorCycle extends Scenario {

    private static final String INTERVAL = "interval";

    private final ThreadLocal<Integer> number =
            new ThreadLocal<Integer>() {
                @Override
                protected Integer initialValue() {
                    return 0;
                }
            };

    private int interval;

    public MonitorCycle(Map properties) {
        super(properties);

        this.interval = getInt(this.properties, INTERVAL);
    }

    @Override
    public List<JsonRpcRequest> getRequests() {
        try {
            TimeUnit.SECONDS.sleep(this.interval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Integer counter = this.number.get();
        if (counter != 2) {
            this.number.set(counter + 1);
            return Arrays.asList(new RequestBuilder("Host.getVMList").withParameter("onlyUUID", false).build());
        } else {
            this.number.set(0);
            return Arrays.asList(new RequestBuilder("Host.getAllVmStats").build());
        }
    }

    @Override
    public List<JsonRpcRequest> responsesToRequests(List<JsonRpcResponse> response) throws ScenarioException {
        return null;
    }

}
