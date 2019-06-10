/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies.target;

import it.unibo.alchemist.model.implementations.movestrategies.AbstractStrategyWithGPS;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;
import it.unibo.alchemist.model.interfaces.Route;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * This strategy follows a {@link Route}.
 * 
 */
public class FollowTrace extends AbstractStrategyWithGPS implements TargetSelectionStrategy<GeoPosition> {

    private static final long serialVersionUID = -446053307821810437L;
    private final Reaction<?> reaction;

    /**
     * @param r
     *            the reaction
     */
    public FollowTrace(final Reaction<?> r) {
        reaction = r;
    }

    @Override
    public final GPSPoint getTarget() {
        final Time time = reaction.getTau();
        assert getTrace().getNextPosition(time) != null;
        return getTrace().getNextPosition(time);
    }
}
