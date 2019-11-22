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
import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import org.danilopianini.lang.CollectionWithCurrentElement;
import org.danilopianini.lang.ImmutableCollectionWithCurrentElement;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 */
public abstract class DrawLayer implements Effect {

    private static final int MAX_NUMBER_OF_CONTOUR_LEVELS = 50;
    private static final int MAX_COLOUR_VALUE = 255;
    private static final Logger L = LoggerFactory.getLogger(DrawShape.class);
    private static final long serialVersionUID = 1L;
    @ExportForGUI(nameToExport = "Number of contour levels")
    private RangedInteger contourLevels = new RangedInteger(0, MAX_NUMBER_OF_CONTOUR_LEVELS);
    @ExportForGUI(nameToExport = "A")
    private RangedInteger alpha = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE / 2);
    @ExportForGUI(nameToExport = "Draw only layer containing a molecule")
    private boolean layerFilter;
    @ExportForGUI(nameToExport = "Incarnation to use")
    private CollectionWithCurrentElement<String> curIncarnation;
    @ExportForGUI(nameToExport = "Molecule")
    private String molString = "";
    @ExportForGUI(nameToExport = "R")
    private RangedInteger red = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "B")
    private RangedInteger blue = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "G")
    private RangedInteger green = new RangedInteger(0, MAX_COLOUR_VALUE);
    private Color colorCache = Color.BLACK;
    @Nullable
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Molecule molecule;
    @Nullable
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Object molStringCached;
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "If null, it gets reinitialized anyway if needed")
    private transient CollectionWithCurrentElement<String> prevIncarnation;
    private transient Incarnation<?, ?> incarnation;
    private transient Optional<Node> markerNode = Optional.empty();

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    public <T, P extends Position2D<P>> void apply(final Graphics2D g, final Node<T> n, final Environment<T, P> env, final IWormhole2D<P> wormhole) {
        if (markerNode.isEmpty()) {
            markerNode = Optional.of(n);
        }
        if (!env.getNodes().contains(n)) {
            markerNode = Optional.empty();
        }
        if (markerNode.isPresent() && markerNode.get() == n) { // same object ==
            if (layerFilter && (curIncarnation == null || incarnation == null || curIncarnation != prevIncarnation || molString != molStringCached)) {
                if (curIncarnation == null) {
                    final Set<String> availableIncarnations = SupportedIncarnations.getAvailableIncarnations();
                    if (availableIncarnations.isEmpty()) {
                        throw new IllegalStateException(getClass().getSimpleName() + " can't work if no incarnation is available.");
                    }
                    curIncarnation = new ImmutableCollectionWithCurrentElement<>(availableIncarnations, availableIncarnations.stream().findAny().get());
                }
                molStringCached = molString;
                prevIncarnation = curIncarnation;
                incarnation = SupportedIncarnations.get(curIncarnation.getCurrent())
                        .orElseThrow(() -> new IllegalStateException(curIncarnation.getCurrent() + " is not a valid incarnation."));
                /*
                 * Process in a separate thread: if it fails, does not kill EDT.
                 */
                final Thread th = new Thread(() -> molecule = incarnation.createMolecule(molString));
                th.start();
                try {
                    th.join();
                } catch (final InterruptedException e) {
                    L.error("Bug.", e);
                }
            }

            colorCache = new Color(red.getVal(), green.getVal(), blue.getVal(), alpha.getVal());
            g.setColor(colorCache);

            if (layerFilter && molecule != null && env.getLayer(molecule).isPresent()) {
                final Layer layer = env.getLayer(molecule).get();
                drawLayer(layer, g, wormhole, contourLevels.getVal());
            } else {
                env.getLayers().forEach(l -> drawLayer(l, g, wormhole, contourLevels.getVal()));
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
     * Effectively draws a layer.
     * @param layer - the layer to be drawn
     * @param g - Graphics2D
     * @param wormhole - wormhole
     * @param contourLevels - number of contour levels to be drawn
     */
    protected abstract void drawLayer(Layer layer, Graphics2D g, IWormhole2D wormhole, int contourLevels);

    /**
     * @return the number of contour levels
     */
    public RangedInteger getContourLevels() {
        return contourLevels;
    }

    /**
     * @param contourLevels the number of contour levels
     */
    public void setContourLevels(final RangedInteger contourLevels) {
        this.contourLevels = contourLevels;
    }

    /**
     * @return alpha channel
     */
    public RangedInteger getAlpha() {
        return alpha;
    }

    /**
     * @param alpha the alpha channel
     */
    public void setAlpha(final RangedInteger alpha) {
        this.alpha = alpha;
    }

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
     * @return a String representing the current incarnation
     */
    public CollectionWithCurrentElement<String> getCurIncarnation() {
        return curIncarnation;
    }

    /**
     * @param curIncarnation current incarnation
     */
    public void setCurIncarnation(final CollectionWithCurrentElement<String> curIncarnation) {
        this.curIncarnation = curIncarnation;
    }

    /**
     * @return a string representing the current molecule
     */
    public String getMolString() {
        return molString;
    }

    /**
     * @param molString the molecule
     */
    public void setMolString(final String molString) {
        this.molString = molString;
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
}

