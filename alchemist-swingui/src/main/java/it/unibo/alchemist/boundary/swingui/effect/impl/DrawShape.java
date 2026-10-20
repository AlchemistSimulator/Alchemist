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
import it.unibo.alchemist.boundary.swingui.effect.api.Effect;
import it.unibo.alchemist.boundary.swingui.impl.ColorChannel;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.SupportedIncarnations;
import org.apache.commons.math3.util.FastMath;
import org.danilopianini.lang.CollectionWithCurrentElement;
import org.danilopianini.lang.ImmutableCollectionWithCurrentElement;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Set;

/**
 * Effect drawing nodes as shapes, optionally filtered and colored according to a molecule.
 *
 * @deprecated The entire Swing UI is deprecated and planned to be replaced with a modern UI.
 */
@Deprecated
@SuppressFBWarnings
public final class DrawShape implements Effect {

    private static final int DEFAULT_SIZE = 5;
    private static final int MAX_COLOUR_VALUE = 255;
    private static final int MAX_SCALE = 100;
    private static final int MIN_SCALE = 0;
    private static final int PROPERTY_SCALE = 10;
    private static final int SCALE_DIFF = MAX_SCALE - MIN_SCALE;
    private static final int SCALE_INITIAL = SCALE_DIFF / 2 + MIN_SCALE;
    private static final Logger L = LoggerFactory.getLogger(DrawShape.class);
    @ExportForGUI(nameToExport = "Incarnation to use")
    private CollectionWithCurrentElement<String> curIncarnation;
    @ExportForGUI(nameToExport = "Mode")
    private Mode mode = Mode.FILL_ELLIPSE;
    @ExportForGUI(nameToExport = "R")
    private RangedInteger red = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "B")
    private RangedInteger blue = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "G")
    private RangedInteger green = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "A")
    private RangedInteger alpha = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "Scale Factor")
    private RangedInteger scaleFactor = new RangedInteger(MIN_SCALE, MAX_SCALE, SCALE_INITIAL);
    @ExportForGUI(nameToExport = "Size")
    private RangedInteger size = new RangedInteger(0, 100, DEFAULT_SIZE);
    @ExportForGUI(nameToExport = "Draw only nodes containing a molecule")
    private boolean molFilter;
    @ExportForGUI(nameToExport = "Molecule")
    private String molString = "";
    @ExportForGUI(nameToExport = "Tune colors using a molecule property")
    private boolean molPropertyFilter;
    @ExportForGUI(nameToExport = "Molecule property")
    private String property = "";
    @ExportForGUI(nameToExport = "Write the value")
    private boolean writingPropertyValue;
    @ExportForGUI(nameToExport = "Channel to use")
    private ColorChannel c = ColorChannel.ALPHA;
    @ExportForGUI(nameToExport = "Reverse effect")
    private boolean reverse;
    @ExportForGUI(nameToExport = "Property order of magnitude")
    private RangedInteger propoom = new RangedInteger(-PROPERTY_SCALE, PROPERTY_SCALE, 0);
    @ExportForGUI(nameToExport = "Minimum property value")
    private RangedInteger minprop = new RangedInteger(-PROPERTY_SCALE, PROPERTY_SCALE, 0);
    @ExportForGUI(nameToExport = "Maximum property value")
    private RangedInteger maxprop = new RangedInteger(-PROPERTY_SCALE, PROPERTY_SCALE, PROPERTY_SCALE);
    private Color colorCache = Color.BLACK;
    @Nullable
    private transient Molecule molecule;
    @Nullable
    private transient Object molStringCached;
    private transient CollectionWithCurrentElement<String> prevIncarnation;
    private transient Incarnation<?, ?> incarnation;

    /**
     * Builds a new {@link DrawShape} effect.
     */
    public DrawShape() {
        final Set<String> availableIncarnations = SupportedIncarnations.getAvailableIncarnations();
        if (availableIncarnations.isEmpty()) {
            throw new IllegalStateException(getClass().getSimpleName() + " can't work if no incarnation is available.");
        }
        curIncarnation = new ImmutableCollectionWithCurrentElement<>(
                availableIncarnations,
                availableIncarnations.stream().findAny().get()
        );
    }

    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    @Deprecated
    public void apply(final Graphics2D g, final Node<?> n, final int x, final int y) {
        if (molString != molStringCached // NOPMD: pointer comparison is wanted here
                || incarnation == null
                || curIncarnation != prevIncarnation) { // NOPMD: pointer comparison is wanted here
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
        if (!molFilter || molecule != null && n.contains(molecule)) {
            final double ks = (scaleFactor.getVal() - MIN_SCALE) * 2 / (double) SCALE_DIFF;
            final int sizex = size.getVal();
            final int startx = x - sizex / 2;
            final int sizey = (int) Math.ceil(sizex * ks);
            final int starty = y - sizey / 2;
            final Color toRestore = g.getColor();
            colorCache = new Color(red.getVal(), green.getVal(), blue.getVal(), alpha.getVal());
            Color newcolor = colorCache;
            if (molPropertyFilter && molecule != null) {
                final int minV = (int) (minprop.getVal() * FastMath.pow(PROPERTY_SCALE, propoom.getVal()));
                final int maxV = (int) (maxprop.getVal() * FastMath.pow(PROPERTY_SCALE, propoom.getVal()));
                if (minV < maxV) {
                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    double propval = incarnation.readConcentration((Node) n, molecule, property);
                    if (isWritingPropertyValue()) {
                        g.setColor(colorCache);
                        g.drawString(Double.toString(propval), startx + sizex, starty + sizey);
                    }
                    propval = Math.min(Math.max(propval, minV), maxV);
                    propval = (propval - minV) / (maxV - minV);
                    if (reverse) {
                        propval = 1f - propval;
                    }
                    newcolor = c.alter(newcolor, (float) propval);
                }
            }
            g.setColor(newcolor);
            switch (mode) {
            case DRAW_ELLIPSE:
                g.drawOval(startx, starty, sizex, sizey);
                break;
            case DRAW_RECTANGLE:
                g.drawRect(startx, starty, sizex, sizey);
                break;
            case FILL_RECTANGLE:
                g.fillRect(startx, starty, sizex, sizey);
                break;
            case FILL_ELLIPSE:
            default:
                g.fillOval(startx, starty, sizex, sizey);
            }
            g.setColor(toRestore);
        }
    }

    /**
     * Returns the alpha channel.
     *
     * @return Alpha
     */
    public RangedInteger getAlpha() {
        return alpha;
    }

    /**
     * Sets the alpha channel.
     *
     * @param a alpha
     */
    public void setAlpha(final RangedInteger a) {
        this.alpha = a;
    }

    /**
     * Returns the blue channel.
     *
     * @return Blue
     */
    public RangedInteger getBlue() {
        return blue;
    }

    /**
     * Sets the blue channel.
     *
     * @param b blue
     */
    public void setBlue(final RangedInteger b) {
        this.blue = b;
    }

    /**
     * Returns the color channel altered according to the molecule property.
     *
     * @return Color Channel
     */
    public ColorChannel getColorChannel() {
        return c;
    }

    @Override
    public Color getColorSummary() {
        return colorCache;
    }

    /**
     * Returns the green channel.
     *
     * @return Green
     */
    public RangedInteger getGreen() {
        return green;
    }

    /**
     * Sets the green channel.
     *
     * @param g green
     */
    public void setGreen(final RangedInteger g) {
        this.green = g;
    }

    /**
     * Returns the incarnation to use.
     *
     * @return the current incarnation
     */
    public CollectionWithCurrentElement<String> getIncarnation() {
        return curIncarnation;
    }

    /**
     * Sets the incarnation to use.
     *
     * @param element incarnation
     */
    public void setIncarnation(final CollectionWithCurrentElement<String> element) {
        this.curIncarnation = element;
    }

    /**
     * Returns the maximum property value.
     *
     * @return maxprop
     */
    public RangedInteger getMaxprop() {
        return maxprop;
    }

    /**
     * Sets the maximum property value.
     *
     * @param mp maxprop
     */
    public void setMaxprop(final RangedInteger mp) {
        this.maxprop = mp;
    }

    /**
     * Returns the minimum property value.
     *
     * @return minprop
     */
    public RangedInteger getMinprop() {
        return minprop;
    }

    /**
     * Sets the minimum property value.
     *
     * @param mp minprop
     */
    public void setMinprop(final RangedInteger mp) {
        this.minprop = mp;
    }

    /**
     * Returns the drawing mode.
     *
     * @return current mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the drawing mode.
     *
     * @param m mode
     */
    public void setMode(final Mode m) {
        this.mode = m;
    }

    /**
     * Returns the molecule used for filtering.
     *
     * @return molecule
     */
    @org.jetbrains.annotations.Nullable
    public Molecule getMolecule() {
        return molecule;
    }

    /**
     * Returns the string representing the molecule.
     *
     * @return molString
     */
    public String getMolString() {
        return molString;
    }

    /**
     * Sets the string representing the molecule.
     *
     * @param mols molString
     */
    public void setMolString(final String mols) {
        this.molString = mols;
    }

    /**
     * Returns the name of the molecule property.
     *
     * @return property
     */
    public String getProperty() {
        return property;
    }

    /**
     * Sets the name of the molecule property.
     *
     * @param pr property
     */
    public void setProperty(final String pr) {
        this.property = pr;
    }

    /**
     * Returns the order of magnitude of the property.
     *
     * @return propoom
     */
    public RangedInteger getPropoom() {
        return propoom;
    }

    /**
     * Sets the order of magnitude of the property.
     *
     * @param oom Order of magnitude
     */
    public void setPropoom(final RangedInteger oom) {
        this.propoom = oom;
    }

    /**
     * Returns the red channel.
     *
     * @return red
     */
    public RangedInteger getRed() {
        return red;
    }

    /**
     * Sets the red channel.
     *
     * @param r red
     */
    public void setRed(final RangedInteger r) {
        this.red = r;
    }

    /**
     * Returns the scale factor.
     *
     * @return scaleFactor
     */
    public RangedInteger getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Sets the scale factor.
     *
     * @param sf scaleFactor
     */
    public void setScaleFactor(final RangedInteger sf) {
        this.scaleFactor = sf;
    }

    /**
     * Returns the size of the shape.
     *
     * @return size
     */
    public RangedInteger getSize() {
        return size;
    }

    /**
     * Sets the size of the shape.
     *
     * @param s size
     */
    public void setSize(final RangedInteger s) {
        this.size = s;
    }

    /**
     * Checks whether only nodes containing the molecule are drawn.
     *
     * @return molFilter
     */
    public boolean isMolFilter() {
        return molFilter;
    }

    /**
     * Sets whether only nodes containing the molecule are drawn.
     *
     * @param mol
     *            molFilter
     */
    public void setMolFilter(final boolean mol) {
        this.molFilter = mol;
    }

    /**
     * Checks whether colors are tuned using a molecule property.
     *
     * @return molPropertyFilter
     */
    public boolean isMolPropertyFilter() {
        return molPropertyFilter;
    }

    /**
     * Sets whether colors are tuned using a molecule property.
     *
     * @param molpf molPropertyFilter
     */
    public void setMolPropertyFilter(final boolean molpf) {
        this.molPropertyFilter = molpf;
    }

    /**
     * Checks whether the effect of the property is reversed.
     *
     * @return reverse
     */
    public boolean isReverse() {
        return reverse;
    }

    /**
     * Sets whether the effect of the property is reversed.
     *
     * @param r
     *            reverse
     */
    public void setReverse(final boolean r) {
        this.reverse = r;
    }

    /**
     * Sets the color channel altered according to the molecule property.
     *
     * @param colorChannel colorChannel
     */
    public void setC(final ColorChannel colorChannel) {
        this.c = colorChannel;
    }

    /**
     * Checks whether the property value is written.
     *
     * @return true if it is writing the property value
     */
    public boolean isWritingPropertyValue() {
        return writingPropertyValue;
    }

    /**
     * Sets whether the property value is written.
     *
     * @param writingPropertyValue
     *            if it should draw the property value
     */
    public void setWritingPropertyValue(final boolean writingPropertyValue) {
        this.writingPropertyValue = writingPropertyValue;
    }

    /**
     * Drawing modes for the shapes.
     */
    public enum Mode {
        /**
         * Draws the outline of an ellipse.
         */
        DRAW_ELLIPSE,

        /**
         * Draws the outline of a rectangle.
         */
        DRAW_RECTANGLE,

        /**
         * Draws a filled ellipse.
         */
        FILL_ELLIPSE,

        /**
         * Draws a filled rectangle.
         */
        FILL_RECTANGLE;

        @Override
        public String toString() {
            final String sup = super.toString();
            final StringBuilder sb = new StringBuilder(2 * sup.length());
            if (!sup.isEmpty()) {
                sb.append(sup.charAt(0));
            }
            for (int i = 1; i < sup.length(); i++) {
                final char curChar = sup.charAt(i);
                if (Character.isUpperCase(curChar)) {
                    sb.append(' ');
                }
                sb.append(curChar);
            }
            return sb.toString();
        }
    }
}
