package it.unibo.alchemist.boundary.gui;

import it.unibo.alchemist.boundary.monitors.FX2DDisplay;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import javafx.application.Application;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestSingleApplicationBuilder {
    public static final Map<String, String> TEST_PARAMETERS;

    static {
        TEST_PARAMETERS = new HashMap<>();
        TEST_PARAMETERS.put(SingleRunApp.USE_STEP_MONITOR_PARAMETER_NAME, "true");
        TEST_PARAMETERS.put(SingleRunApp.USE_TIME_MONITOR_PARAMETER_NAME, "true");
        TEST_PARAMETERS.put(SingleRunApp.USE_FX_2D_DISPLAY_PARAMETER_NAME, "true");
        TEST_PARAMETERS.put(SingleRunApp.USE_FX_MAP_DISPLAY_PARAMETER_NAME, "true");
        TEST_PARAMETERS.put(SingleRunApp.USE_DEFAULT_DISPLAY_MONITOR_FOR_ENVIRONMENT_CLASS_PARAMETER_NAME, OSMEnvironment.class.getName());
        TEST_PARAMETERS.put(SingleRunApp.USE_SPECIFIED_DISPLAY_MONITOR_PARAMETER_NAME, FX2DDisplay.class.getName());
    }

    public static void main(final String... args) {
        Application.launch(SingleRunApp.class, getParams(TEST_PARAMETERS));
    }

    private static String getParam(final Map.Entry<String, String> valueNameEntry) {
        return (valueNameEntry.getValue().equals("") ? "" : SingleRunApp.PARAMETER_NAME_START + valueNameEntry.getKey() + SingleRunApp.PARAMETER_NAME_END) + valueNameEntry.getValue();
    }

    public static String[] getParams(final Map<String, String> valueNameParamsMap) {
        return valueNameParamsMap
                .entrySet()
                .stream()
                .map(TestSingleApplicationBuilder::getParam)
                .collect(Collectors.toList())
                .toArray(new String[]{});
    }
}
