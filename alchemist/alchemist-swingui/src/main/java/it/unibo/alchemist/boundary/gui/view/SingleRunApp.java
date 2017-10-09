package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.gui.controller.ButtonsBarController;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.monitors.*;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static it.unibo.alchemist.boundary.gui.view.SingleRunApp.Parameter.PARAMETER_NAME_END;
import static it.unibo.alchemist.boundary.gui.view.SingleRunApp.Parameter.PARAMETER_NAME_START;

/**
 * The class models a non-reusable GUI for simulation display.
 *
 * @param <T> the {@link Concentration} type
 */
public class SingleRunApp<T> extends Application {
    /**
     * Main layout without nested layouts. Must inject eventual other nested layouts.
     */
    public static final String ROOT_LAYOUT = "RootLayout";
    /**
     * Default logger for the class.
     */
    private static final Logger L = LoggerFactory.getLogger(SingleRunApp.class);
    private final Map<String, String> namedParams = new HashMap<>();
    private final List<String> unnamedParams = new ArrayList<>();
    private boolean initialized = false;
    private Collection<EffectGroup> effectGroups;
    private Optional<Simulation<T>> simulation = Optional.empty();
    private Optional<AbstractFXDisplay<T>> displayMonitor = Optional.empty();
    private Optional<FXTimeMonitor<T>> timeMonitor = Optional.empty();
    private Optional<FXStepMonitor<T>> stepMonitor = Optional.empty();
    private Optional<Integer> closeOperation = Optional.empty();
    private Pane rootLayout;
    private ButtonsBarController buttonsBarController;

    /**
     * Method that launches the application.
     *
     * @param args {@link Parameter parameters} for the application
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    /**
     * Converts a {@link JFrame} closeOperation to an {@link EventHandler} to use in a JavaFX {@link Stage}.
     *
     * @param jfco  the JFrame close operation
     * @param stage the stage to manage
     * @return the {@code EventHandler} that should be set to the provided stage
     * @throws IllegalArgumentException if jfco is not a valid JFrame close operation, or if stage is null
     */
    protected static EventHandler<WindowEvent> parseJFrameCloseOperation(final int jfco, final Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException();
        }

        final EventHandler<WindowEvent> handler;
        switch (jfco) {
            case JFrame.HIDE_ON_CLOSE:
                handler = event -> stage.hide();
                break;
            case JFrame.DISPOSE_ON_CLOSE:
                handler = event -> stage.close();
                break;
            case JFrame.EXIT_ON_CLOSE:
                handler = event -> {
                    stage.close();
                    // Platform.exit(); // TODO check
                    System.exit(0);
                };
                break;
            case JFrame.DO_NOTHING_ON_CLOSE:
            default:
                throw new IllegalArgumentException();
        }
        return handler;
    }

    /**
     * Getter method for the unnamed parameters.
     *
     * @return the unnamed params
     * @see Parameters#getUnnamed()
     */
    protected List<String> getUnnamedParams() {
        if (unnamedParams.isEmpty()) {
            Optional.of(getParameters()).ifPresent(p -> unnamedParams.addAll(p.getUnnamed()));
        }
        return this.unnamedParams;
    }

    /**
     * Getter method for the named parameters.
     *
     * @return the named params
     */
    protected Map<String, String> getNamedParams() {
        if (namedParams.isEmpty()) {
            Optional.of(getParameters()).ifPresent(p -> namedParams.putAll(p.getNamed()));
        }
        return this.namedParams;
    }

    /**
     * The method adds a new named parameter.
     *
     * @param key   the param name
     * @param value the param value
     * @throws IllegalArgumentException if the parameter is not valid, or if {@link Parameter#isNamed() it's not named}
     * @throws IllegalStateException    if the application is already started
     * @see Parameters#getNamed()
     * @see Parameter
     * @see #addNamedParam(Parameter, String)
     */
    public void addNamedParam(final String key, final String value) {
        // Calling Parameter.getParamFromName makes sure that param is valid
        addNamedParam(Parameter.getParamFromName(key), value);
    }

    /**
     * The method adds a new named parameter.
     *
     * @param param the param
     * @param value the param value
     * @throws IllegalArgumentException if {@link Parameter#isNamed() it's not named}
     * @throws IllegalStateException    if the application is already started
     * @see Parameters#getNamed()
     */
    public void addNamedParam(final Parameter param, final String value) {
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }
        if (!param.isNamed()) {
            throw new IllegalArgumentException("The given param is not named");
        }
        namedParams.put(param.getName(), value);
    }

    /**
     * The method adds a new named parameter.
     *
     * @param param the param name
     * @throws IllegalArgumentException if the parameter is not valid, or if {@link Parameter#isNamed() it's named}
     * @throws IllegalStateException    if the application is already started
     * @see Parameters#getUnnamed()
     * @see Parameter
     * @see #addUnnamedParam(Parameter)
     */
    public void addUnnamedParam(final String param) {
        // Calling Parameter.getParamFromName makes sure that param is valid
        addUnnamedParam(Parameter.getParamFromName(
                param.startsWith(PARAMETER_NAME_START)
                        ? param.substring(PARAMETER_NAME_START.length())
                        : param));
    }

    /**
     * The method adds a new named parameter.
     *
     * @param param the param
     * @throws IllegalArgumentException if {@link Parameter#isNamed() it's not named}
     * @throws IllegalStateException    if the application is already started
     * @see Parameters#getUnnamed()
     */
    public void addUnnamedParam(final Parameter param) {
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }
        if (param.isNamed()) {
            throw new IllegalArgumentException("The given param is named");
        }
        unnamedParams.add(param.getName());
    }

    /**
     * The method sets the parameters. All previously add params will be removed.
     *
     * @param params the params
     * @throws IllegalStateException if the application is already started
     * @see Application#getParameters()
     */
    public void setParams(final String[] params) {
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }

        namedParams.clear();
        unnamedParams.clear();

        Arrays.stream(params)
                .forEach(p -> {
                    if (p.startsWith(PARAMETER_NAME_START)) {
                        final String param = p.substring(PARAMETER_NAME_START.length());
                        if (param.contains(PARAMETER_NAME_END)) {
                            final int splitterIntex = param.lastIndexOf(PARAMETER_NAME_END);
                            addNamedParam(param.substring(0, splitterIntex), param.substring(splitterIntex));
                        } else {
                            addUnnamedParam(param);
                        }
                    } else {
                        throw new IllegalArgumentException("The parameter " + p + " is not valid");
                    }
                });
    }

    @Override
    public void start(final Stage primaryStage) {
        parseNamedParams(getNamedParams());
        parseUnnamedParams(getUnnamedParams());

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

        closeOperation.ifPresent(jfco -> primaryStage.setOnCloseRequest(parseJFrameCloseOperation(jfco, primaryStage)));
        primaryStage.getIcons().add(SVGImageUtils.getSvgImage("/icon/icon.svg"));
        primaryStage.setScene(new Scene(this.rootLayout));

        initialized = true;
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
            try {
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
            } catch (final IllegalArgumentException e) {
                L.warn("Invalid argument: " + param, e);
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
     * Setter method for the collection of groups of effects.
     *
     * @param effectGroups the groups of effects
     * @throws IllegalStateException if the application is already started
     */
    public void setEffectGroups(final Collection<EffectGroup> effectGroups) {
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }

        this.effectGroups = effectGroups;
    }

    /**
     * Setter method for simulation.
     *
     * @param simulation the simulation this {@link Application} will display
     * @throws IllegalStateException if the application is already started
     */
    public void setSimulation(final Simulation<T> simulation) {
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }
        this.simulation = Optional.of(simulation);
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
         * The method builds a parameter from a tuple of strings.
         *
         * @param valueNameCouple the key-value tuple of the param
         * @return the correctly formatted param
         */
        public static String getParam(final Tuple2<String, String> valueNameCouple) {
            return (valueNameCouple.v2().equals("") ? "" : PARAMETER_NAME_START + valueNameCouple.v1() + PARAMETER_NAME_END) + valueNameCouple.v2();
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
            return this.isNamed;
        }
    }

    /**
     * Main class to start an empty simulator visualization.
     */
    public abstract static class AbstractBuilder<T> {
        private final Simulation<T> simulation;
        private boolean monitorDisplay;
        private boolean monitorTime;
        private boolean monitorSteps;

        /**
         * Default constructor of the builder.
         *
         * @param simulation the simulation to build the view for
         */
        public AbstractBuilder(final Simulation<T> simulation) {
            this.simulation = simulation;
            this.monitorDisplay = false;
            this.monitorTime = false;
            this.monitorSteps = false;
        }

        /**
         * Specify if the GUI should initialize an {@link OutputMonitor} that will graphically show the simulation using {@link EffectFX effects}.
         *
         * @param monitorDisplay true if the GUI should initialize the {@link OutputMonitor} as a {@link Canvas}
         * @return this builder
         * @see FX2DDisplay
         * @see FXMapDisplay
         */
        public AbstractBuilder<T> monitorDisplay(final boolean monitorDisplay) {
            this.monitorDisplay = monitorDisplay;
            return this;
        }

        /**
         * Specify if the GUI should initialize an {@link OutputMonitor} that will graphically show the step progress.
         *
         * @param monitorSteps true if the GUI should initialize the {@link OutputMonitor} as a {@link Label}
         * @return this builder
         * @see FXStepMonitor
         */
        public AbstractBuilder<T> monitorSteps(final boolean monitorSteps) {
            this.monitorSteps = monitorSteps;
            return this;
        }

        /**
         * Set the default {@link OutputMonitor} that will graphically show the time progress.
         *
         * @param monitorTime
         * @return this builder
         */
        public AbstractBuilder<T> monitorTime(final boolean monitorTime) {
            this.monitorTime = monitorTime;
            return this;
        }

        /**
         * Set a {@link Collection} of {@link EffectGroup}s to the effects to show at first start loading it from a {@link File} at a given path.
         * <p>
         * Replaces all previously added {@code EffectGroups}.
         *
         * @param file the {@code File} containing the {@code EffectGroups} to set
         * @return this builder
         */
        public abstract AbstractBuilder<T> setEffectGroups(final File file);

        /**
         * Set a {@link Collection} of {@link EffectGroup}s to the effects to show at first start loading it from a {@link File} at a given path.
         * <p>
         * Replaces all previously added {@code EffectGroups}.
         *
         * @param path the path of the {@code File} containing the {@code EffectGroups} to set
         * @return this builder
         * @see #setEffectGroups(File)
         */
        public AbstractBuilder<T> setEffectGroups(final String path) {
            return setEffectGroups(new File(path));
        }

        /**
         * Builds a new {@link SingleRunApp}.
         */
        public abstract void build();

        /**
         * Getter method for monitor display property.
         *
         * @return the monitor display property
         */
        protected boolean isMonitorDisplay() {
            return monitorDisplay;
        }

        /**
         * Getter method for monitor display property.
         *
         * @return the monitor time property
         */
        protected boolean isMonitorTime() {
            return monitorTime;
        }

        /**
         * Getter method for monitor display property.
         *
         * @return the monitor steps property
         */
        protected boolean isMonitorSteps() {
            return monitorSteps;
        }

        /**
         * Getter method for the simulation to display.
         *
         * @return the simulation
         */
        protected Simulation<T> getSimulation() {
            return simulation;
        }
    }
}
