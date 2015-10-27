package org.ovirt.vdsm.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ovirt.vdsm.jsonrpc.client.ClientConnectionException;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcClient;
import org.ovirt.vdsm.jsonrpc.client.internal.ResponseWorker;
import org.ovirt.vdsm.jsonrpc.client.reactors.ManagerProvider;
import org.ovirt.vdsm.jsonrpc.client.reactors.Reactor;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorClient;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorFactory;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorType;
import org.ovirt.vdsm.test.client.DefaultStompClientPolicy;
import org.ovirt.vdsm.test.client.DefaultStompConnectionPolicy;
import org.ovirt.vdsm.test.scenarios.Scenario;

public class ScenarioExecutor {

    // default value as used by the engine
    public final static int PARALLELISM = 10;

    private JsonRpcClient jsonClient;

    private ResponseWorker worker;

    private Reactor reactor;

    public ScenarioExecutor(String hostname, int port, ManagerProvider provider) throws ClientConnectionException {
        this.reactor = ReactorFactory.getReactor(provider, ReactorType.STOMP);
        final ReactorClient client = this.reactor.createClient(hostname, port);
        client.setClientPolicy(new DefaultStompConnectionPolicy());
        this.worker = ReactorFactory.getWorker(PARALLELISM);
        this.jsonClient = this.worker.register(client);
        this.jsonClient.setRetryPolicy(new DefaultStompClientPolicy());
    }

    public void submit(Scenario scenario, int threads, int repeat, int time, String reportName)
            throws ClientConnectionException {
        validateConnection();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<ScenarioCallable> callables = populateTasks(scenario, threads, repeat, reportName);
            executor.invokeAll(callables, time, TimeUnit.MINUTES);
            closeTasks(callables);
        } catch (InterruptedException e) {
            System.out.println("Specified time reached cancelling unfinished tasks");
        }
        executor.shutdownNow();
    }

    private List<ScenarioCallable> populateTasks(Scenario scenario, int size, int repeat, String reportName) {
        List<ScenarioCallable> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(new ScenarioCallable(scenario, this.jsonClient, repeat, reportName));
        }
        return list;
    }

    private void closeTasks(List<ScenarioCallable> list) {
        for (ScenarioCallable callable: list) {
            callable.close();
        }
    }

    private void validateConnection() throws ClientConnectionException {
        ReactorClient client = this.jsonClient.getClient();
        while (!client.isInInit()) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new ClientConnectionException("Connecting client interrupted");
            }
        }
        if (!client.isOpen()) {
            throw new ClientConnectionException("Client not connected");
        }
    }

    public void close() throws IOException {
        this.jsonClient.close();
        this.worker.close();
        this.reactor.close();
    }
}
