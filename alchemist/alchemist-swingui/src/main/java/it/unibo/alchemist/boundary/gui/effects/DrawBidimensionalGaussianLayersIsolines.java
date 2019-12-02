/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.isolines.ConcreteIsolinesFactory;
import it.unibo.alchemist.boundary.gui.isolines.IsolinesFactory;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Position2D;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.function.Function;

/**
 * Draw isolines for {@link BidimensionalGaussianLayer}s. It ignores any other layer.
 *
 * This class also manages to infer optimal min and max layer values automatically
 * so the user does not have to set them by hand.
 */
public class DrawBidimensionalGaussianLayersIsolines extends DrawLayersIsolines {

    private static final long serialVersionUID = 1L;
    private boolean minAndMaxToBeSet = true;

    /**
     */
    public DrawBidimensionalGaussianLayersIsolines() {
        super(new ConcreteIsolinesFactory().makeIsolinesFinder(IsolinesFactory.IsolineFinders.CONREC));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <T, P extends Position2D<P>> void drawLayers(final Collection<Layer<T, P>> toDraw, final Environment<T, P> env, final Graphics2D g, final IWormhole2D<P> wormhole) {
        if (minAndMaxToBeSet) {
            final Double minLayerValue = 0.1;
            final Double maxLayerValue = toDraw.stream()
                    .filter(l -> l instanceof BidimensionalGaussianLayer)
                    .map(l -> (BidimensionalGaussianLayer) l)
                    .map(l -> l.getValue(env.makePosition(l.getCenterX(), l.getCenterY())))
                    .max(Double::compare).orElse(minLayerValue);
            super.setMinLayerValue(minLayerValue.toString());
            super.setMaxLayerValue(maxLayerValue.toString());
            minAndMaxToBeSet = false;
        }

        toDraw.stream()
                .filter(l -> l instanceof BidimensionalGaussianLayer)
                .map(l -> (BidimensionalGaussianLayer) l)
                .forEach(l -> super.drawValues((Function<P, Number>) l::getValue, env, g, wormhole));
    }
}
