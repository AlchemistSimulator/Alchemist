/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.export.MeanSquaredError;

/**
 * Tests loading of a custom {@link it.unibo.alchemist.loader.export.Exporter}.
 */
public class TestCustomExport {

    /**
     * Test the ability to inject variables.
     */
    @Test
    public void testLoadCustomExport() {
        final List<Extractor> extractors = new YamlLoader(ResourceLoader.getResourceAsStream("testCustomExport.yml"))
            .getDataExtractors();
        assertEquals(1, extractors.size());
        assertEquals(MeanSquaredError.class, extractors.get(0).getClass());
    }
}
