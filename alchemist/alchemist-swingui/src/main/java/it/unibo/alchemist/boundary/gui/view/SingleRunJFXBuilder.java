package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.core.interfaces.Simulation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import static javafx.application.Application.launch;

public class SingleRunJFXBuilder<T> extends SingleRunApp.AbstractBuilder<T> {
    private Optional<EventHandler<WindowEvent>> defaultOnCloseOperation;
    private Collection<EffectGroup> effectGroups;

    /**
     * Default constructor of the builder.
     *
     * @param simulation the simulation to build the view for
     */
    public SingleRunJFXBuilder(final Simulation<T> simulation) {
        super(simulation);
    }

    /**
     * Simply runs the {@link SingleRunApp Application} with given params.
     *
     * @param args params for the {@link Application}
     */
    public static void main(final String... args) {
        launch(SingleRunApp.class, args);
    }

    @Override
    public SingleRunJFXBuilder<T> monitorDisplay(final boolean monitorDisplay) {
        return (SingleRunJFXBuilder<T>) super.monitorDisplay(monitorDisplay);
    }

    @Override
    public SingleRunJFXBuilder<T> monitorSteps(final boolean monitorSteps) {
        return (SingleRunJFXBuilder<T>) super.monitorSteps(monitorSteps);
    }

    @Override
    public SingleRunJFXBuilder<T> monitorTime(final boolean monitorTime) {
        return (SingleRunJFXBuilder<T>) super.monitorTime(monitorTime);
    }

    /**
     * Set the {@link Stage#setOnCloseRequest(EventHandler) default close operation} to the specified handler.
     *
     * @param eventHandler the handler to call when close operation is requested to the {@link Stage}
     * @return this builder
     */
    public SingleRunJFXBuilder<T> setDefaultOnCloseOperation(final EventHandler<WindowEvent> eventHandler) {
        this.defaultOnCloseOperation = Optional.of(eventHandler);
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
    public SingleRunJFXBuilder<T> setEffectGroups(final Collection<EffectGroup> effectGroups) {
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
    public SingleRunJFXBuilder<T> setEffectGroups(final File file) {
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
    public SingleRunJFXBuilder<T> setEffectGroups(final String path) {
        return setEffectGroups(new File(path));
    }

    /**
     * Add an {@code EffectGroup} to the effects to show at first start.
     *
     * @param effects the {@code EffectGroup} to add
     * @return this builder
     */
    public SingleRunJFXBuilder<T> addEffectGroup(final EffectGroup effects) {
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
    public SingleRunJFXBuilder<T> addEffectGroup(final File file) {
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
    public SingleRunJFXBuilder<T> addEffectGroup(final String path) {
        return addEffectGroup(new File(path));
    }

    /**
     * {@inheritDoc}
     * <br>
     * The new Application is run on JFX Event Thread.
     *
     * @see Platform#runLater(Runnable)
     */
    @Override
    public void build() {
        Platform.runLater(() -> {
            final SingleRunApp<T> app = new SingleRunApp<>();
            // TODO set params
            app.start(new Stage());
        });
    }
}
