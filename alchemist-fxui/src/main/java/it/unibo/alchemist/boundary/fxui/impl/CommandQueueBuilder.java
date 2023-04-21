/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl;

import it.unibo.alchemist.boundary.fxui.api.DrawCommand;
import it.unibo.alchemist.model.Position2D;
import org.apache.commons.collections4.queue.UnmodifiableQueue;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * Builder class that eases the building of a queue of {@link DrawCommand}s.
 *
 * @param <P> the position type
 */
public final class CommandQueueBuilder<P extends Position2D<? extends P>> {
    private final Queue<DrawCommand<P>> commandQueue = new ArrayDeque<>();

    /**
     * Wraps a {@code DrawCommand} around to the queue to be executed on the JavaFX thread.
     *
     * @param doOnJFXThread the action to do
     * @param supplier the boolean supplier that will check if the command should be executed
     * @return this builder
     */
    public CommandQueueBuilder wrapAndAdd(final Supplier<Boolean> supplier, final DrawCommand<P> doOnJFXThread) {
        return addCommand(doOnJFXThread.wrap(supplier));
    }

    /**
     * Adds a {@code DrawCommand} to the queue to be executed on the JavaFX thread.
     *
     * @param doOnJFXThread the action to do
     * @return this builder
     */
    public CommandQueueBuilder addCommand(final DrawCommand<P> doOnJFXThread) {
        commandQueue.add(doOnJFXThread);
        return this;
    }

    /**
     * Clears the queue.
     *
     * @return this builder
     */
    public CommandQueueBuilder cleanQueue() {
        commandQueue.clear();
        return this;
    }

    /**
     * Builds the queue of commands.
     *
     * @return the queue of commands
     */
    public Queue<DrawCommand<P>> buildCommandQueue() {
        return UnmodifiableQueue.unmodifiableQueue(commandQueue);
    }
}
