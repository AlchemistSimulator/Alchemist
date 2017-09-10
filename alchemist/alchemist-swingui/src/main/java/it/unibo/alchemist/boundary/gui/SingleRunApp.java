package it.unibo.alchemist.boundary.gui;

import it.unibo.alchemist.boundary.gui.controller.ButtonsBarController;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.monitors.*;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

class SingleRunApp<T> extends Application {
    public static final String USE_SPECIFIED_DISPLAY_MONITOR_PARAMETER_NAME = "use-display-monitor";
    public static final String USE_DEFAULT_DISPLAY_MONITOR_FOR_ENVIRONMENT_CLASS_PARAMETER_NAME = "use-default-display-monitor-for-environment";
    public static final String USE_FX_2D_DISPLAY_PARAMETER_NAME = "use-FX2DDisplay-monitor";
    public static final String USE_FX_MAP_DISPLAY_PARAMETER_NAME = "use-FXMapDisplay-monitor";
    public static final String USE_TIME_MONITOR_PARAMETER_NAME = "use-time-monitor";
    public static final String USE_STEP_MONITOR_PARAMETER_NAME = "use-step-monitor";
    public static final String USE_EFFECT_GROUPS_FROM_FILE = "use-effect-groups-from-file";

    private static final String PARAMETER_NAME_START = "--";
    private static final String PARAMETER_NAME_END = "=";

    private static final Logger L = LoggerFactory.getLogger(SingleRunApp.class);
    /**
     * Main layout without nested layouts. Must inject eventual other nested layouts.
     */
    private static final String ROOT_LAYOUT = "RootLayout";

    private Collection<EffectGroup> effectGroups;
    private Optional<AbstractFXDisplay<T>> displayMonitor;
    private Optional<FXTimeMonitor<T>> timeMonitor;
    private Optional<FXStepMonitor<T>> stepMonitor;
    private Stage stage;
    private Pane rootLayout;
    private ButtonsBarController buttonsBarController;

    public static void main(String... args) {
        Application.launch();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void start(final Stage primaryStage) {
        final Map<String, String> parameters = getParameters().getNamed();
        parameters.forEach((key, value) -> {
            switch (key) {
                case USE_SPECIFIED_DISPLAY_MONITOR_PARAMETER_NAME:
                    try {
                        initDisplayMonitor(value);
                    } catch (final Exception exception) {
                        L.error("Display monitor not valid");
                        throw exception;
                    }
                    break;
                case USE_DEFAULT_DISPLAY_MONITOR_FOR_ENVIRONMENT_CLASS_PARAMETER_NAME:
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
                case USE_FX_2D_DISPLAY_PARAMETER_NAME:
                    if (value == null || Boolean.valueOf(value) || value.equals("")) {
                        initDisplayMonitor(FX2DDisplay.class.getName());
                    }
                    break;
                case USE_FX_MAP_DISPLAY_PARAMETER_NAME:
                    if (value == null || Boolean.valueOf(value) || value.equals("")) {
                        initDisplayMonitor(FXMapDisplay.class.getName());
                    }
                    break;
                case USE_TIME_MONITOR_PARAMETER_NAME:
                    if (value == null || Boolean.valueOf(value) || value.equals("")) {
                        timeMonitor = Optional.of(new FXTimeMonitor<>());
                    }
                    break;
                case USE_STEP_MONITOR_PARAMETER_NAME:
                    if (value == null || Boolean.valueOf(value) || value.equals("")) {
                        stepMonitor = Optional.of(new FXStepMonitor<>());
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
                default:
                    L.warn("Unexpected argument " + PARAMETER_NAME_START + key + PARAMETER_NAME_END + value);
            }
        });

        if (this.stage == null) {
            this.stage = primaryStage;
        }

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

        this.stage.show();
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
}
