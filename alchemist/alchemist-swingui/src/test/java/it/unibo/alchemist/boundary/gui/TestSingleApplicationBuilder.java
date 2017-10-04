package it.unibo.alchemist.boundary.gui;

import javafx.application.Application;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static it.unibo.alchemist.boundary.gui.SingleRunApp.Parameter.*;

public class TestSingleApplicationBuilder {
    public static final Map<String, String> TEST_PARAMETERS;

    static {
        TEST_PARAMETERS = new HashMap<>();
        TEST_PARAMETERS.put(USE_STEP_MONITOR.getName(), "true");
        TEST_PARAMETERS.put(USE_TIME_MONITOR.getName(), "true");
        TEST_PARAMETERS.put(USE_FX_2D_DISPLAY.getName(), "true");
//        TEST_PARAMETERS.put(USE_FX_MAP_DISPLAY.getName(), "true");
//        TEST_PARAMETERS.put(USE_DEFAULT_DISPLAY_MONITOR_FOR_ENVIRONMENT_CLASS.getName(), OSMEnvironment.class.getName());
//        TEST_PARAMETERS.put(USE_SPECIFIED_DISPLAY_MONITOR.getName(), FX2DDisplay.class.getName());
    }

    public static void main(final String... args) {
//        Application.launch(SingleRunApp.class, getParams(TEST_PARAMETERS));
        Application.launch(SingleRunApp.class);
    }

    private static String getParam(final Map.Entry<String, String> valueNameEntry) {
        return (valueNameEntry.getValue().equals("") ? "" : PARAMETER_NAME_START + valueNameEntry.getKey() + PARAMETER_NAME_END) + valueNameEntry.getValue();
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
