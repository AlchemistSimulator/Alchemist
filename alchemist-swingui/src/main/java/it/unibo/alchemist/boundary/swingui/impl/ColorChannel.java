/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.impl;

import java.awt.Color;

/**
 */
public enum ColorChannel {

    /**
     * Alpha channel.
     */
    ALPHA(3, true),
    /**
     * Red channel.
     */
    RED(0, true),
    /**
     * Green channel.
     */
    GREEN(1, true),
    /**
     * Hue channel (HSB).
     */
    HUE(0, false),
    /**
     * Saturation channel (HSB).
     */
    SATURATION(1, false),
    /**
     * Brightness channel (HSB).
     */
    BRIGHTNESS(2, false),
    /**
     * Blue channel.
     */
    BLUE(2, true);

    private final int channel;
    private final boolean isRGB;

    ColorChannel(final int idx, final boolean rgb) {
        channel = idx;
        isRGB = rgb;
    }

    /**
     * Given a color, modifies the channel setting the passed value.
     *
     * @param c
     *            the initial color
     * @param q
     *            the value for the channel
     * @return the modified color
     */
    public Color alter(final Color c, final float q) {
        final float[] cc = c.getRGBComponents(null);
        if (isRGB) {
            cc[channel] = q;
            return new Color(cc[0], cc[1], cc[2], cc[3]);
        }
        final float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), new float[4]);
        hsb[channel] = q;
        final Color temp = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
        temp.getRGBComponents(hsb);
        return new Color(hsb[0], hsb[1], hsb[2], cc[3]);
    }

}
