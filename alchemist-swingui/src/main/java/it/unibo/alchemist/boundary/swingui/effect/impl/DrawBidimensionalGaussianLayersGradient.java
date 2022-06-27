/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.impl;

/**
 * Draw a gradient in the background of the gui for
 * {@link  it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer}s
 * values. It ignores any other layer.
 */
public class DrawBidimensionalGaussianLayersGradient extends DrawLayersGradient {

    private static final long serialVersionUID = 1L;

    /**
     * Builds a new {@link DrawBidimensionalGaussianLayersGradient}.
     */
    public DrawBidimensionalGaussianLayersGradient() {
        super(new BidimensionalGaussianLayersMapper());
    }
}
