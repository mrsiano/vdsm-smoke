package org.ovirt.vdsm.test;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.codehaus.jackson.JsonNode;
import org.junit.Test;
import org.ovirt.vdsm.jsonrpc.client.ClientConnectionException;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcClient;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;
import org.ovirt.vdsm.jsonrpc.client.reactors.ManagerProvider;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorClient;
import org.ovirt.vdsm.test.scenarios.Scenario;
import org.ovirt.vdsm.test.scenarios.ScenarioException;

@SuppressWarnings("unchecked")
public class ScenarioExecutorTestCase {

    @Test
    public void testMultipleScenarios()
            throws ClientConnectionException, InterruptedException, ExecutionException, TimeoutException,
            ScenarioException {
        JsonNode node = mock(JsonNode.class);
        List<JsonRpcResponse> response = Arrays.asList(new JsonRpcResponse(node, null, node));
        ScenarioExecutor executor = prepareExecutor(response);

        List<JsonRpcRequest> request = Arrays.asList(mock(JsonRpcRequest.class));
        Scenario first = mock(Scenario.class);
        when(first.getRequests()).thenReturn(request);
        when(first.hasNext()).thenReturn(Boolean.TRUE);
        Scenario different = mock(Scenario.class);
        when(different.responsesToRequests(response)).thenReturn(request);
        when(different.hasNext()).thenReturn(Boolean.FALSE);
        when(first.getNext()).thenReturn(different);

        File temp = null;
        try {
            temp = File.createTempFile("temp-report", ".tmp");
            executor.submit(first, 1, 1, 1, temp.getAbsolutePath());
        } catch (IOException e) {
            fail();
        } finally {
            if (temp != null) {
                temp.delete();
            }
        }

        verify(first, times(1)).getRequests();
        verify(first, times(0)).responsesToRequests(response);

        verify(different, times(0)).getRequests();
        verify(different, times(1)).responsesToRequests(response);
    }

    @Test
    public void testMultipleRepeats()
            throws InterruptedException, ExecutionException, TimeoutException, ClientConnectionException,
            ScenarioException {
        JsonNode node = mock(JsonNode.class);
        List<JsonRpcResponse> response = Arrays.asList(new JsonRpcResponse(node, null, node));
        ScenarioExecutor executor = prepareExecutor(response);

        List<JsonRpcRequest> request = Arrays.asList(mock(JsonRpcRequest.class));
        Scenario scenario = mock(Scenario.class);
        when(scenario.getRequests()).thenReturn(request);
        when(scenario.hasNext()).thenReturn(Boolean.FALSE);

        File temp = null;
        try {
            temp = File.createTempFile("temp-report", ".tmp");
            executor.submit(scenario, 1, 10, 1, temp.getAbsolutePath());
        } catch (IOException e) {
            fail();
        } finally {
            if (temp != null) {
                temp.delete();
            }
        }

        verify(scenario, times(10)).getRequests();
        verify(scenario, times(0)).responsesToRequests(response);
    }

    @Test
    public void testMultipleThreads()
            throws InterruptedException, ExecutionException, TimeoutException, ClientConnectionException,
            ScenarioException {
        JsonNode node = mock(JsonNode.class);
        List<JsonRpcResponse> response = Arrays.asList(new JsonRpcResponse(node, null, node));
        ScenarioExecutor executor = prepareExecutor(response);

        List<JsonRpcRequest> request = Arrays.asList(mock(JsonRpcRequest.class));
        Scenario scenario = mock(Scenario.class);
        when(scenario.getRequests()).thenReturn(request);
        when(scenario.hasNext()).thenReturn(Boolean.FALSE);

        File temp = null;
        try {
            temp = new File("/tmp");
            executor.submit(scenario, 10, 1, 1, temp.getAbsolutePath());
        } finally {
            if (temp != null) {
                temp.delete();
            }
        }

        verify(scenario, times(10)).getRequests();
        verify(scenario, times(0)).responsesToRequests(response);
    }

    private ScenarioExecutor prepareExecutor(List<JsonRpcResponse> response)
            throws InterruptedException, ExecutionException, TimeoutException, ClientConnectionException {
        ManagerProvider provider = mock(ManagerProvider.class);
        JsonRpcClient client = mock(JsonRpcClient.class);
        ReactorClient reactorClient = mock(ReactorClient.class);
        when(reactorClient.isInInit()).thenReturn(Boolean.TRUE);
        when(reactorClient.isOpen()).thenReturn(Boolean.TRUE);
        when(client.getClient()).thenReturn(reactorClient);
        Future<List<JsonRpcResponse>> future = mock(Future.class);
        when(future.get(ScenarioCallable.TIMEOUT, TimeUnit.SECONDS)).thenReturn(response);
        when(client.batchCall(any(List.class))).thenReturn(future);
        ScenarioExecutor executor = new ScenarioExecutor("localhost", 54321, provider);
        setField(executor, "jsonClient", client);
        return executor;
    }

    private void setField(Object object, String name, Object value) {
        Field field;
        try {
            field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
