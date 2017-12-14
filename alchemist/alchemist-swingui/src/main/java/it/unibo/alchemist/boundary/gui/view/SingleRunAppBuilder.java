package it.unibo.alchemist.boundary.gui.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.core.interfaces.Simulation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

/**
 * Builder class for {@link SingleRunApp}, meant to be used to build and run the single run gui along with another (alreay started) JavaFX {@link Application}.
 *
 * @param <T> the concentration type
 */
public class SingleRunAppBuilder<T> {
    private static final String DEFAULT_EFFECTS = "/it/unibo/alchemist/gui/effects/json/DefaultEffects.json";
    private final Simulation<T> simulation;
    private final Collection<EffectGroup> effectGroups;
    private volatile boolean shouldUseDefaultEffects;

    /**
     * Default constructor of the builder.
     *
     * @param simulation the simulation to build the view for
     */
    public SingleRunAppBuilder(final Simulation<T> simulation) {
        this.simulation = Objects.requireNonNull(simulation);
        this.effectGroups = new ArrayList<>();
        this.shouldUseDefaultEffects = true;
    }

    /**
     * Set if should load default effects while building the {@link SingleRunApp}.
     * <p>
     * By default, it is true.
     *
     * @param shouldUse if should load default {@link EffectFX effects}
     * @return this builder
     */
    public SingleRunAppBuilder<T> useDefaultEffects(final boolean shouldUse) {
        this.shouldUseDefaultEffects = shouldUse;
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
    public SingleRunAppBuilder<T> setEffectGroups(final Collection<EffectGroup> effectGroups) {
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
    public SingleRunAppBuilder<T> setEffectGroups(final File file) {
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
    public SingleRunAppBuilder<T> setEffectGroups(final String path) {
        return setEffectGroups(new File(path));
    }

    /**
     * Add an {@code EffectGroup} to the effects to show at first start.
     *
     * @param effects the {@code EffectGroup} to add
     * @return this builder
     */
    public SingleRunAppBuilder<T> addEffectGroup(final EffectGroup effects) {
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
    public SingleRunAppBuilder<T> addEffectGroup(final File file) {
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
    public SingleRunAppBuilder<T> addEffectGroup(final String path) {
        return addEffectGroup(new File(path));
    }

    /**
     * Starts the JavaFX Thread, if necessary.
     *
     * @return true if it was already running, false otherwise
     */
    private boolean startJFXThread() {
        try {
            // Starts JavaFX thread, if necessary
            // PlatformImpl.startup(() -> { });
            new JFXPanel(); // To avoid internal APIs in line above, use this to start JavaFX thread
            return false;
        } catch (final IllegalStateException e) {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     * <br>
     * The new Application is run on JFX Event Thread.
     *
     * @see Platform#runLater(Runnable)
     */
    public void build() {
        if (shouldUseDefaultEffects) {
            try {
                effectGroups.add(EffectSerializer.effectsFromResources(DEFAULT_EFFECTS));
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        final Runnable lambda = () -> {
            final SingleRunApp<T> app = new SingleRunApp<>();
            if (!effectGroups.isEmpty()) {
                app.setEffectGroups(effectGroups);
            }
            app.setSimulation(simulation);
            final Stage stage = new Stage();
            app.start(stage);
        };

        if (startJFXThread() && Platform.isFxApplicationThread()) {
            lambda.run();
        } else {
            Platform.runLater(lambda);
        }
    }
}
