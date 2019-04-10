/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.export.MeanSquaredError;
import it.unibo.alchemist.model.interfaces.Environment;

/**
 * Tests loading of a custom {@link it.unibo.alchemist.loader.export.Exporter}.
 */
public class PreventRegressions {

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

    /**
     * Test environment serialization and incarnation restoration.
     */
    @Test
    public void testLoadAndSerialize() {
        final Environment<?, ?> env = new YamlLoader(ResourceLoader.getResourceAsStream("testCustomExport.yml")).getDefault();
        assertTrue(env.getIncarnation().isPresent());
        final byte[] serialized = SerializationUtils.serialize(env);
        assertNotNull(serialized);
        final Object deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(env));
        assertNotNull(deserialized);
        assertEquals(env.getClass(), deserialized.getClass());
        assertTrue(((Environment<?, ?>) deserialized).getIncarnation().isPresent()); 
    }
}
