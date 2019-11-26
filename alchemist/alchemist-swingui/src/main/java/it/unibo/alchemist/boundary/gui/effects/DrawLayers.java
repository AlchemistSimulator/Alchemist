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
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.danilopianini.lang.CollectionWithCurrentElement;
import org.danilopianini.lang.ImmutableCollectionWithCurrentElement;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.Collection;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Basic class for every effect that draws something related to layers.
 *
 * This class is a workaround: the {@link Effect} abstraction is meant to add effects
 * to nodes, not to draw layers. At present, is the finest workaround available.
 * This workaround has the following disadvantages:
 * - when there aren't nodes visible in the gui the effects are not used at all,
 * so this effect won't work.
 *
 * This class collects the following responsibilities:
 * - it manages to draw layers only when necessary (as the apply method will be called for every node).
 * Every subclass must only define the drawLayers method, which is guaranteed to be called
 * only when necessary.
 * - it declares gui controls for the selection of the color to use
 * - it declares gui controls for the selection of a filter, used to filter the layers to draw.
 * In particular, it allows the user to specify a molecule, meaning that only the layer
 * containing such molecule will be drawn (otherwise the effect is applied to all layers)
 */
public abstract class DrawLayers implements Effect {

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
    @ExportForGUI(nameToExport = "Incarnation to use")
    private CollectionWithCurrentElement<String> curIncarnation;
    @ExportForGUI(nameToExport = "Molecule")
    private String molString = "";
    @ExportForGUI(nameToExport = "A")
    private RangedInteger alpha = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE / INITIAL_ALPHA_DIVIDER);
    @ExportForGUI(nameToExport = "R")
    private RangedInteger red = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "G")
    private RangedInteger green = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "B")
    private RangedInteger blue = new RangedInteger(MAX_COLOUR_VALUE, MAX_COLOUR_VALUE);
    private Color colorCache = Color.BLUE;
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
     */
    public DrawLayers() {
        final Set<String> availableIncarnations = SupportedIncarnations.getAvailableIncarnations();
        if (availableIncarnations.isEmpty()) {
            throw new IllegalStateException(getClass().getSimpleName() + " can't work if no incarnation is available.");
        }
        curIncarnation = new ImmutableCollectionWithCurrentElement<>(availableIncarnations, availableIncarnations.stream().findAny().get());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "unchecked"})
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    public <T, P extends Position2D<P>> void apply(final Graphics2D g, final Node<T> n, final Environment<T, P> env, final IWormhole2D<P> wormhole) {
        // if marker node is no longer in the environment or it is no longer displayed, we need to change it
        if (markerNode.isPresent() && (!env.getNodes().contains(markerNode.get())
                                        || !wormhole.isInsideView(wormhole.getViewPoint(env.getPosition((Node<T>) markerNode.get()))))) {
            markerNode = Optional.empty();
        }
        if (markerNode.isEmpty()) {
            markerNode = Optional.of(n);
        }
        if (markerNode.get() == n) { // at this point markerNode.isPresent() is always true, so we directly get it
            if (layerFilter && (incarnation == null || curIncarnation != prevIncarnation || molString != molStringCached)) {
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
     * Effectively draw the layers. This method will be called only when re-drawing the layers is necessary.
     *
     * @param toDraw   - the layers to draw
     * @param env      - the environment (mainly used to create positions)
     * @param g        - the graphics2D
     * @param wormhole - the wormhole
     * @param <T>      - node concentration type
     * @param <P>      - position type
     */
    protected abstract <T, P extends Position2D<P>> void drawLayers(Collection<Layer<T, P>> toDraw, Environment<T, P> env, Graphics2D g, IWormhole2D<P> wormhole);

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
     * @param curIncarnation a String representing the incarnation to use
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

