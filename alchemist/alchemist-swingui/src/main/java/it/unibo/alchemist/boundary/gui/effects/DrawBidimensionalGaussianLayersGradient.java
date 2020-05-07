/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects;

/**
 * Draw a gradient in the background of the gui for {@link  it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer}s
 * values. It ignores any other layer.
 */
public class DrawBidimensionalGaussianLayersGradient extends DrawLayersGradient {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected LayerToFunctionMapper createMapper() {
        return new BidimensionalGaussianLayersMapper();
    }
}
