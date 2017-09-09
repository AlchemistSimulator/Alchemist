package it.unibo.alchemist.boundary.gui;

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
import javafx.application.Application;
import javafx.embed.swing.JFXPanel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Main class to start an empty simulator visualization.
 */
public class SingleRunView {
    private static final Logger L = LoggerFactory.getLogger(SingleRunView.class);
    /**
     * Main layout without nested layouts. Must inject eventual other nested layouts.
     */
    private static final String ROOT_LAYOUT = "RootLayout";

    private final Stage stage;
    private final Pane rootLayout;
    private final ButtonsBarController buttonsBarController;
    private final Collection<EffectGroup> effectGroups;
    private final Canvas displayMonitor;
    private final Label timeMonitor;
    private final Label stepMonitor;

    /**
     * Builds a single-use graphical interface.
     *
     * @param sim            the simulation for this GUI
     * @param effectsFile    the effects file
     * @param closeOperation the type of close operation for this GUI
     * @param <T>            concentration type
     */
    private SingleRunView(final Collection<EffectGroup> effects, final Canvas canvas, final Label time, final Label step, final Stage stage) {
        this.effectGroups = effects;
        this.displayMonitor = canvas;
        this.timeMonitor = time;
        this.stepMonitor = step;
        this.stage = stage;

        try {
            this.rootLayout = FXResourceLoader.getLayout(AnchorPane.class, this, ROOT_LAYOUT);
            final StackPane main = (StackPane) rootLayout.getChildren().get(0);
            main.getChildren().add(canvas);
            this.buttonsBarController = new ButtonsBarController();
            main.getChildren().add(FXResourceLoader.getLayout(BorderPane.class, buttonsBarController, "ButtonsBarLayout"));
        } catch (final IOException e) {
            L.error("I/O Exception loading FXML layout files", e);
            throw new IllegalStateException(e);
        }

    }

    /**
     * Method that launches the application.
     *
     * @param args arguments
     */
    public static void main(final String[] args) throws InterruptedException {
        new SingleRunView(
                new ArrayList<>(),
                new Canvas(),
                new Label("0.000"),
                new Label("0"),
                new Stage()
        ).buildApp();
    }

    /**
     * Getter method for the built {@code Stage}.
     *
     * @return the {@code Stage}
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * {@link Application#launch(String...) Starts} a new {@link Application} with the built {@link Stage}.
     *
     * @see Application
     * @see Builder
     */
    public void buildApp() {
        new Application() {
            @Override
            public void start(final Stage primaryStage) {
                primaryStage.show();
            }
        }.start(stage);
    }

    /**
     * Builder class for a {@link SingleRunView} that will show graphically a {@link Simulation}.
     *
     * @param <T> the {@link Concentration} type
     */
    public static class Builder<T> {
        private final Simulation<T> simulation;
        private Optional<Canvas> displayMonitor;
        private Optional<Label> timeMonitor;
        private Optional<Label> stepMonitor;
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
            jFrameCloseOperation = Optional.empty();
            defaultOnCloseOperation = Optional.empty();
            this.effectGroups = new ArrayList<>();
        }

        /**
         * Set the default {@link OutputMonitor} that will graphically show the simulation using {@link EffectFX effects}.
         * <p>
         * It will automatically use the best {@link AbstractFXDisplay OutputMonitor} to use.
         *
         * @return this builder
         * @see #setDisplayMonitor(AbstractFXDisplay)
         */
        public Builder setDefaultDisplayMonitor() {
            return setDisplayMonitor(
                    Objects.requireNonNull(simulation).getEnvironment() instanceof OSMEnvironment
                            ? new FX2DDisplay<>()
                            : new FXMapDisplay<>()
            );
        }

        /**
         * Set the default {@link OutputMonitor} that will graphically show the simulation using {@link EffectFX effects}.
         *
         * @param displayMonitor the {@code FX2DOutputMonitor}
         * @return this builder
         */
        public Builder setDisplayMonitor(final AbstractFXDisplay<T> displayMonitor) {
            this.displayMonitor = Optional.of(displayMonitor);
            return this;
        }

        /**
         * Set the default {@link OutputMonitor} that will graphically show the step progress.
         *
         * @param stepMonitor the {@code FXStepMonitor}
         * @return this builder
         */
        public Builder setStepMonitor(final FXStepMonitor<T> stepMonitor) {
            this.stepMonitor = Optional.of(stepMonitor);
            return this;
        }

        /**
         * Set the default {@link OutputMonitor} that will graphically show the time progress.
         *
         * @param timeMonitor the {@code FXTimeMonitor}
         * @return this builder
         */
        public Builder setTimeMonitor(final FXTimeMonitor<T> timeMonitor) {
            this.timeMonitor = Optional.of(timeMonitor);
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
                    effectGroups,
                    displayMonitor.orElse(new Canvas()),
                    timeMonitor.orElse(new Label("0.000")),
                    stepMonitor.orElse(new Label("0")),
                    stage
            );
        }
    }
}
