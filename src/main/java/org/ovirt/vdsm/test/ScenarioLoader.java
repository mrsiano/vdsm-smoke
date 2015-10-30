package org.ovirt.vdsm.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.vdsm.test.scenarios.Scenario;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("rawtypes")
public class ScenarioLoader {

    private final static String SCENARIO_PACKAGE = "org.ovirt.vdsm.test.scenarios.";
    private final static String SCENARIO_NAME = "scenario";
    private final static String PROPERTIES = "properties";
    private final static String SCENARIOS_NAME = "scenarios";

    private Map map = new HashMap();
    private String basePath;

    public ScenarioLoader(String path) throws IOException {
        this.map = loadConfig(path);
        this.basePath = new URL("file:" + path).getPath();
    }

    public Scenario getScenario() throws IOException {
        if (this.map.containsKey(SCENARIOS_NAME)) {
            String[] names = this.map.get(SCENARIOS_NAME).toString().split(",");
            Scenario base = null;
            Scenario next = null;
            for (String name : names) {
                Map map = this.loadConfig(this.basePath);
                Scenario scenario = loadScenario(name.trim(), (Map) map.get(PROPERTIES));
                if (base == null) {
                    base = next = scenario;
                } else {
                    next.setNext(scenario);
                    next = scenario;
                }
            }
            return base;
        }
        return loadScenario((String) this.map.get(SCENARIO_NAME), (Map) this.map.get(PROPERTIES));
    }

    private Scenario loadScenario(String name, Map properties) {
        try {
            Class<?> clazz = Class.forName(SCENARIO_PACKAGE + name);
            Constructor<?> constructor = clazz.getConstructor(Map.class);
            return (Scenario) constructor.newInstance(properties);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException("Provided scenario not found due to:", e);
        }
    }

    private Map loadConfig(String path) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream is = new FileInputStream(new File(path))) {
            return (Map) yaml.load(is);
        }
    }
}
