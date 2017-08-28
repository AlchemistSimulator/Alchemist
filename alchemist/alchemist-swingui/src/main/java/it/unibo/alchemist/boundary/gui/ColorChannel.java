/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

/**
 * Enumeration that models all the possible channels of a color.
 */
public enum ColorChannel {

    /**
     * Alpha channel.
     */
    Alpha(3, true),
    /**
     * Red channel.
     */
    Red(0, true),
    /**
     * Green channel.
     */
    Green(1, true),
    /**
     * Hue channel (HSB).
     */
    Hue(0, false),
    /**
     * Saturation channel (HSB).
     */
    Saturation(1, false),
    /**
     * Brightness channel (HSB).
     */
    Brightness(2, false),
    /**
     * Blue channel.
     */
    Blue(2, true);

    private final int channel;
    private final boolean isRGB;

    ColorChannel(final int idx, final boolean rgb) {
        channel = idx;
        isRGB = rgb;
    }

    /**
     * Given a color, modifies the channel setting the passed value.
     *
     * @param c the initial color
     * @param q the value for the channel
     * @return the modified color
     */
    public java.awt.Color alter(final java.awt.Color c, final float q) {
        final float[] cc = c.getRGBComponents(null);
        if (isRGB) {
            cc[channel] = q;
            return new java.awt.Color(cc[0], cc[1], cc[2], cc[3]);
        }
        final float[] hsb = java.awt.Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), new float[4]);
        hsb[channel] = q;
        final java.awt.Color temp = new java.awt.Color(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
        temp.getRGBComponents(hsb);
        return new java.awt.Color(hsb[0], hsb[1], hsb[2], cc[3]);
    }

    /**
     * Given a color, modifies the channel setting the passed value.
     *
     * @param c the initial color
     * @param q the value for the channel
     * @return the modified color
     */
    public javafx.scene.paint.Color alter(final javafx.scene.paint.Color c, final float q) {
        final java.awt.Color color = alter(new java.awt.Color((float) c.getRed(), (float) c.getGreen(), (float) c.getBlue(), (float) c.getOpacity()), q);
        return javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
}
