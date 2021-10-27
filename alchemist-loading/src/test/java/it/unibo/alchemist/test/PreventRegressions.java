/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.loader.LoadAlchemist;
import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.export.MeanSquaredError;
import it.unibo.alchemist.model.interfaces.Environment;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests loading of a custom {@link it.unibo.alchemist.loader.export.Exporter}.
 */
class PreventRegressions {

    /**
     * Test the ability to inject variables.
     */
    @Test
    void testLoadCustomExport() {
        final List<Extractor> extractors = LoadAlchemist.from(ResourceLoader.getResource("testCustomExport.yml"))
                .getDefault()
                .getExporters()
                .get(0)
                .getDataExtractor();
        assertEquals(1, extractors.size());
        assertEquals(MeanSquaredError.class, extractors.get(0).getClass());
    }

    /**
     * Test environment serialization and incarnation restoration.
     */
    @Test
    void testLoadAndSerialize() {
        final Environment<?, ?> env = LoadAlchemist
            .from(ResourceLoader.getResource("testCustomExport.yml"))
            .getDefault()
            .getEnvironment();
        assertTrue(env.getIncarnation().isPresent());
        final byte[] serialized = SerializationUtils.serialize(env);
        assertNotNull(serialized);
        final Object deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(env));
        assertNotNull(deserialized);
        assertEquals(env.getClass(), deserialized.getClass());
        assertTrue(((Environment<?, ?>) deserialized).getIncarnation().isPresent()); 
    }
}
