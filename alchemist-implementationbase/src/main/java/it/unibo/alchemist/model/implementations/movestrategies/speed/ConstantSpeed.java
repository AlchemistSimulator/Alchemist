/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies.speed;

import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;

/**
 * This strategy makes the node move at an average constant speed, which is
 * influenced by the {@link it.unibo.alchemist.model.interfaces.TimeDistribution} of the {@link Reaction} hosting
 * this {@link it.unibo.alchemist.model.interfaces.Action}. This action tries to normalize on the {@link Reaction}
 * rate, but if the {@link it.unibo.alchemist.model.interfaces.TimeDistribution} has a high variance, the movements
 * on the map will inherit this tract.
 *
 * @param <P> Position type
 * 
 */
public final class ConstantSpeed<P extends Position<P>> implements SpeedSelectionStrategy<P> {

    private static final long serialVersionUID = 1746429998480123049L;
    private final double speed;
    private final Reaction<?> reaction;

    /**
     * @param reaction
     *            the reaction
     * @param speed
     *            the speed, in meters/second
     */
    public ConstantSpeed(final Reaction<?> reaction, final double speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed must be positive");
        }
        this.speed = speed;
        this.reaction = reaction;
    }

    @Override
    public double getNodeMovementLength(final P target) {
        return speed / reaction.getRate();
    }

}
