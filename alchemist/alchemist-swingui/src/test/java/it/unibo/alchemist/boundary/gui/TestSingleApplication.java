package it.unibo.alchemist.boundary.gui;

import it.unibo.alchemist.boundary.gui.controller.ButtonsBarController;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.monitors.AbstractFXDisplay;
import it.unibo.alchemist.boundary.monitors.FXStepMonitor;
import it.unibo.alchemist.boundary.monitors.FXTimeMonitor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class TestSingleApplication extends Application {
    /**
     * Main layout without nested layouts. Must inject eventual other nested layouts.
     */
    private static final String ROOT_LAYOUT = "RootLayout";

    private static final Logger L = LoggerFactory.getLogger(TestSingleApplication.class);

    private Pane rootLayout;
    private ButtonsBarController buttonsBarController;
    private Optional<AbstractFXDisplay<?>> displayMonitor = Optional.empty();
    private Optional<FXTimeMonitor<?>> timeMonitor = Optional.empty();
    private Optional<FXStepMonitor<?>> stepMonitor = Optional.empty();

    /**
     * Method that launches the application.
     *
     * @param args arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    @SuppressWarnings("unchecked, Duplicates")
    public void start(final Stage primaryStage) throws IOException {
//        final Parameters parameters = getParameters();
//
//        logParams(parameters);
//
//        parameters.getNamed().forEach((key, value) -> {
//            switch (key) {
//                case SingleRunApp.USE_SPECIFIED_DISPLAY_MONITOR_PARAMETER_NAME:
//                    try {
//                        initDisplayMonitor(value);
//                    } catch (final Exception exception) {
//                        L.error("Display monitor not valid");
//                        throw exception;
//                    }
//                    break;
//                case SingleRunApp.USE_DEFAULT_DISPLAY_MONITOR_FOR_ENVIRONMENT_CLASS_PARAMETER_NAME:
//                    if (!displayMonitor.isPresent()) {
//                        if (value == null || value.equals("")) {
//                            displayMonitor = Optional.empty();
//                            L.error("Display monitor not valid");
//                            throw new IllegalArgumentException();
//                        }
//
//                        final Class<? extends Environment<?>> clazz;
//
//                        try {
//                            clazz = (Class<? extends Environment<?>>) Class.forName(value);
//                        } catch (final ClassCastException | ClassNotFoundException exception) {
//                            displayMonitor = Optional.empty();
//                            L.error("Display monitor not valid");
//                            throw new IllegalArgumentException(exception);
//                        }
//
//                        initDisplayMonitor(
//                                clazz.isAssignableFrom(OSMEnvironment.class)
//                                        ? FX2DDisplay.class.getName()
//                                        : FXMapDisplay.class.getName()
//                        );
//                    } else {
//                        L.warn("Display monitor already initialized to " + displayMonitor.get().getClass().getName());
//                    }
//                    break;
//                case SingleRunApp.USE_FX_2D_DISPLAY_PARAMETER_NAME:
//                    if (value == null || Boolean.valueOf(value) || value.equals("")) {
//                        initDisplayMonitor(FX2DDisplay.class.getName());
//                    }
//                    break;
//                case SingleRunApp.USE_FX_MAP_DISPLAY_PARAMETER_NAME:
//                    if (value == null || Boolean.valueOf(value) || value.equals("")) {
//                        initDisplayMonitor(FXMapDisplay.class.getName());
//                    }
//                    break;
//                case SingleRunApp.USE_TIME_MONITOR_PARAMETER_NAME:
//                    if (value == null || Boolean.valueOf(value) || value.equals("")) {
//                        timeMonitor = Optional.of(new FXTimeMonitor<>());
//                    }
//                    break;
//                case SingleRunApp.USE_STEP_MONITOR_PARAMETER_NAME:
//                    if (value == null || Boolean.valueOf(value) || value.equals("")) {
//                        stepMonitor = Optional.of(new FXStepMonitor<>());
//                    }
//                    break;
//                default:
//                    L.warn("Unexpected argument " + SingleRunApp.PARAMETER_NAME_START + key + SingleRunApp.PARAMETER_NAME_END + value);
//            }
//        });

        primaryStage.setTitle(ResourceLoader.getStringRes("main_title"));

        this.rootLayout = FXResourceLoader.getLayout(AnchorPane.class, this, ROOT_LAYOUT);
        final StackPane main = (StackPane) this.rootLayout.getChildren().get(0);
        main.getChildren().add(new Canvas());
        buttonsBarController = new ButtonsBarController();
        main.getChildren().add(FXResourceLoader.getLayout(BorderPane.class, buttonsBarController, "ButtonsBarLayout"));

        primaryStage.getIcons().add(SVGImageUtils.getSvgImage("/icon/icon.svg"));
        primaryStage.setScene(new Scene(this.rootLayout));

        L.debug("Initialization completed");
        primaryStage.show();
    }

    private void logParams(final Parameters params) {
        final StringBuilder rawParamsBuilder = new StringBuilder("Raw parameters: ").append('\n');
        params.getRaw().forEach(p -> rawParamsBuilder.append(p).append('\n'));
        L.debug(rawParamsBuilder.toString());

        final StringBuilder namedParamsBuilder = new StringBuilder("Named parameters: ").append('\n');
        params.getNamed().forEach((n, p) -> namedParamsBuilder.append(n).append(": ").append(p).append('\n'));
        L.debug(namedParamsBuilder.toString());

        final StringBuilder unnamedParamsBuilder = new StringBuilder("Unnamed parameters: ").append('\n');
        params.getUnnamed().forEach(p -> unnamedParamsBuilder.append(p).append('\n'));
        L.debug(unnamedParamsBuilder.toString());
    }

    @SuppressWarnings("unchecked")
    private void initDisplayMonitor(final String className) {
        if (!displayMonitor.isPresent()) {
            if (className == null || className.equals("")) {
                displayMonitor = Optional.empty();
                throw new IllegalArgumentException();
            }

            final Class<? extends AbstractFXDisplay> clazz;

            try {
                clazz = (Class<? extends AbstractFXDisplay<?>>) Class.forName(className);
            } catch (final ClassCastException | ClassNotFoundException exception) {
                displayMonitor = Optional.empty();
                throw new IllegalArgumentException(exception);
            }

            final Constructor[] constructors = clazz.getDeclaredConstructors();
            Constructor constructor = null;
            for (final Constructor c : constructors) {
                if (c.getGenericParameterTypes().length == 0) {
                    constructor = c;
                    break;
                }
            }

            if (constructor == null) {
                displayMonitor = Optional.empty();
                throw new IllegalArgumentException();
            } else {
                try {
                    displayMonitor = Optional.of((AbstractFXDisplay<?>) constructor.newInstance());
                } catch (final IllegalAccessException
                        | IllegalArgumentException
                        | InstantiationException
                        | InvocationTargetException
                        | ExceptionInInitializerError exception) {
                    displayMonitor = Optional.empty();
                    throw new IllegalArgumentException(exception);
                }
            }
        } else {
            L.warn("Display monitor already initialized to " + displayMonitor.get().getClass().getName());
        }
    }
}
