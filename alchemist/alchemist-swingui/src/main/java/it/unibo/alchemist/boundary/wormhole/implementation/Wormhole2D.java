/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.implementation.adapter.ComponentViewType;
import it.unibo.alchemist.boundary.wormhole.implementation.adapter.NodeViewType;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;
import java.awt.*;
import java.awt.geom.Dimension2D;
import javafx.scene.Node;
import org.apache.xmlgraphics.java2d.Dimension2DDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;

/**
 * Partial implementation for the interface {@link BidimensionalWormhole} for a {@link Node JavaFX view}.
 * <br/>
 * This considers the particular case of the view as an entity into the
 * sceern-space: the y-axis grows on the bottom side of the screen.
 */
public class Wormhole2D<P extends Position2D<? extends P>> extends AbstractWormhole2D<P> {

    private static final Logger L = LoggerFactory.getLogger(Wormhole2D.class);

    /**
     * Bidimensional wormhole constructor for an AWT/Swing {@link Component} class.
     * <br/>
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     *
     * @param env  the {@link Environment}
     * @param comp the controlled {@code Component}
     */
    public Wormhole2D(final Environment<?, P> env, final Component comp) {
        super(
                env,
                new ComponentViewType(comp),
                from(comp.getWidth() / 2, comp.getHeight() / 2)
        );
    }

    /**
     * Bidimensional wormhole constructor for an AWT/Swing {@link Node} class.
     * <br/>
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     *
     * @param env         the {@code Environment}
     * @param view        the controlled {@code Node}
     */
    public Wormhole2D(final Environment<?, P> env, final Node view) {
        super(
                env,
                new NodeViewType(view),
                from(view.getBoundsInLocal().getWidth() / 2, view.getBoundsInLocal().getHeight() / 2)
        );
    }
    @Override
    public Dimension2D getViewSize() {
        return new Dimension2DDouble(getView().getWidth(), getView().getHeight());
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
    protected double getViewRatio() {
        return Math.max(1, getView().getWidth()) / Math.max(1, getView().getHeight());
    }
}
