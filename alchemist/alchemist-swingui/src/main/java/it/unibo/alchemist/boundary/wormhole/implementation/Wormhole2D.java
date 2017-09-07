/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.implementation.adapter.ComponentViewType;
import it.unibo.alchemist.model.interfaces.Environment;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Dimension2D;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;

/**
 * Partial implementation for the interface {@link it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D}.<br>
 * I am considering the particular case of the view as an entity into the
 * screen-space: the y-axis grows on the bottom side of the screen.
 */
public class Wormhole2D extends AbstractWormhole {
    /**
     * Default logger.
     */
    private static final Logger L = LoggerFactory.getLogger(Wormhole2D.class);

    /**
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     *
     * @param env  the {@link Environment}
     * @param comp the controlled {@link Component}
     */
    public Wormhole2D(final Environment<?> env, final Component comp) {
        super(
                env,
                new ComponentViewType(comp),
                from(comp.getWidth() / 2, comp.getHeight() / 2)
        );
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#getSize()
     */
    @Override
    public Tuple2<Double, Double> getViewSize() {
        return new Tuple2<>(getView().getWidth(), getView().getHeight());
    }

    @Override
    public void optimalZoom() {
        if (getEnvRatio() <= getViewRatio()) {
            setZoom(Math.max(1, getView().getHeight()) / getEnvironment().getSize()[1]);
        } else {
            setZoom(Math.max(1, getView().getWidth()) / getEnvironment().getSize()[0]);
        }
    }

    @Override
    protected Logger getLogger() {
        return it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D.L;
    }

    /**
     * {@inheritDoc}
     *
     * @see #getViewSize()
     */
    @Override
    protected double getViewRatio() {
        final Tuple2<Double, Double> size = getViewSize();
        return Math.max(1, size.v1()) / Math.max(1, size.v2());
    }
}
