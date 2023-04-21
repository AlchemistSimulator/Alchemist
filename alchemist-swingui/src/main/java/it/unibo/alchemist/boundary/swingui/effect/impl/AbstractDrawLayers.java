/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.swingui.effect.api.DrawLayers;
import it.unibo.alchemist.boundary.ui.api.Wormhole2D;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Layer;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position2D;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class collects the following responsibilities:
 * - it manages to draw layers only when necessary (as the apply method will be called for every node).
 * Every subclass must only define the
 * {@link AbstractDrawLayers#drawLayers(Collection, Environment, Graphics2D, Wormhole2D)}
 * method, which is guaranteed to be called only when necessary.
 * - it declares gui controls for the selection of the color to use
 * - it declares gui controls for the selection of a filter, used to filter the layers to draw.
 * In particular, it allows the user to specify a molecule, meaning that only the layer
 * containing such molecule will be drawn (otherwise the effect is applied to all layers)
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public abstract class AbstractDrawLayers extends AbstractDrawOnce implements DrawLayers {

    /**
     */
    protected static final int MAX_COLOUR_VALUE = 255;
    /**
     */
    protected static final int INITIAL_ALPHA_DIVIDER = 2;
    /**
     */
    protected static final Logger L = LoggerFactory.getLogger(DrawShape.class);
    private static final long serialVersionUID = 1L;
    @ExportForGUI(nameToExport = "Draw only layer containing a molecule")
    private boolean layerFilter;
    @ExportForGUI(nameToExport = "Molecule")
    private String molString = "";
    @ExportForGUI(nameToExport = "A")
    private RangedInteger alpha = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE / INITIAL_ALPHA_DIVIDER);
    @ExportForGUI(nameToExport = "R")
    private RangedInteger red = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "G")
    private RangedInteger green = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "B")
    private RangedInteger blue = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE);
    private Color colorCache = Color.BLUE;
    @Nullable
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Molecule molecule;
    @Nullable
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Object molStringCached;
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"PMD.CompareObjectsWithEquals"})
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    protected <T, P extends Position2D<P>> void draw(
            final Graphics2D graphics2D,
            final Node<T> node,
            final Environment<T, P> environment,
            final Wormhole2D<P> wormhole
    ) {
        if (layerFilter && (molecule == null || molString != molStringCached)) {
            molStringCached = molString;
            molecule = environment.getIncarnation().createMolecule(molString);
        }
        colorCache = new Color(red.getVal(), green.getVal(), blue.getVal(), alpha.getVal());
        graphics2D.setColor(colorCache);
        final List<Layer<T, P>> toDraw = new ArrayList<>();
        if (layerFilter && molecule != null && environment.getLayer(molecule).isPresent()) {
            toDraw.add(environment.getLayer(molecule).get());
        } else {
            toDraw.addAll(environment.getLayers());
        }
        if (!toDraw.isEmpty()) {
            drawLayers(toDraw, environment, graphics2D, wormhole);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColorSummary() {
        return colorCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract <T, P extends Position2D<P>> void drawLayers(
            Collection<Layer<T, P>> toDraw,
            Environment<T, P> environment,
            Graphics2D graphics,
            Wormhole2D<P> wormhole
    );

    /**
     * @return a boolean representing whether or not layer filter is on
     */
    public boolean isLayerFilter() {
        return layerFilter;
    }

    /**
     * @param layerFilter a boolean representing whether or not layer filter must be on
     */
    public void setLayerFilter(final boolean layerFilter) {
        this.layerFilter = layerFilter;
    }

    /**
     * @return a string representing the current molecule
     */
    public String getMolString() {
        return molString;
    }

    /**
     * @param molString a string representing the molecule to use
     */
    public void setMolString(final String molString) {
        this.molString = molString;
    }

    /**
     * @return alpha channel
     */
    public RangedInteger getAlpha() {
        return alpha;
    }

    /**
     * @param alpha alpha channel
     */
    public void setAlpha(final RangedInteger alpha) {
        this.alpha = alpha;
    }

    /**
     * @return red channel
     */
    public RangedInteger getRed() {
        return red;
    }

    /**
     * @param red red channel
     */
    public void setRed(final RangedInteger red) {
        this.red = red;
    }

    /**
     * @return green channel
     */
    public RangedInteger getGreen() {
        return green;
    }

    /**
     * @param green green channel
     */
    public void setGreen(final RangedInteger green) {
        this.green = green;
    }

    /**
     * @return blue channel
     */
    public RangedInteger getBlue() {
        return blue;
    }

    /**
     * @param blue blue channel
     */
    public void setBlue(final RangedInteger blue) {
        this.blue = blue;
    }

    /**
     * @return the cached color
     */
    protected Color getColorCache() {
        return this.colorCache;
    }

    /**
     * @param c color
     */
    protected void setColorCache(final Color c) {
        this.colorCache = c;
    }

    /**
     * @return the molecule used for layer filter
     */
    @org.jetbrains.annotations.Nullable
    protected Molecule getMolecule() {
        return this.molecule;
    }
}

