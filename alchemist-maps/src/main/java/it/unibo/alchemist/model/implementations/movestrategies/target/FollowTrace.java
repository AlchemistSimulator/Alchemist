/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies.target;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.movestrategies.AbstractStrategyWithGPS;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;
import it.unibo.alchemist.model.interfaces.Route;
import it.unibo.alchemist.model.Time;

/**
 * This strategy follows a {@link Route}.
 * 
 */
public final class FollowTrace<T> extends AbstractStrategyWithGPS implements TargetSelectionStrategy<T, GeoPosition> {

    private static final long serialVersionUID = -446053307821810437L;
    private final Reaction<?> reaction;

    /**
     * @param reaction
     *            the reaction
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is made by purpose")
    public FollowTrace(final Reaction<?> reaction) {
        this.reaction = reaction;
    }

    @Override
    public GPSPoint getTarget() {
        final Time time = reaction.getTau();
        assert getTrace().getNextPosition(time) != null;
        return getTrace().getNextPosition(time);
    }

    @Override
    public FollowTrace<T> cloneIfNeeded(final Node<T> destination, final Reaction<T> reaction) {
        return new FollowTrace<>(reaction);
    }
}
