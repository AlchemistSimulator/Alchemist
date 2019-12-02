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

/**
 * Draw isolines for {@link it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer}s.
 * It ignores any other layer.
 */
public class DrawBidimensionalGaussianLayersIsolines extends DrawLayersIsolines {

    /**
     */
    public DrawBidimensionalGaussianLayersIsolines() {
        super(new ConcreteIsolinesFactory().makeIsolinesFinder(IsolinesFactory.IsolineFinders.CONREC));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LayerToFunctionMapper createMapper() {
        return new BidimensionalGaussianLayersMapper();
    }
}
