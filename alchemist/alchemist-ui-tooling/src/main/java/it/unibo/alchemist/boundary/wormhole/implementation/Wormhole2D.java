/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.interfaces.ViewType;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;

import java.awt.geom.Dimension2D;
import java.util.function.Function;

/**
 * Partial implementation for the class {@link it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole}.<br>
 * I am considering the particular case of the view as an entity into the
 * screen-space: the y-axis grows on the bottom side of the screen.
 *
 * @param <P> position type
 */
public class Wormhole2D<P extends Position2D<? extends P>> extends AbstractWormhole2D<P> {

    /**
     * Bidimensional wormhole constructor for any {@link ViewType}.
     * <br/>
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     *
     * @param env  the {@link Environment}
     * @param viewType the {@link ViewType} of the UI used for implementing the wormhole.
     * @param viewTypeToPointAdapter a {@link Function} used to create the initial position of the wormhole.
     */
    public <T extends ViewType> Wormhole2D(
            final Environment<?, P> env,
            final T viewType,
            final Function<T, PointAdapter<P>> viewTypeToPointAdapter
    ) {
        super(
                env,
                viewType,
                viewTypeToPointAdapter.apply(viewType)
        );
    }

    @Override
    public final Dimension2D getViewSize() {
        return new DoubleDimension(getView().getWidth(), getView().getHeight());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimalZoom() {
        if (getEnvRatio() <= getViewRatio()) {
            setZoom(Math.max(1, getView().getHeight()) / getEnvironment().getSize()[1]);
        } else {
            setZoom(Math.max(1, getView().getWidth()) / getEnvironment().getSize()[0]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double getViewRatio() {
        return Math.max(1, getView().getWidth()) / Math.max(1, getView().getHeight());
    }
}
