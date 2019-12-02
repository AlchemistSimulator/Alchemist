/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Collection;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * This class collects the following responsibilities:
 * - it manages to draw layers only when necessary (as the apply method will be called for every node).
 * Every subclass must only define the {@link AbstractDrawLayers#drawLayers(Collection, Environment, Graphics2D, IWormhole2D)}
 * method, which is guaranteed to be called only when necessary.
 * - it declares gui controls for the selection of the color to use
 * - it declares gui controls for the selection of a filter, used to filter the layers to draw.
 * In particular, it allows the user to specify a molecule, meaning that only the layer
 * containing such molecule will be drawn (otherwise the effect is applied to all layers)
 */
public abstract class AbstractDrawLayers implements DrawLayers {

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
    private transient Optional<Node> markerNode = Optional.empty();

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "unchecked"})
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    public <T, P extends Position2D<P>> void apply(final Graphics2D g, final Node<T> n, final Environment<T, P> env, final IWormhole2D<P> wormhole) {
        // if marker node is no longer in the environment or it is no longer displayed, we need to change it
        if (markerNode.isPresent()
                && (!env.getNodes().contains(markerNode.get()) || !wormhole.isInsideView(wormhole.getViewPoint(env.getPosition((Node<T>) markerNode.get()))))) {
            markerNode = Optional.empty();
        }
        if (markerNode.isEmpty()) {
            markerNode = Optional.of(n);
        }
        if (markerNode.get() == n) { // at this point markerNode.isPresent() is always true, so we directly get it
            if (layerFilter && (molecule == null || molString != molStringCached)) {
                molStringCached = molString;
                env.getIncarnation().ifPresent(incarnation -> molecule = incarnation.createMolecule(molString));
            }
            colorCache = new Color(red.getVal(), green.getVal(), blue.getVal(), alpha.getVal());
            g.setColor(colorCache);
            final List<Layer<T, P>> toDraw = new ArrayList<>();
            if (layerFilter && molecule != null && env.getLayer(molecule).isPresent()) {
                toDraw.add(env.getLayer(molecule).get());
            } else {
                toDraw.addAll(env.getLayers());
            }
            if (!toDraw.isEmpty()) {
                drawLayers(toDraw, env, g, wormhole);
            }
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
    public abstract <T, P extends Position2D<P>> void drawLayers(Collection<Layer<T, P>> toDraw, Environment<T, P> env, Graphics2D g, IWormhole2D<P> wormhole);

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

