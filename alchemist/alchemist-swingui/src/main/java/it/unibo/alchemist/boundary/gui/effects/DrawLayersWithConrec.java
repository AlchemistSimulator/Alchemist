/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Layer;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;

/**
 */
public class DrawLayersWithConrec extends DrawLayers {

    private static final long serialVersionUID = 1L;
    //private static final int SAMPLES = 100;

    /**
     *
     * @param layer - the layer to be drawn
     * @param g - Graphics2D
     * @param wormhole - wormhole
     * @param contourLevels - number of contour levels to be drawn
     */
    @Override
    protected void drawLayer(final Layer layer, final Graphics2D g, final IWormhole2D wormhole, final int contourLevels) {
        final Dimension2D viewSize = wormhole.getViewSize();
        final int viewStartX = 0;
        final int viewStartY = 0;
        final int viewEndX = (int) Math.ceil(viewSize.getWidth());
        final int viewEndY = (int) Math.ceil(viewSize.getHeight());
        //final int envStartX = wormhole.getEnvPoint();
        //L.debug(viewSize.getWidth());
        g.drawLine(viewStartX, viewStartY, viewEndX, viewEndY);
    }
}
