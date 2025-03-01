/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.impl;

import it.unibo.alchemist.boundary.swingui.effect.isolines.api.IsolinesFactory;
import it.unibo.alchemist.boundary.swingui.effect.isolines.impl.ConcreteIsolinesFactory;
import it.unibo.alchemist.model.layers.BidimensionalGaussianLayer;

import java.io.Serial;

/**
 * Draw isolines for {@link BidimensionalGaussianLayer}s.
 * It ignores any other layer.
 *
 * @deprecated The entire Swing UI is deprecated and planned to be replaced with a modern UI.
 */
@Deprecated
public class DrawBidimensionalGaussianLayersIsolines extends AbstractrDrawLayersIsolines {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     */
    public DrawBidimensionalGaussianLayersIsolines() {
        super(
            new ConcreteIsolinesFactory().makeIsolinesFinder(IsolinesFactory.IsolineFinders.CONREC),
            new BidimensionalGaussianLayersMapper()
        );
    }
}
