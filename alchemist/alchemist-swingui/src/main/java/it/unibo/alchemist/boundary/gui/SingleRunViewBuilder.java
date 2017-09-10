package it.unibo.alchemist.boundary.gui;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.monitors.*;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.*;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Main class to start an empty simulator visualization.
 */
public class SingleRunViewBuilder<T> {
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
    public SingleRunViewBuilder(final Simulation<T> simulation) {
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
    public SingleRunViewBuilder monitorDisplay(final boolean monitorDisplay) {
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
    public SingleRunViewBuilder monitorSteps(final boolean monitorSteps) {
        this.monitorSteps = monitorSteps;
        return this;
    }

    /**
     * Set the default {@link OutputMonitor} that will graphically show the time progress.
     *
     * @param monitorTime
     * @return this builder
     */
    public SingleRunViewBuilder monitorTime(final boolean monitorTime) {
        this.monitorTime = monitorTime;
        return this;
    }

    /**
     * Set the title for the {@link Stage}.
     *
     * @param title the title
     * @return this builder
     */
    public SingleRunViewBuilder setTitle(final String title) {
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
    public SingleRunViewBuilder setIcon(final String path) {
        return setIcon(SVGImageUtils.getSvgImage(path));
    }

    /**
     * Set the default icon for the {@link Stage}.
     *
     * @param icon the icon to set
     * @return this builder
     */
    public SingleRunViewBuilder setIcon(final Image icon) {
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
    public SingleRunViewBuilder setDefaultOnCloseOperation(final int jFrameCloseOperation) throws IllegalArgumentException {
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
    public SingleRunViewBuilder setDefaultOnCloseOperation(final EventHandler<WindowEvent> eventHandler) {
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
    public SingleRunViewBuilder setEffectGroups(final Collection<EffectGroup> effectGroups) {
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
    public SingleRunViewBuilder setEffectGroups(final File file) {
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
    public SingleRunViewBuilder setEffectGroups(final String path) {
        return setEffectGroups(new File(path));
    }

    /**
     * Add an {@code EffectGroup} to the effects to show at first start.
     *
     * @param effects the {@code EffectGroup} to add
     * @return this builder
     */
    public SingleRunViewBuilder addEffectGroup(final EffectGroup effects) {
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
    public SingleRunViewBuilder addEffectGroup(final File file) {
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
    public SingleRunViewBuilder addEffectGroup(final String path) {
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
    public SingleRunApp build() {
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

        return new SingleRunApp(); // TODO not working like so
    }
}

