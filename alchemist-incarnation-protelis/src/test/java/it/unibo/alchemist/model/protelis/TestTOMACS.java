/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.protelis;

import it.unibo.alchemist.loader.LoadAlchemist;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.protelis.actions.RunProtelisProgram;
import it.unibo.alchemist.protelis.properties.ProtelisDevice;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.util.Collection;
import java.util.stream.StreamSupport;

/**
 * Tests that the TOMACS setup could be successfully loaded (in particular,
 * Protelis rounds with personalized message retaining time).
 */
class TestTOMACS {

    /**
     * 
     */
    @Test
    @SuppressWarnings("unchecked")
    void testCustomRetainTimeLoading() {
        final Loader loader = LoadAlchemist.from(ResourceLoader.getResource("tomacs.yml"));
        Assertions.assertTrue(StreamSupport.stream(loader.getDefault().getEnvironment().spliterator(), false)
            .flatMap(n -> n.getReactions().stream()
                .map(Reaction::getActions)
                .flatMap(Collection::stream)
                .filter(a -> a instanceof RunProtelisProgram)
                .map(a -> (RunProtelisProgram<?>) a)
                .map(a -> n.asProperty(ProtelisDevice.class).getNetworkManager(a)))
            .mapToDouble(AlchemistNetworkManager::getRetentionTime)
            .peek(d -> Assertions.assertTrue(Double.isFinite(d)))
            .count() > 0);
    }

}
