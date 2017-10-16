package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.gui.view.params.Parameter;
import javafx.application.Application;
import org.jooq.lambda.tuple.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestSingleApplication {
    public static final Map<String, String> TEST_PARAMETERS;

    static {
        TEST_PARAMETERS = new HashMap<>();
        TEST_PARAMETERS.put(Parameter.USE_STEP_MONITOR.getName(), "");
        TEST_PARAMETERS.put(Parameter.USE_TIME_MONITOR.getName(), "");
        TEST_PARAMETERS.put(Parameter.USE_FX_2D_DISPLAY.getName(), "");
//        TEST_PARAMETERS.put(SingleRunApp.Parameter.USE_FX_MAP_DISPLAY.getName(), "");
//        TEST_PARAMETERS.put(SingleRunApp.Parameter.USE_DEFAULT_DISPLAY_MONITOR_FOR_ENVIRONMENT_CLASS.getName(), OSMEnvironment.class.getName());
//        TEST_PARAMETERS.put(SingleRunApp.Parameter.USE_SPECIFIED_DISPLAY_MONITOR.getName(), FX2DDisplay.class.getName());
    }

    public static void main(final String... args) {
        Application.launch(TestApplication.class);
    }

    public static String[] getParams(final Map<String, String> valueNameParamsMap) {
        return valueNameParamsMap
                .entrySet()
                .stream()
                .map(e -> new Tuple2<>(e.getKey(), e.getValue()))
                .map(Parameter::getParam)
                .collect(Collectors.toList())
                .toArray(new String[]{});
    }
}
