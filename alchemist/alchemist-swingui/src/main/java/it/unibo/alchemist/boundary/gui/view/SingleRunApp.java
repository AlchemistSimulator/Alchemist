package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.gui.controller.ButtonsBarController;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.monitor.AbstractFXDisplay;
import it.unibo.alchemist.boundary.monitor.FX2DDisplay;
import it.unibo.alchemist.boundary.monitor.FXMapDisplay;
import it.unibo.alchemist.boundary.monitor.FXStepMonitor;
import it.unibo.alchemist.boundary.monitor.FXTimeMonitor;
import it.unibo.alchemist.boundary.monitor.PlayPauseMonitor;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
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
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibo.alchemist.boundary.gui.controller.ButtonsBarController.BUTTONS_BAR_LAYOUT;

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
    private boolean initialized = false;
    @Nullable
    private Simulation<T> simulation;
    @Nullable
    private AbstractFXDisplay<T> displayMonitor;
    private PlayPauseMonitor<T> playPauseMonitor;
    private FXTimeMonitor<T> timeMonitor;
    private FXStepMonitor<T> stepMonitor;

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
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }
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
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }
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
    @SuppressWarnings("unchecked")
    public void start(final Stage primaryStage) {
        parseNamedParams(getNamedParams());
        parseUnnamedParams(getUnnamedParams());
        final Optional<Simulation<T>> optSim = Optional.ofNullable(this.simulation);
        optSim.ifPresent(sim -> {
            try {
                initDisplayMonitor(
                        MapEnvironment.class.isAssignableFrom(Class.forName(sim.getEnvironment().getClass().getName()))
                                ? FXMapDisplay.class.getName()
                                : FX2DDisplay.class.getName()
                );
            } catch (final ClassCastException | ClassNotFoundException exception) {
                L.error("Display monitor not valid");
                throw new IllegalArgumentException(exception);
            }
        });
        final Optional<AbstractFXDisplay> optDisplayMonitor = Optional.ofNullable(this.displayMonitor);
        final Pane rootLayout;
        try {
            rootLayout = FXResourceLoader.getLayout(AnchorPane.class, this, ROOT_LAYOUT);
            final StackPane main = (StackPane) rootLayout.getChildren().get(0);
            optDisplayMonitor.ifPresent(dm -> {
                dm.widthProperty().bind(main.widthProperty());
                dm.heightProperty().bind(main.heightProperty());
                main.getChildren().add(dm);
            });
            this.timeMonitor = new FXTimeMonitor<>();
            this.stepMonitor = new FXStepMonitor<>();
            this.playPauseMonitor = new PlayPauseMonitor<>(simulation);

            optSim.ifPresent(s -> {
                optDisplayMonitor.ifPresent(s::addOutputMonitor);
                s.addOutputMonitor(this.playPauseMonitor);
                s.addOutputMonitor(this.timeMonitor);
                s.addOutputMonitor(this.stepMonitor);
            });
            final ButtonsBarController buttonsBarController = new ButtonsBarController(playPauseMonitor, timeMonitor, stepMonitor);

            final BorderPane bar = FXResourceLoader.getLayout(BorderPane.class, buttonsBarController, BUTTONS_BAR_LAYOUT);
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
            primaryStage.getIcons().add(SVGImageUtils.getSvgImage("/icon/icon.svg"));
            primaryStage.setScene(new Scene(rootLayout));

            initialized = true;
            primaryStage.show();
        } catch (final IOException e) {
            L.error("I/O Exception loading FXML layout files", e);
            throw new UncheckedIOException(e);
        }
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
            switch (key) {
                case USE_EFFECT_GROUPS_FROM_FILE:
                    try {
                        effectGroups.addAll(EffectSerializer.effectGroupsFromFile(new File(value)));
                    } catch (final IOException e) {
                        L.warn(e.getMessage());
                        effectGroups.clear(); // TODO check if necessary
                    }
                    break;
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
                switch (param.startsWith(PARAMETER_NAME_START)
                        ? param.substring(PARAMETER_NAME_START.length())
                        : param) {
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
            final Class<? extends AbstractFXDisplay> clazz;
            clazz = (Class<? extends AbstractFXDisplay>) Class.forName(className);

            final Constructor[] constructors = clazz.getDeclaredConstructors();
            Constructor constructor = null;
            for (final Constructor c : constructors) {
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
                    throw new IllegalArgumentException(exception);
                }
            }
        } catch (final ClassCastException | ClassNotFoundException exception) {
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
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }

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
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }

        this.effectGroups.addAll(effectGroups);
    }

    /**
     * Adds effect from a file.
     *
     * @param path the path of the collection of EffectGroups.
     * @throws IllegalStateException if the application is already started
     */
    public void addEffectGroups(final String path) {
        if (initialized) {
            throw new IllegalStateException("Application is already initialized");
        }

        addNamedParam(USE_EFFECT_GROUPS_FROM_FILE, path);
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
        this.simulation = simulation;
    }
}
