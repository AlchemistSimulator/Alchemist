/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.loader.LoadAlchemist;
import it.unibo.alchemist.loader.export.exporters.CSVExporter;
import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.export.Exporter;
import it.unibo.alchemist.loader.export.extractors.MeanSquaredError;
import it.unibo.alchemist.model.Environment;
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
class TestRegressions {

    private static Exporter<?, ?> exporterOf(final String simulation) {
        final var exporters = LoadAlchemist
            .from(ResourceLoader.getResource(simulation))
            .getDefault()
            .getExporters();
        assertEquals(1, exporters.size());
        return exporters.get(0);
    }

    /**
     * Test the ability to inject variables.
     */
    @Test
    void testLoadCustomExport() {
        final var exporter = exporterOf("testCustomExport.yml");
        assertTrue(CSVExporter.class.isAssignableFrom(exporter.getClass()));
        final List<Extractor<?>> dataExtractors = exporter.getDataExtractors();
        assertEquals(1, dataExtractors.size());
        assertEquals(MeanSquaredError.class, dataExtractors.get(0).getClass());
    }

    /**
     * Test environment serialization and incarnation restoration.
     */
    @Test
    void testLoadAndSerialize() {
        final Environment<?, ?> environment = LoadAlchemist
            .from(ResourceLoader.getResource("testCustomExport.yml"))
            .getDefault()
            .getEnvironment();
        assertNotNull(environment.getIncarnation());
        final byte[] serialized = SerializationUtils.serialize(environment);
        assertNotNull(serialized);
        final Object deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(environment));
        assertNotNull(deserialized);
        assertEquals(environment.getClass(), deserialized.getClass());
        assertNotNull(((Environment<?, ?>) deserialized).getIncarnation());
    }
}
