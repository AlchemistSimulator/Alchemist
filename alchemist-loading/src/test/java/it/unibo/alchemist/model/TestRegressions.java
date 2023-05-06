/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import it.unibo.alchemist.boundary.Exporter;
import it.unibo.alchemist.boundary.Extractor;
import it.unibo.alchemist.boundary.LoadAlchemist;
import it.unibo.alchemist.boundary.exporters.CSVExporter;
import it.unibo.alchemist.boundary.extractors.MeanSquaredError;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests loading of a custom {@link Exporter}.
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
