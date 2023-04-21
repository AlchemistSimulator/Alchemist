/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.monitors.impl;

import it.unibo.alchemist.boundary.OutputMonitor;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code OutputMonitor} that monitors the current
 * {@link it.unibo.alchemist.core.Simulation#getStep() steps} of the {@code Simulation}.
 *
 * @param <N> the numeric type
 * @param <T> The type which describes the {@link it.unibo.alchemist.model.Concentration} of a molecule
 * @param <P> The position type
 */
public abstract class AbstractNumericLabelMonitor<N, T, P extends Position<? extends P>>
        extends Label implements OutputMonitor<T, P> {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 2L;
    private final N init;
    private volatile boolean mayRender = true;
    private volatile N current;
    private @Nullable String name;

    /**
     * Constructor.
     *
     * @param init the initial {@link N} class value
     */
    public AbstractNumericLabelMonitor(final @Nonnull N init) {
        this(init, null);
    }

    /**
     * Constructor.
     *
     * @param init the initial {@link N} class value
     * @param name the name tag
     */
    public AbstractNumericLabelMonitor(final @Nonnull N init, final @Nullable String name) {
        this.init = Objects.requireNonNull(init);
        setName(name);
        setTextFill(Color.WHITE);
        update(init);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialized(@Nonnull final Environment<T, P> environment) {
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
                setText(getName().isPresent() ? getName().get() + current.toString() : String.valueOf(current.toString()));
            });
        }
    }

    /**
     * Getter method for name tag to be put before the numerical value.
     *
     * @return the current name tag
     */
    protected final Optional<String> getName() {
    return Optional.ofNullable(name);
    }

    /**
     * Setter method for name tag to be put before the numerical value.
     *
     * @param name the name tag
     */
    protected final void setName(final @Nullable String name) {
        this.name = name;
    }
}
