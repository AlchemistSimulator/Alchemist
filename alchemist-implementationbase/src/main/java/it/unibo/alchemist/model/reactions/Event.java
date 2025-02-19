/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.TimeDistribution;

import javax.annotation.Nonnull;
import java.io.Serial;

/**
 * This reaction completely ignores the propensity conditioning of the
 * conditions, and tries to run every time the {@link TimeDistribution} wants
 * to.
 *
 * @param <T> concentration type
 */
public final class Event<T> extends AbstractReaction<T> {

    @Serial
    private static final long serialVersionUID = -1640973841645383193L;

    /**
     * @param node the node this {@link Event} belongs to
     * @param timeDistribution the {@link TimeDistribution} this event should use
     */
    public Event(final Node<T> node, final TimeDistribution<T> timeDistribution) {
        super(node, timeDistribution);
    }

    @Override
    protected void updateInternalStatus(
            final Time currentTime,
            final boolean hasBeenExecuted,
            final Environment<T, ?> environment
    ) {
    }

    @Override
    public double getRate() {
        return getTimeDistribution().getRate();
    }

    @Override
    @Nonnull
    public Event<T> cloneOnNewNode(@Nonnull final Node<T> node, @Nonnull final Time currentTime) {
        return makeClone(() -> new Event<>(node, getTimeDistribution().cloneOnNewNode(node, currentTime)));
    }
}
