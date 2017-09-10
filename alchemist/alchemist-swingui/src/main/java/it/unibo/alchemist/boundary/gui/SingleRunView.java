package it.unibo.alchemist.boundary.gui;

import com.sun.javafx.application.PlatformImpl;
import it.unibo.alchemist.boundary.gui.controller.ButtonsBarController;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.monitors.*;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import it.unibo.alchemist.model.interfaces.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Main class to start an empty simulator visualization.
 */
public class SingleRunView<T> {
    private static final Logger L = LoggerFactory.getLogger(SingleRunView.class);
    /**
     * Main layout without nested layouts. Must inject eventual other nested layouts.
     */
    private static final String ROOT_LAYOUT = "RootLayout";
    private final SingleRunApp app;
    private final Stage stage;

    /**
     * Builds a single-use graphical interface.
     *
     * @param simulation     the simulation to view
     * @param stage          the stage where the view will be shown
     * @param monitorDisplay true if the view should use a {@link FX2DOutputMonitor}, false if it should use a generic {@link Canvas}
     * @param effectGroups   the group of effects to load on first start
     * @param monitorTime    true if the view should use a {@link FXTimeMonitor}, false if it should use a generic {@link Label}
     * @param monitorSteps   true if the view should use a {@link FXStepMonitor}, false if it should use a generic {@link Label}
     */
    private SingleRunView(final Simulation simulation, final Stage stage, final boolean monitorDisplay, final Collection<EffectGroup> effectGroups, final boolean monitorTime, final boolean monitorSteps) {
        this.app = new SingleRunApp(simulation, monitorDisplay, effectGroups, monitorTime, monitorSteps);
        this.stage = stage;
    }

    /**
     * Method that launches the application.
     *
     * @param args arguments
     */
    public static void main(final String[] args) throws InterruptedException {
        PlatformImpl.startup(() -> {
        });
        Platform.runLater(() -> new SingleRunView(
                new Simulation() {
                    @Override
                    public void addOutputMonitor(OutputMonitor op) {

                    }

                    @Override
                    public Environment getEnvironment() {
                        return null;
                    }

                    @Override
                    public Optional<Throwable> getError() {
                        return null;
                    }

                    @Override
                    public long getFinalStep() {
                        return 0;
                    }

                    @Override
                    public Time getFinalTime() {
                        return null;
                    }

                    @Override
                    public Status getStatus() {
                        return null;
                    }

                    @Override
                    public long getStep() {
                        return 0;
                    }

                    @Override
                    public Time getTime() {
                        return null;
                    }

                    @Override
                    public void goToStep(long steps) {

                    }

                    @Override
                    public void goToTime(Time t) {

                    }

                    @Override
                    public void neighborAdded(Node node, Node n) {

                    }

                    @Override
                    public void neighborRemoved(Node node, Node n) {

                    }

                    @Override
                    public void nodeAdded(Node node) {

                    }

                    @Override
                    public void nodeMoved(Node node) {

                    }

                    @Override
                    public void nodeRemoved(Node node, Neighborhood oldNeighborhood) {

                    }

                    @Override
                    public void pause() {

                    }

                    @Override
                    public void play() {

                    }

                    @Override
                    public void removeOutputMonitor(OutputMonitor op) {

                    }

                    @Override
                    public void schedule(CheckedRunnable r) {

                    }

                    @Override
                    public void terminate() {

                    }

                    @Override
                    public Status waitFor(Status s, long timeout, TimeUnit timeunit) {
                        return null;
                    }

                    @Override
                    public void run() {

                    }
                },
                new Stage(),
                false,
                new ArrayList<>(),
                false,
                false
        ).showApp());
    }

    public void showApp() {
        app.start(stage);
    }

    /**
     * {@link Application#launch(String...) Starts} a new {@link Application} with the built {@link Stage}.
     *
     * @see Application
     * @see Builder
     */
    public void runApp() {
        app.setStage(stage);
    }

    /**
     * Builder class for a {@link SingleRunView} that will show graphically a {@link Simulation}.
     *
     * @param <T> the {@link Concentration} type
     */
    public static class Builder<T> {
        private final Simulation<T> simulation;
        private boolean monitorDisplay;
        private boolean monitorTime;
        private boolean monitorSteps;
        private Optional<String> title;
        private Optional<Image> icon;
        private Optional<Integer> jFrameCloseOperation;
        private Optional<EventHandler<WindowEvent>> defaultOnCloseOperation;
        private Collection<EffectGroup> effectGroups;

        /**
         * Default constructor of the builder.
         *
         * @param simulation the simulation to build the view for
         */
        public Builder(final Simulation<T> simulation) {
            this.simulation = simulation;
            this.monitorDisplay = false;
            this.monitorTime = false;
            this.monitorSteps = false;
            this.jFrameCloseOperation = Optional.empty();
            this.defaultOnCloseOperation = Optional.empty();
            this.effectGroups = new ArrayList<>();
        }

        /**
         * Specify if the GUI should initialize an {@link OutputMonitor} that will graphically show the simulation using {@link EffectFX effects}.
         *
         * @param monitorDisplay true if the GUI should initialize the {@link OutputMonitor} as a {@link Canvas}
         * @return this builder
         * @see FX2DDisplay
         * @see FXMapDisplay
         */
        public Builder monitorDisplay(final boolean monitorDisplay) {
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
        public Builder monitorSteps(final boolean monitorSteps) {
            this.monitorSteps = monitorSteps;
            return this;
        }

        /**
         * Set the default {@link OutputMonitor} that will graphically show the time progress.
         *
         * @param monitorTime
         * @return this builder
         */
        public Builder monitorTime(final boolean monitorTime) {
            this.monitorTime = monitorTime;
            return this;
        }

        /**
         * Set the title for the {@link Stage}.
         *
         * @param title the title
         * @return this builder
         */
        public Builder setTitle(final String title) {
            this.title = Optional.of(title);
            return this;
        }

        /**
         * Set the default icon for the {@link Stage} loading from a SVG {@code File} at a given path.
         *
         * @param path the path of the icon to set
         * @return this builder
         * @see #setIcon(Image)
         * @see SVGImageUtils#getSvgImage(String)
         */
        public Builder setIcon(final String path) {
            return setIcon(SVGImageUtils.getSvgImage(path));
        }

        /**
         * Set the default icon for the {@link Stage}.
         *
         * @param icon the icon to set
         * @return this builder
         */
        public Builder setIcon(final Image icon) {
            this.icon = Optional.of(icon);
            return this;
        }

        /**
         * Set the {@link Stage#setOnCloseRequest(EventHandler) default close operation} to a standard handler, identified by {@link JFrame} default close operations.
         * <p>
         * It clears a previously set {@link #setDefaultOnCloseOperation(EventHandler) default close operation}.
         *
         * @param jFrameCloseOperation the identifier of a standard handler to call when close operation is requested to the {@link Stage}
         * @return this builder
         * @throws IllegalArgumentException if specified operation is not valid for {@link JFrame}
         * @see JFrame#DO_NOTHING_ON_CLOSE
         * @see JFrame#HIDE_ON_CLOSE
         * @see JFrame#DISPOSE_ON_CLOSE
         * @see JFrame#EXIT_ON_CLOSE
         */
        public Builder setDefaultOnCloseOperation(final int jFrameCloseOperation) throws IllegalArgumentException {
            if (jFrameCloseOperation == JFrame.DO_NOTHING_ON_CLOSE
                    || jFrameCloseOperation == JFrame.HIDE_ON_CLOSE
                    || jFrameCloseOperation == JFrame.DISPOSE_ON_CLOSE
                    || jFrameCloseOperation == JFrame.EXIT_ON_CLOSE) {
                this.jFrameCloseOperation = Optional.of(jFrameCloseOperation);
                this.defaultOnCloseOperation = Optional.empty();
            } else {
                throw new IllegalArgumentException();
            }
            return this;
        }

        /**
         * Set the {@link Stage#setOnCloseRequest(EventHandler) default close operation} to the specified handler.
         * <p>
         * It clears a previously set {@link #setDefaultOnCloseOperation(int) default close operation}.
         *
         * @param eventHandler the handler to call when close operation is requested to the {@link Stage}
         * @return this builder
         */
        public Builder setDefaultOnCloseOperation(final EventHandler<WindowEvent> eventHandler) {
            this.defaultOnCloseOperation = Optional.of(eventHandler);
            this.jFrameCloseOperation = Optional.empty();
            return this;
        }

        /**
         * Set a {@link Collection} of {@link EffectGroup}s to the effects to show at first start loading it from a {@link File} at a given path.
         * <p>
         * Removes all previously added {@code EffectGroups}.
         *
         * @param effectGroups the {@code EffectGroups} to set
         * @return this builder
         */
        public Builder setEffectGroups(final Collection<EffectGroup> effectGroups) {
            this.effectGroups.clear();
            this.effectGroups.addAll(effectGroups);
            return this;
        }

        /**
         * Set a {@link Collection} of {@link EffectGroup}s to the effects to show at first start loading it from a {@link File} at a given path.
         * <p>
         * Removes all previously added {@code EffectGroups}.
         *
         * @param file the {@code File} containing the {@code EffectGroups} to set
         * @return this builder
         * @see #setEffectGroups(Collection)
         * @see EffectSerializer#effectGroupsFromFile(File)
         */
        public Builder setEffectGroups(final File file) {
            try {
                return setEffectGroups(EffectSerializer.effectGroupsFromFile(file));
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /**
         * Set a {@link Collection} of {@link EffectGroup}s to the effects to show at first start loading it from a {@link File} at a given path.
         * <p>
         * Removes all previously added {@code EffectGroups}.
         *
         * @param path the path of the {@code File} containing the {@code EffectGroups} to set
         * @return this builder
         * @see #setEffectGroups(File)
         */
        public Builder setEffectGroups(final String path) {
            return setEffectGroups(new File(path));
        }

        /**
         * Add an {@code EffectGroup} to the effects to show at first start.
         *
         * @param effects the {@code EffectGroup} to add
         * @return this builder
         */
        public Builder addEffectGroup(final EffectGroup effects) {
            this.effectGroups.add(effects);
            return this;
        }

        /**
         * Add an {@link EffectGroup} to the effects to show at first start loading it from a given {@code File}.
         *
         * @param file the file containing the {@link EffectGroup} to add
         * @return this builder
         * @see #addEffectGroup(EffectGroup)
         * @see EffectSerializer#effectsFromFile(File)
         */
        public Builder addEffectGroup(final File file) {
            try {
                return addEffectGroup(EffectSerializer.effectsFromFile(file));
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /**
         * Add an {@link EffectGroup} to the effects to show at first start loading it from a {@link File} at a given path.
         *
         * @param path the path of the {@code File} containing the {@link EffectGroup} to add
         * @return this builder
         * @see #addEffectGroup(File)
         */
        public Builder addEffectGroup(final String path) {
            return addEffectGroup(new File(path));
        }

        /**
         * Build a new {@link Stage} with the parameters specified to this builder.
         * <p>
         * You should call {@link Stage#show() show()} method of the returned {@code Stage}
         * or pass this to an {@link Application} to see the built GUI.
         *
         * @return {@code Stage} with the parameters specified to this builder
         */
        public SingleRunView build() {
            final Stage stage = new Stage();

            // Set close operation
            jFrameCloseOperation.ifPresent(jfco -> {
                final EventHandler<WindowEvent> handler;
                switch (jfco) {
                    case JFrame.HIDE_ON_CLOSE:
                        handler = event -> stage.hide();
                        defaultOnCloseOperation = Optional.empty();
                        break;
                    case JFrame.DISPOSE_ON_CLOSE:
                        handler = event -> stage.close();
                        defaultOnCloseOperation = Optional.empty();
                        break;
                    case JFrame.EXIT_ON_CLOSE:
                        handler = event -> {
                            stage.close();
                            // Platform.exit(); // TODO check
                            System.exit(0);
                        };
                        defaultOnCloseOperation = Optional.empty();
                        break;
                    case JFrame.DO_NOTHING_ON_CLOSE:
                        defaultOnCloseOperation = Optional.empty();
                    default:
                        handler = event -> { /* Do nothing */ };
                }
                stage.setOnCloseRequest(handler);
            });
            defaultOnCloseOperation.ifPresent(stage::setOnCloseRequest);

            // Set stage title
            stage.setTitle(title.orElse("Alchemist Simulation"));

            // Set stage icon
            icon.ifPresent(stage.getIcons()::add);

            return new SingleRunView(
                    simulation,
                    stage,
                    monitorDisplay,
                    effectGroups,
                    monitorTime,
                    monitorSteps
            );
        }
    }

    private class SingleRunApp extends Application {
        private final Simulation<T> simulation;
        private final Collection<EffectGroup> effectGroups;
        private final Optional<AbstractFXDisplay<T>> displayMonitor;
        private final Optional<FXTimeMonitor<T>> timeMonitor;
        private final Optional<FXStepMonitor<T>> stepMonitor;
        private Stage stage;
        private Pane rootLayout;
        private ButtonsBarController buttonsBarController;

        private SingleRunApp(final Simulation<T> simulation, final boolean displayMonitor, final Collection<EffectGroup> effectGroups, final boolean timeMonitor, final boolean stepMonitor) {
            this.effectGroups = effectGroups;
            this.simulation = simulation;
            final AbstractFXDisplay<T> monitor = Objects.requireNonNull(simulation).getEnvironment() instanceof OSMEnvironment
                    ? new FX2DDisplay<>()
                    : new FXMapDisplay<>();
            this.displayMonitor = displayMonitor ? Optional.of(monitor) : Optional.empty();
            this.timeMonitor = timeMonitor ? Optional.of(new FXTimeMonitor<T>()) : Optional.empty();
            this.stepMonitor = stepMonitor ? Optional.of(new FXStepMonitor<T>()) : Optional.empty();
            this.stage = null;
        }

        public Stage getStage() {
            return stage;
        }

        public void setStage(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void start(final Stage primaryStage) {
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
    }
}
