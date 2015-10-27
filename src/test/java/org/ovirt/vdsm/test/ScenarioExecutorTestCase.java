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
            throws ClientConnectionException, InterruptedException, ExecutionException, TimeoutException, ScenarioException {
        JsonNode node = mock(JsonNode.class);
        JsonRpcResponse response = new JsonRpcResponse(node, null, node);
        ScenarioExecutor executor = prepareExecutor(response);

        JsonRpcRequest request = mock(JsonRpcRequest.class);
        Scenario first = mock(Scenario.class);
        when(first.getRequest()).thenReturn(request);
        when(first.hasNext()).thenReturn(Boolean.TRUE);
        Scenario different = mock(Scenario.class);
        when(different.responseToRequest(response)).thenReturn(request);
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

        verify(first, times(1)).getRequest();
        verify(first, times(0)).responseToRequest(response);

        verify(different, times(0)).getRequest();
        verify(different, times(1)).responseToRequest(response);
    }

    @Test
    public void testMultipleRepeats()
            throws InterruptedException, ExecutionException, TimeoutException, ClientConnectionException, ScenarioException {
        JsonNode node = mock(JsonNode.class);
        JsonRpcResponse response = new JsonRpcResponse(node, null, node);
        ScenarioExecutor executor = prepareExecutor(response);

        JsonRpcRequest request = mock(JsonRpcRequest.class);
        Scenario scenario = mock(Scenario.class);
        when(scenario.getRequest()).thenReturn(request);
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

        verify(scenario, times(10)).getRequest();
        verify(scenario, times(0)).responseToRequest(response);
    }

    @Test
    public void testMultipleThreads()
            throws InterruptedException, ExecutionException, TimeoutException, ClientConnectionException, ScenarioException {
        JsonNode node = mock(JsonNode.class);
        JsonRpcResponse response = new JsonRpcResponse(node, null, node);
        ScenarioExecutor executor = prepareExecutor(response);

        JsonRpcRequest request = mock(JsonRpcRequest.class);
        Scenario scenario = mock(Scenario.class);
        when(scenario.getRequest()).thenReturn(request);
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

        verify(scenario, times(10)).getRequest();
        verify(scenario, times(0)).responseToRequest(response);
    }

    private ScenarioExecutor prepareExecutor(JsonRpcResponse response)
            throws InterruptedException, ExecutionException, TimeoutException, ClientConnectionException {
        ManagerProvider provider = mock(ManagerProvider.class);
        JsonRpcClient client = mock(JsonRpcClient.class);
        ReactorClient reactorClient = mock(ReactorClient.class);
        when(reactorClient.isInInit()).thenReturn(Boolean.TRUE);
        when(reactorClient.isOpen()).thenReturn(Boolean.TRUE);
        when(client.getClient()).thenReturn(reactorClient);
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get(ScenarioCallable.TIMEOUT, TimeUnit.SECONDS)).thenReturn(response);
        when(client.call(any(JsonRpcRequest.class))).thenReturn(future);
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
