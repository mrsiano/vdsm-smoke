package org.ovirt.vdsm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.junit.Test;
import org.ovirt.vdsm.test.scenarios.Different;
import org.ovirt.vdsm.test.scenarios.Scenario;

public class ScenarioLoaderTestCase {

    @Test(expected = FileNotFoundException.class)
    public void testFileLoading() throws IOException {
        new ScenarioLoader("/test-patch/first.yaml");
    }

    @Test
    public void testSampleScenario() throws IOException {
        URL file = getClass().getResource("/first.yaml");
        ScenarioLoader loader = new ScenarioLoader(file.getPath());
        assertTrue(Scenario.class.isInstance(loader.getScenario()));
    }

    @Test
    public void testMultiScenario() throws IOException {
        URL file = getClass().getResource("/multiple.yaml");
        ScenarioLoader loader = new ScenarioLoader(file.getPath());
        Scenario base = loader.getScenario();
        assertNotNull(base);
        assertTrue(org.ovirt.vdsm.test.scenarios.First.class.isInstance(base));
        Scenario next = base.getNext();
        assertNotNull(next);
        assertTrue(Different.class.isInstance(next));
    }

    @Test
    public void testProperties() throws IOException {
        URL file = getClass().getResource("/different.yaml");
        ScenarioLoader loader = new ScenarioLoader(file.getPath());
        Different scenario = (Different) loader.getScenario();
        Map<String, String> properties = scenario.getProperties();
        assertEquals("propvalue", properties.get("propname"));
        assertEquals("propvalue2", properties.get("propname2"));
    }
}
