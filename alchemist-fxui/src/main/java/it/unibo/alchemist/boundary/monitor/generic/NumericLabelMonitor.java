/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitor.generic;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import java.util.Objects;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

/**
 * {@code OutputMonitor} that monitors the current
 * {@link it.unibo.alchemist.core.interfaces.Simulation#getStep() steps} of the {@code Simulation}.
 *
 * @param <N> the numeric type
 * @param <T> The type which describes the {@link it.unibo.alchemist.model.interfaces.Concentration} of a molecule
 * @param <P> The position type
 */
public abstract class NumericLabelMonitor<N, T, P extends Position<? extends P>>
        extends Label implements OutputMonitor<T, P> {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;
    private final N init;
    private volatile boolean mayRender = true;
    private volatile N current;
    private Optional<String> name;

    /**
     * Constructor.
     *
     * @param init the initial {@link N} class value
     */
    public NumericLabelMonitor(final @Nonnull N init) {
        this(init, null);
    }

    /**
     * Constructor.
     *
     * @param init the initial {@link N} class value
     * @param name the name tag
     */
    public NumericLabelMonitor(final @Nonnull N init, final @Nullable String name) {
        this.init = Objects.requireNonNull(init);
        setName(name);
        setTextFill(Color.WHITE);
        update(init);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialized(final Environment<T, P> environment) {
        update(init);
    }

    /**
     * Updates the GUI.
     *
     * @param val the value to update with
     */
    protected final void update(final N val) {
        current = val;
        if (mayRender) {
            mayRender = false;
            Platform.runLater(() -> {
                mayRender = true;
                setText(
                        getName().isPresent()
                                ? getName().get() + current.toString()
                                : String.valueOf(current.toString())
                );
            });
        }
    }

    /**
     * Getter method for name tag to be put before the numerical value.
     *
     * @return the current name tag
     */
    protected final Optional<String> getName() {
        return this.name;
    }

    /**
     * Setter method for name tag to be put before the numerical value.
     *
     * @param name the name tag
     */
    protected final void setName(final @Nullable String name) {
        this.name = Optional.ofNullable(name);
    }
}
