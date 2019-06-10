/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview;
import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.scene.image.Image;

/**
 * The the use of SVG images.
 *
 */
public class TestLoadSvgImage {

    /**
     * 
     */
    public static void installSvgLoader() {
        SvgImageLoaderFactory.install();
    }

    /**
     * 
     */
    @Test
    public void testImage() {
        final InputStream imageData = ResourceLoader.getResourceAsStream("icon/testicon.svg");
        Assertions.assertNotNull(imageData);
        final Image image = new Image(imageData);
        Assertions.assertNotNull(image);
    }

}
