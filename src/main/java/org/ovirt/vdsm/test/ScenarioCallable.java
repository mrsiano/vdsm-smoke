package org.ovirt.vdsm.test;

import static com.codahale.metrics.MetricRegistry.*;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.vdsm.jsonrpc.client.ClientConnectionException;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcClient;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;
import org.ovirt.vdsm.test.scenarios.Scenario;
import org.ovirt.vdsm.test.scenarios.ScenarioException;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class ScenarioCallable implements Callable<Object> {

    public static final int TIMEOUT = 2;
    private Scenario scenario;
    private JsonRpcClient client;
    private int times;
    private CsvReporter reporter;
    private MetricRegistry registry;

    public ScenarioCallable(Scenario scenario, JsonRpcClient client, int times, String reportName) {
        this.scenario = scenario;
        this.client = client;
        this.times = times;

        this.registry = new MetricRegistry();
        this.reporter = CsvReporter.forRegistry(this.registry)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.MILLISECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(new File(reportName));
        this.reporter.start(200, TimeUnit.MILLISECONDS);
    }

    @Override
    public Object call() throws Exception {
        try {
            final Timer timer = registry.timer(name(scenario.getClass(), "requests"));
            for (int i = 0; i < this.times; i++) {
                JsonRpcRequest request = scenario.getRequest();
                this.runScenario(request, this.scenario, timer);
            }
            this.reporter.report();
        } catch (ScenarioException ignored) {
            // making sure we are done when interrupted
        }
        return null;
    }

    private void runScenario(JsonRpcRequest request, Scenario scenario, Timer timer) throws ScenarioException {
        try {
            final Timer.Context context = timer.time();
            JsonRpcResponse response = null;
            try {
                Future<JsonRpcResponse> future = this.client.call(request);
                response = future.get(TIMEOUT, TimeUnit.SECONDS);
            } finally {
                context.stop();
            }
            if (scenario.hasNext()) {
                Scenario next = scenario.getNext();
                this.runScenario(next.responseToRequest(response), next, timer);
            }
        } catch (ClientConnectionException | TimeoutException e) {
            System.err.println("Connectivity issue occured");
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Issue occured during running scenario: " + e.getMessage());
            throw new ScenarioException(e);
        }
    }

    public void close() {
        this.reporter.close();
    }
}
