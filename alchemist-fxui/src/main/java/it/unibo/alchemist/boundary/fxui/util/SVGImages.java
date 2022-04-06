/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.fxui.util;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.InputStream;

import org.kaikikm.threadresloader.ResourceLoader;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.scene.image.Image;

/**
 * Utility class that install that wraps {@link SvgImageLoaderFactory}, letting
 * install a SVG loader and return a SVG image.
 */
public final class SVGImages {
    /**
     * Default Alchemist icon path.
     */
    public static final String DEFAULT_ALCHEMIST_ICON_PATH = "icon/icon.svg";

    /**
     * Private, empty, constructor, as this is an utility class.
     */
    private SVGImages() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    static {
        SvgImageLoaderFactory.install();
    }

    /**
     * Returns the Image of a SVG image.
     * 
     * @param path
     *            The SVG image position
     * @param width
     *            The percent width of image
     * @param height
     *            The percent height of image
     * @return The image
     */
    public static Image getSvgImage(final String path, final double width, final double height) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final InputStream imageStream = ResourceLoader.getResourceAsStream(path);
        return new Image(imageStream, screenSize.getWidth() * width / 100, screenSize.getHeight() * height / 100, true, true);
    }

    /**
     * Returns the Image of a SVG image.
     *
     * @param path
     *            The SVG image position
     * @return The image
     */
    public static Image getSvgImage(final String path) {
        final InputStream imageStream = ResourceLoader.getResourceAsStream(path);
        return new Image(imageStream);
    }

}
