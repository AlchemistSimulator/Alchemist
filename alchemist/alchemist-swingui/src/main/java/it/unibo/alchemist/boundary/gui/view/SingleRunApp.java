package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.gui.controller.ButtonsBarController;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.monitor.FXStepMonitor;
import it.unibo.alchemist.boundary.monitor.FXTimeMonitor;
import it.unibo.alchemist.boundary.monitor.PlayPauseMonitor;
import it.unibo.alchemist.boundary.monitors.AbstractFXDisplay;
import it.unibo.alchemist.boundary.monitors.FX2DDisplay;
import it.unibo.alchemist.boundary.monitors.FXMapDisplay;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.input.ActionOnKey;
import it.unibo.alchemist.input.KeyboardActionListener;
import it.unibo.alchemist.input.KeyboardTriggerAction;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Position2D;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibo.alchemist.boundary.gui.controller.ButtonsBarController.BUTTONS_BAR_LAYOUT;

/**
 * The class models a non-reusable GUI for simulation display.
 *
 * @param <T> the {@link Concentration} type
 */
public class SingleRunApp<T, P extends Position2D<? extends P>> extends Application {
    /**
     * Main layout without nested layouts. Must inject eventual other nested layouts.
     */
    public static final String ROOT_LAYOUT = "RootLayout";
    /**
     * Effect pass param name.
     */
    public static final String USE_EFFECT_GROUPS_FROM_FILE = "use-effect-groups-from-file";
    /**
     * Default parameter start string.
     */
    public static final String PARAMETER_NAME_START = "--";
    /**
     * Default parameter end string.
     */
    public static final String PARAMETER_NAME_END = "=";
    /**
     * Default logger for the class.
     */
    private static final Logger L = LoggerFactory.getLogger(SingleRunApp.class);

    private final Map<String, String> namedParams = new HashMap<>();
    private final List<String> unnamedParams = new ArrayList<>();
    private ObservableList<EffectGroup> effectGroups = FXCollections.observableArrayList();
    private boolean initialized;
    @Nullable
    private Simulation<T, P> simulation;
    @Nullable
    private AbstractFXDisplay<T> displayMonitor;
    private PlayPauseMonitor<T, P> playPauseMonitor;
    private FXTimeMonitor<T, P> timeMonitor;
    private FXStepMonitor<T, P> stepMonitor;

    /**
     * Method that launches the application.
     * <br>
     * For testing purpose only
     *
     * @param args {@link Parameters parameters} for the application
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    /**
     * Getter method for the unnamed parameters.
     *
     * @return the unnamed params
     * @see Parameters#getUnnamed()
     */
    private List<String> getUnnamedParams() {
        if (unnamedParams.isEmpty()) {
            Optional.ofNullable(getParameters()).ifPresent(p -> unnamedParams.addAll(p.getUnnamed()));
        }
        return this.unnamedParams;
    }

    /**
     * Getter method for the named parameters.
     *
     * @return the named params
     */
    private Map<String, String> getNamedParams() {
        if (namedParams.isEmpty()) {
            Optional.ofNullable(getParameters()).ifPresent(p -> namedParams.putAll(p.getNamed()));
        }
        return this.namedParams;
    }

    /**
     * The method adds a new named parameter.
     *
     * @param name  the param name
     * @param value the param value
     * @throws IllegalArgumentException if the parameter is not valid, or if {@link Parameters#getUnnamed()} it's not named}
     * @throws IllegalStateException    if the application is already started
     * @see Parameters#getNamed()
     */
    public void addNamedParam(final String name, final String value) {
        checkIfInitialized();
        if (value == null || value.equals("")) {
            throw new IllegalArgumentException("The given param is not named");
        }
        namedParams.put(name, value);
    }

    /**
     * The method adds a new named parameter.
     *
     * @param param the param name
     * @throws IllegalArgumentException if the parameter is not valid, or if {@link Parameters#getNamed()} it's named}
     * @throws IllegalStateException    if the application is already started
     * @see Parameters#getUnnamed()
     */
    public void addUnnamedParam(final String param) {
        checkIfInitialized();
        if (param == null || param.equals("")) {
            throw new IllegalArgumentException("The given param is not valid");
        }
        unnamedParams.add(param);
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
                            final int splitterIndex = param.lastIndexOf(PARAMETER_NAME_END);
                            addNamedParam(param.substring(0, splitterIndex), param.substring(splitterIndex));
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
        final Optional<Simulation<T, P>> optSim = getSimulation();
        optSim.ifPresent(sim -> {
            try {
                initDisplayMonitor(
                        MapEnvironment.class.isAssignableFrom(sim.getEnvironment().getClass())
                                ? FXMapDisplay.class.getName()
                                : FX2DDisplay.class.getName()
                );
            } catch (final ClassCastException exception) {
                L.error("Display monitor not valid");
                throw new IllegalArgumentException(exception);
            }
        });
        final Optional<AbstractFXDisplay<T>> optDisplayMonitor = Optional.ofNullable(this.displayMonitor);
        final Pane rootLayout;
        try {
            rootLayout = FXResourceLoader.getLayout(AnchorPane.class, this, ROOT_LAYOUT);
            final StackPane main = (StackPane) rootLayout.getChildren().get(0);
            final Scene scene = new Scene(rootLayout);
            optDisplayMonitor.ifPresent(dm -> {
                dm.widthProperty().bind(main.widthProperty());
                dm.heightProperty().bind(main.heightProperty());
                dm.widthProperty().addListener((observable, oldValue, newValue) -> {
                    if (!Objects.equals(oldValue, newValue)) {
                        dm.repaint();
                    }
                });
                dm.heightProperty().addListener((observable, oldValue, newValue) -> {
                    if (!Objects.equals(oldValue, newValue)) {
                        dm.repaint();
                    }
                });
                main.getChildren().addAll(dm.getInteractionCanvases());
                main.getChildren().add(dm);
                initKeybindings(scene, dm.getKeyboardListener());
            });
            this.timeMonitor = new FXTimeMonitor<>();
            this.stepMonitor = new FXStepMonitor<>();
            this.playPauseMonitor = new PlayPauseMonitor<>(simulation);
            optSim.ifPresent(s -> {
                optDisplayMonitor.ifPresent(it -> s.addOutputMonitor((OutputMonitor<T, P>) it));
                s.addOutputMonitor(this.playPauseMonitor);
                s.addOutputMonitor(this.timeMonitor);
                s.addOutputMonitor(this.stepMonitor);
            });
            final ButtonsBarController buttonsBarController = new ButtonsBarController(displayMonitor, playPauseMonitor, timeMonitor, stepMonitor);

            final BorderPane bar = FXResourceLoader.getLayout(BorderPane.class, buttonsBarController, BUTTONS_BAR_LAYOUT);
            bar.setPickOnBounds(false);
            main.widthProperty().addListener((observable, oldValue, newValue) -> bar.setPrefWidth(newValue.doubleValue()));
            main.getChildren().add(bar);
            buttonsBarController.getObservableEffectsList().addAll(this.effectGroups);
            effectGroups = buttonsBarController.getObservableEffectsList();
            optDisplayMonitor.ifPresent(d -> d.setEffects(buttonsBarController.getObservableEffectsList()));
            primaryStage.setTitle("Alchemist Simulation");
            primaryStage.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });
            primaryStage.getIcons().add(SVGImageUtils.getSvgImage(SVGImageUtils.DEFAULT_ALCHEMIST_ICON_PATH));
//            final Scene scene = new Scene(rootLayout);
//            initKeybindings(scene);
            primaryStage.setScene(scene);
            initialized = true;
            primaryStage.show();
            // The initialization of the monitors MUST be done AFTER the Stage is shown
            optSim.ifPresent(s -> initMonitors(s, (OutputMonitor<T, P>) optDisplayMonitor.orElse(null), stepMonitor, timeMonitor, playPauseMonitor));
        } catch (final IOException e) {
            L.error("I/O Exception loading FXML layout files", e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * The method schedules on the {@link Simulation} thread the initialization of given {@link OutputMonitor OutputMonitors}.
     *
     * @param simulation the simulation to {@link Simulation#schedule(CheckedRunnable) schedule} initialization to and to take {@link Environment} from
     * @param monitors   the {@code OutputMonitors} to {@link OutputMonitor#initialized(Environment) initialize}
     * @see Simulation#schedule(CheckedRunnable)
     * @see OutputMonitor#initialized(Environment)
     */
    @SafeVarargs
    private final void initMonitors(final @NotNull Simulation<T, P> simulation, final @Nullable OutputMonitor<T, P>... monitors) { // NOPMD - UnnecessaryFinalModifier - necessary to @SafeVarargs tag
        if (monitors != null) {
            for (final OutputMonitor<T, P> m : monitors) {
                if (m != null) {
                    simulation.schedule(() -> m.initialized(simulation.getEnvironment()));
                }
            }
        }
    }

    /**
     * Initializes the key bindings.
     * <p>
     * Should be overridden to implement keyboard interaction with the GUI.
     */
    protected void initKeybindings(final Scene scene, final KeyboardActionListener listener) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case P: playPauseMonitor.fireEvent(new ActionEvent(e.getSource(), playPauseMonitor));
                    e.consume();
                    break;
                default: listener.onAction(new KeyboardTriggerAction(ActionOnKey.PRESSED, e.getCode()), e);
                    break;
            }
        });
        scene.setOnKeyReleased(e -> listener.onAction(new KeyboardTriggerAction(ActionOnKey.RELEASED, e.getCode()), e));
    }

    /**
     * The method parses the given parameters from a key-value map.
     *
     * @param params the map of parameters name and respective values
     * @throws IllegalArgumentException if the value is not valid for the parameter
     * @see Parameters#getNamed()
     */
    private void parseNamedParams(final Map<String, String> params) {
        params.forEach((key, value) -> {
//            switch (key) {
//                case USE_EFFECT_GROUPS_FROM_FILE:
//                    try {
//                        effectGroups.addAll(EffectSerializer.effectGroupsFromFile(new File(value)));
//                    } catch (final IOException e) {
//                        L.warn(e.getMessage());
//                        effectGroups.clear(); // TODO check if necessary
//                    }
//                    break;
//                default:
//                    L.warn("Unexpected argument " + PARAMETER_NAME_START + key + PARAMETER_NAME_END + value);
//                    break;
//            }
            // TODO remove if and use switch (like comment above) for new named parameters
            if (key.equals(USE_EFFECT_GROUPS_FROM_FILE)) {
                try {
                    effectGroups.addAll(EffectSerializer.effectGroupsFromFile(new File(value)));
                } catch (final IOException e) {
                    L.warn(e.getMessage());
                    effectGroups.clear(); // TODO check if necessary
                }
            } else {
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
//            try {
//                switch (param.startsWith(PARAMETER_NAME_START)
//                        ? param.substring(PARAMETER_NAME_START.length())
//                        : param) {
//                    default:
//                        L.warn("Unexpected argument " + PARAMETER_NAME_START + param);
//                }
//            } catch (final IllegalArgumentException e) {
//                L.warn("Invalid argument: " + param, e);
//            }
            // TODO remove if and use the code commented above for new named parameters
            L.warn("Unexpected argument " + PARAMETER_NAME_START + (param.startsWith(PARAMETER_NAME_START)
                    ? param.substring(PARAMETER_NAME_START.length())
                    : param));
        });
    }

    /**
     * Initializes a new {@link AbstractFXDisplay} for the specified {@link Class#getName()}.
     *
     * @param className the name of the {@code AbstractFXDisplay} {@link OutputMonitor} to be inizialized
     * @throws IllegalArgumentException if the class name is null, empty or not an {@link AbstractFXDisplay},
     *                                  or the {@link AbstractFXDisplay} does not have a 0 arguments constructor
     * @see Class#forName(String)
     */
    @Contract("null -> fail")
    @SuppressWarnings("unchecked")
    private void initDisplayMonitor(final String className) {
        if (className == null || className.equals("")) {
            throw new IllegalArgumentException();
        }

        try {
            final Class<? extends AbstractFXDisplay<T>> clazz;
            clazz = (Class<? extends AbstractFXDisplay<T>>) Class.forName(className);

            final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> constructor = null;
            for (final Constructor<?> c : constructors) {
                if (c.getGenericParameterTypes().length == 0) {
                    constructor = c;
                    break;
                }
            }

            if (constructor == null) {
                throw new IllegalArgumentException();
            } else {
                try {
                    displayMonitor = (AbstractFXDisplay<T>) constructor.newInstance();
                } catch (final IllegalAccessException | IllegalArgumentException | InstantiationException
                        | InvocationTargetException | ExceptionInInitializerError exception) {
                    L.warn("No valid constructor found");
                    throw new IllegalArgumentException(exception);
                }
            }
        } catch (final ClassCastException | ClassNotFoundException exception) {
            L.warn(className + " is not a valid DisplayMonitor class");
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Setter method for the collection of groups of effects.
     *
     * @param effectGroups the groups of effects
     * @throws IllegalStateException if the application is already started
     */
    public void setEffectGroups(final Collection<EffectGroup> effectGroups) {
        checkIfInitialized();
        this.effectGroups.clear();
        this.effectGroups.addAll(effectGroups);
    }

    /**
     * Adds the effects to the current effects.
     *
     * @param effectGroups the group of effects to add
     * @throws IllegalStateException if the application is already started
     */
    public void addEffectGroups(final Collection<EffectGroup> effectGroups) {
        checkIfInitialized();
        this.effectGroups.addAll(effectGroups);
    }

    /**
     * Adds effect from a file.
     *
     * @param path the path of the collection of EffectGroups.
     * @throws IllegalStateException if the application is already started
     */
    public void addEffectGroups(final String path) {
        checkIfInitialized();
        addNamedParam(USE_EFFECT_GROUPS_FROM_FILE, path);
    }

    /**
     * Getter method for the simulation object.
     *
     * @return the optional simulation
     */
    private Optional<Simulation<T, P>> getSimulation() {
        return Optional.ofNullable(simulation);
    }

    /**
     * Setter method for simulation.
     *
     * @param simulation the simulation this {@link Application} will display
     * @throws IllegalStateException if the application is already started
     */
    public void setSimulation(final Simulation<T, P> simulation) {
        checkIfInitialized();
        this.simulation = simulation;
    }

    /**
     * Checks if the {@link Application} is already {@link #initialized}.
     *
     * @throws IllegalStateException if the application is initialized
     */
    private void checkIfInitialized() throws IllegalStateException {
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }
    }
}
