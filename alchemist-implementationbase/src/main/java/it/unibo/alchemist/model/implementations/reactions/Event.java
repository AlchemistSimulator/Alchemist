/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * 
 */
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.TimeDistribution;

/**
 * This reaction completely ignores the propensity conditioning of the
 * conditions, and tries to run every time the {@link TimeDistribution} wants
 * to.
 * 
 * @param <T> concentration type
 */
public final class Event<T> extends AbstractReaction<T> {

    private static final long serialVersionUID = -1640973841645383193L;

    /**
     * @param node the node this {@link Event} belongs to
     * @param timedist the {@link TimeDistribution} this event should use
     */
    public Event(final Node<T> node, final TimeDistribution<T> timedist) {
        super(node, timedist);
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
    public Event<T> cloneOnNewNode(final Node<T> node, final Time currentTime) {
        return makeClone(() -> new Event<>(node, getTimeDistribution().cloneOnNewNode(node, currentTime)));
    }
}
