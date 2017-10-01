package it.unibo.alchemist.boundary.gui;

import it.unibo.alchemist.boundary.gui.controller.ButtonsBarController;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.monitors.*;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static it.unibo.alchemist.boundary.gui.SingleRunApp.Parameter.PARAMETER_NAME_END;
import static it.unibo.alchemist.boundary.gui.SingleRunApp.Parameter.PARAMETER_NAME_START;

/**
 * The class models a non-reusable GUI for simulation display.
 *
 * @param <T> the {@link Concentration} type
 */
public class SingleRunApp<T> extends Application {
    /**
     * Default logger for the class.
     */
    private static final Logger L = LoggerFactory.getLogger(SingleRunApp.class);

    /**
     * Main layout without nested layouts. Must inject eventual other nested layouts.
     */
    private static final String ROOT_LAYOUT = "RootLayout";

    private Collection<EffectGroup> effectGroups;
    private Optional<AbstractFXDisplay<T>> displayMonitor = Optional.empty();
    private Optional<FXTimeMonitor<T>> timeMonitor = Optional.empty();
    private Optional<FXStepMonitor<T>> stepMonitor = Optional.empty();
    private Optional<Integer> closeOperation = Optional.empty();
    private Pane rootLayout;
    private ButtonsBarController buttonsBarController;

    /**
     * Method that launches the application.
     *
     * @param args arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        final Parameters parameters = getParameters();
        parseNamedParams(parameters.getNamed());
        parseUnnamedParams(parameters.getUnnamed());

        try {
            this.rootLayout = FXResourceLoader.getLayout(AnchorPane.class, this, ROOT_LAYOUT);
            final StackPane main = (StackPane) rootLayout.getChildren().get(0);
            main.getChildren().add(displayMonitor.isPresent() ? this.displayMonitor.get() : new Canvas());
            this.buttonsBarController = new ButtonsBarController();
            main.getChildren().add(FXResourceLoader.getLayout(BorderPane.class, buttonsBarController, "ButtonsBarLayout"));
        } catch (final IOException e) {
            L.error("I/O Exception loading FXML layout files", e);
            throw new IllegalStateException(e);
        }

        closeOperation.ifPresent(jfco -> {
            final EventHandler<WindowEvent> handler;
            switch (jfco) {
                case JFrame.HIDE_ON_CLOSE:
                    handler = event -> primaryStage.hide();
                    break;
                case JFrame.DISPOSE_ON_CLOSE:
                    handler = event -> primaryStage.close();
                    break;
                case JFrame.EXIT_ON_CLOSE:
                    handler = event -> {
                        primaryStage.close();
                        // Platform.exit(); // TODO check
                        System.exit(0);
                    };
                    break;
                case JFrame.DO_NOTHING_ON_CLOSE:
                default:
                    handler = event -> { /* Do nothing */ };
            }
            primaryStage.setOnCloseRequest(handler);
        });
        primaryStage.getIcons().add(SVGImageUtils.getSvgImage("/icon/icon.svg"));
        primaryStage.setScene(new Scene(this.rootLayout));

        L.debug("Initialization completed");
        primaryStage.show();
    }

    /**
     * The method parses the given parameters from a key-value map.
     *
     * @param params the map of parameters name and respective values
     * @throws IllegalArgumentException if the value is not valid for the parameter
     * @see Parameters#getNamed()
     */
    @SuppressWarnings("unchecked")
    private void parseNamedParams(final Map<String, String> params) {
        params.forEach((key, value) -> {
            switch (Parameter.getParamFromName(key)) {
                case USE_SPECIFIED_DISPLAY_MONITOR:
                    try {
                        initDisplayMonitor(value);
                    } catch (final Exception exception) {
                        L.error("Display monitor not valid");
                        throw new IllegalArgumentException(exception);
                    }
                    break;
                case USE_DEFAULT_DISPLAY_MONITOR_FOR_ENVIRONMENT_CLASS:
                    if (!displayMonitor.isPresent()) {
                        if (value == null || value.equals("")) {
                            displayMonitor = Optional.empty();
                            L.error("Display monitor not valid");
                            throw new IllegalArgumentException();
                        }

                        final Class<? extends Environment<?>> clazz;

                        try {
                            clazz = (Class<? extends Environment<?>>) Class.forName(value);
                        } catch (final ClassCastException | ClassNotFoundException exception) {
                            displayMonitor = Optional.empty();
                            L.error("Display monitor not valid");
                            throw new IllegalArgumentException(exception);
                        }

                        initDisplayMonitor(
                                clazz.isAssignableFrom(OSMEnvironment.class)
                                        ? FX2DDisplay.class.getName()
                                        : FXMapDisplay.class.getName()
                        );
                    } else {
                        L.warn("Display monitor already initialized to " + displayMonitor.get().getClass().getName());
                    }
                    break;
                case USE_EFFECT_GROUPS_FROM_FILE:
                    try {
                        effectGroups = EffectSerializer.effectGroupsFromFile(new File(value));
                    } catch (final IOException e) {
                        L.warn(e.getMessage());
                        effectGroups = new ArrayList<>(0);
                    }
                    break;
                case USE_CLOSE_OPERATION:
                    final int intValue = Integer.valueOf(value);
                    if (intValue == JFrame.DISPOSE_ON_CLOSE
                            || intValue == JFrame.HIDE_ON_CLOSE
                            || intValue == JFrame.DO_NOTHING_ON_CLOSE
                            || intValue == JFrame.EXIT_ON_CLOSE) {
                        this.closeOperation = Optional.of(intValue);
                    } else {
                        L.error("Close operation " + value + "is not valid");
                        throw new IllegalArgumentException();
                    }
                default:
                    L.warn("Unexpected argument " + PARAMETER_NAME_START + key + PARAMETER_NAME_END + value);
            }
        });
    }

    /**
     * The method parses the given parameters from a list.
     *
     * @param params the list of parameters
     * @see Parameters#getUnnamed()
     */
    private void parseUnnamedParams(final List<String> params) {
        params.forEach(param -> {
            L.debug("The given unnamed param is: " + param);
            switch (Parameter.getParamFromName(
                    param.startsWith(PARAMETER_NAME_START)
                            ? param.substring(PARAMETER_NAME_START.length())
                            : param)) {
                case USE_FX_2D_DISPLAY:
                    initDisplayMonitor(FX2DDisplay.class.getName());
                    break;
                case USE_FX_MAP_DISPLAY:
                    initDisplayMonitor(FXMapDisplay.class.getName());
                    break;
                case USE_TIME_MONITOR:
                    timeMonitor = Optional.of(new FXTimeMonitor<>());
                    break;
                case USE_STEP_MONITOR:
                    stepMonitor = Optional.of(new FXStepMonitor<>());
                    break;
                default:
                    L.warn("Unexpected argument " + PARAMETER_NAME_START + param);
            }
        });
    }

    /**
     * Initializes a new {@link AbstractFXDisplay} for the specified {@link Class#getName()}.
     * <p>
     * If not valid, the displayMonitor is initialized to {@link Optional#EMPTY null}.
     * <p>
     * If {@link #displayMonitor} is already initialized, it does nothing.
     *
     * @param className the name of the {@code AbstractFXDisplay} {@link OutputMonitor} to be inizialized
     * @throws IllegalArgumentException if the class name is null, empty or not an {@link AbstractFXDisplay},
     *                                  or the {@link AbstractFXDisplay} does not have a 0 arguments constructor
     * @see Class#forName(String)
     */
    @SuppressWarnings("unchecked")
    private void initDisplayMonitor(final String className) {
        if (!displayMonitor.isPresent()) {
            if (className == null || className.equals("")) {
                displayMonitor = Optional.empty();
                throw new IllegalArgumentException();
            }

            final Class<? extends AbstractFXDisplay> clazz;

            try {
                clazz = (Class<? extends AbstractFXDisplay>) Class.forName(className);
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
                    displayMonitor = Optional.of((AbstractFXDisplay<T>) constructor.newInstance());
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

    /**
     * An enum representation of the parameters supported by {@link SingleRunApp} application class.
     *
     * @see Application#getParameters()
     * @see Parameters
     */
    public enum Parameter {
        USE_SPECIFIED_DISPLAY_MONITOR("use-display-monitor", true),
        USE_DEFAULT_DISPLAY_MONITOR_FOR_ENVIRONMENT_CLASS("use-default-display-monitor-for-environment", true),
        USE_FX_2D_DISPLAY("use-FX2DDisplay-monitor", false),
        USE_FX_MAP_DISPLAY("use-FXMapDisplay-monitor", false),
        USE_TIME_MONITOR("use-time-monitor", false),
        USE_STEP_MONITOR("use-step-monitor", false),
        USE_EFFECT_GROUPS_FROM_FILE("use-effect-groups-from-file", true),
        USE_CLOSE_OPERATION("use-close-operation", true);

        public static final String PARAMETER_NAME_START = "--";
        public static final String PARAMETER_NAME_END = "=";

        private final String name;
        private final boolean isNamed;

        /**
         * Default constructor.
         *
         * @param name    the name of the param
         * @param isNamed the named property of the param
         */
        Parameter(final String name, final boolean isNamed) {
            this.name = name;
            this.isNamed = isNamed;
        }

        /**
         * Utility method to get a param from its name.
         *
         * @param name the name of the param to get
         * @return the param with given name
         */
        public static Parameter getParamFromName(final String name) {
            for (final Parameter p : values()) {
                if (name.equals(p.name)) {
                    return p;
                }
            }
            throw new IllegalArgumentException();
        }

        /**
         * Returns all the named params.
         *
         * @return the named params
         * @see Parameters#getNamed()
         */
        public static List<Parameter> getNamedParams() {
            return Arrays.stream(values())
                    .filter(Parameter::isNamed)
                    .collect(Collectors.toList());
        }

        /**
         * Returns all the unnamed params.
         *
         * @return the unnamed params
         * @see Parameters#getUnnamed()
         */
        public static List<Parameter> getUnnamedParams() {
            return Arrays.stream(values())
                    .filter(p -> !p.isNamed())
                    .collect(Collectors.toList());
        }

        /**
         * The method returns a list of Strings containing all the params in an easy printable way; named and unnamed params are distinguishable.
         *
         * @return a list of Strings containing each param
         */
        public static List<String> getPrintableParamsList() {
            return Arrays.stream(values())
                    .map(p -> {
                        String name = PARAMETER_NAME_START + p.getName();
                        if (p.isNamed) {
                            name += PARAMETER_NAME_END + " ... ";
                        }
                        return name;
                    })
                    .collect(Collectors.toList());
        }

        /**
         * Getter method for the name with the param is called and interpreted with.
         *
         * @return the name of the param
         */
        public final String getName() {
            return this.name;
        }

        /**
         * Getter method for the named property of the param.
         *
         * @return the named property of the param
         */
        public boolean isNamed() {
            return isNamed;
        }
    }
}
