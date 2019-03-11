/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.boundary.gui.ColorChannel;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;
import org.danilopianini.lang.CollectionWithCurrentElement;
import org.danilopianini.lang.ImmutableCollectionWithCurrentElement;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 */
public class DrawShape implements Effect {

    /**
     */
    public enum Mode {
        /**
         * 
         */
        DrawEllipse, DrawRectangle, FillEllipse, FillRectangle;

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

    private static final int DEFAULT_SIZE = 5;
    private static final int MAX_COLOUR_VALUE = 255;
    private static final int MAX_SCALE = 100;
    private static final int MIN_SCALE = 0;
    private static final int PROPERTY_SCALE = 10;
    private static final int SCALE_DIFF = MAX_SCALE - MIN_SCALE;
    private static final int SCALE_INITIAL = (SCALE_DIFF) / 2 + MIN_SCALE;
    private static final Logger L = LoggerFactory.getLogger(DrawShape.class);

    private static final long serialVersionUID = 1L;

    @ExportForGUI(nameToExport = "Incarnation to use")
    private CollectionWithCurrentElement<String> curIncarnation;
    @ExportForGUI(nameToExport = "Mode")
    private Mode mode = Mode.FillEllipse;
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
    private ColorChannel c = ColorChannel.Alpha;
    @ExportForGUI(nameToExport = "Reverse effect")
    private boolean reverse;
    @ExportForGUI(nameToExport = "Property order of magnitude")
    private RangedInteger propoom = new RangedInteger(-PROPERTY_SCALE, PROPERTY_SCALE, 0);
    @ExportForGUI(nameToExport = "Minimum property value")
    private RangedInteger minprop = new RangedInteger(-PROPERTY_SCALE, PROPERTY_SCALE, 0);
    @ExportForGUI(nameToExport = "Maximum property value")
    private RangedInteger maxprop = new RangedInteger(-PROPERTY_SCALE, PROPERTY_SCALE, PROPERTY_SCALE);

    private Color colorCache = Color.BLACK;
    private transient Molecule molecule;
    private transient Object molStringCached;
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "If null, it gets reinitialized anyway if needed")
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
        curIncarnation = new ImmutableCollectionWithCurrentElement<>(availableIncarnations, availableIncarnations.stream().findAny().get());
    }

    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    public void apply(final Graphics2D g, final Node<?> n, final int x, final int y) {
        if (molString != molStringCached // NOPMD: pointer comparison is wanted here
                || incarnation == null
                || curIncarnation != prevIncarnation) { // NOPMD: pointer comparison is wanted here
            molStringCached = molString;
            prevIncarnation = curIncarnation;
            incarnation = SupportedIncarnations.get(curIncarnation.getCurrent()).get();
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
        if (!molFilter || (molecule != null && n.contains(molecule))) {
            final double ks = (scaleFactor.getVal() - MIN_SCALE) * 2 / (double) (SCALE_DIFF);
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
                    double propval = incarnation.getProperty((Node) n, molecule, property);
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
            case FillEllipse:
                g.fillOval(startx, starty, sizex, sizey);
                break;
            case DrawEllipse:
                g.drawOval(startx, starty, sizex, sizey);
                break;
            case DrawRectangle:
                g.drawRect(startx, starty, sizex, sizey);
                break;
            case FillRectangle:
                g.fillRect(startx, starty, sizex, sizey);
                break;
            default:
                g.fillOval(startx, starty, sizex, sizey);
            }
            g.setColor(toRestore);
        }
    }

    /**
     * @return Alpha
     */
    protected RangedInteger getAlpha() {
        return alpha;
    }

    /**
     * @return Blue
     */
    protected RangedInteger getBlue() {
        return blue;
    }

    /**
     * @return Color Channel
     */
    protected ColorChannel getColorChannel() {
        return c;
    }

    @Override
    public Color getColorSummary() {
        return colorCache;
    }

    /**
     * @return Green
     */
    protected RangedInteger getGreen() {
        return green;
    }

    /**
     * @return the current incarnation
     */
    protected CollectionWithCurrentElement<String> getIncarnation() {
        return curIncarnation;
    }

    /**
     * @return maxprop
     */
    protected RangedInteger getMaxprop() {
        return maxprop;
    }

    /**
     * @return minprop
     */
    protected RangedInteger getMinprop() {
        return minprop;
    }

    /**
     * @return current mode
     */
    protected Mode getMode() {
        return mode;
    }

    /**
     * @return molecule
     */
    protected Molecule getMolecule() {
        return molecule;
    }

    /**
     * @return molString
     */
    protected String getMolString() {
        return molString;
    }

    /**
     * @return property
     */
    protected String getProperty() {
        return property;
    }

    /**
     * @return propoom
     */
    protected RangedInteger getPropoom() {
        return propoom;
    }

    /**
     * @return red
     */
    protected RangedInteger getRed() {
        return red;
    }

    /**
     * @return scaleFactor
     */
    protected RangedInteger getScaleFactor() {
        return scaleFactor;
    }

    /**
     * @return size
     */
    protected RangedInteger getSize() {
        return size;
    }

    /**
     * @return molFilter
     */
    protected boolean isMolFilter() {
        return molFilter;
    }

    /**
     * @return molPropertyFilter
     */
    protected boolean isMolPropertyFilter() {
        return molPropertyFilter;
    }

    /**
     * @return reverse
     */
    protected boolean isReverse() {
        return reverse;
    }

    /**
     * @param a alpha
     */
    protected void setAlpha(final RangedInteger a) {
        this.alpha = a;
    }

    /**
     * @param b blue
     */
    protected void setBlue(final RangedInteger b) {
        this.blue = b;
    }

    /**
     * @param colorChannel colorChannel
     */
    protected void setC(final ColorChannel colorChannel) {
        this.c = colorChannel;
    }

    /**
     * @param g green
     */
    protected void setGreen(final RangedInteger g) {
        this.green = g;
    }

    /**
     * @param element incarnation
     */
    protected void setIncarnation(final CollectionWithCurrentElement<String> element) {
        this.curIncarnation = element;
    }

    /**
     * @param mp maxprop
     */
    protected void setMaxprop(final RangedInteger mp) {
        this.maxprop = mp;
    }

    /**
     * @param mp minprop
     */
    protected void setMinprop(final RangedInteger mp) {
        this.minprop = mp;
    }

    /**
     * @param m mode
     */
    protected void setMode(final Mode m) {
        this.mode = m;
    }

    /**
     * @param mol molFilter
     */
    protected void setMolFilter(final boolean mol) {
        this.molFilter = mol;
    }
    /**
     * @param molpf molPropertyFilter
     */
    protected void setMolPropertyFilter(final boolean molpf) {
        this.molPropertyFilter = molpf;
    }
    /**
     * @param mols molString
     */
    protected void setMolString(final String mols) {
        this.molString = mols;
    }

    /**
     * @param pr property
     */
    protected void setProperty(final String pr) {
        this.property = pr;
    }

    /**
     * @param oom Order of magnitude
     */
    protected void setPropoom(final RangedInteger oom) {
        this.propoom = oom;
    }

    /**
     * @param r red
     */
    protected void setRed(final RangedInteger r) {
        this.red = r;
    }

    /**
     * @param r reverse
     */
    protected void setReverse(final boolean r) {
        this.reverse = r;
    }

    /**
     * @param sf scaleFactor
     */
    protected void setScaleFactor(final RangedInteger sf) {
        this.scaleFactor = sf;
    }

    /**
     * @param s size
     */
    protected void setSize(final RangedInteger s) {
        this.size = s;
    }

    /**
     * @return true if it is writing the property value
     */
    protected boolean isWritingPropertyValue() {
        return writingPropertyValue;
    }

    /**
     * @param writingPropertyValue if it should draw the property value
     */
    protected void setWritingPropertyValue(final boolean writingPropertyValue) {
        this.writingPropertyValue = writingPropertyValue;
    }


}


