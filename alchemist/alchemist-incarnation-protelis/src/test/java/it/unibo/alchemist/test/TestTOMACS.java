/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import java.util.Collection;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;

/**
 * Tests that the TOMACS setup could be successfully loaded (in particular,
 * Protelis rounds with personalized message retaining time).
 */
public class TestTOMACS {

    /**
     * 
     */
    @Test
    public void testCustomRetainTimeLoading() {
        final Loader loader = new YamlLoader(ResourceLoader.getResourceAsStream("tomacs.yml"));
        Assertions.assertTrue(StreamSupport.stream(loader.getDefault().spliterator(), false)
            .map(n -> (ProtelisNode) n)
            .flatMap(n -> n.getReactions().stream()
                    .map(Reaction::getActions)
                    .flatMap(Collection::stream)
                    .filter(a -> a instanceof RunProtelisProgram)
                    .map(a -> (RunProtelisProgram) a)
                    .map(n::getNetworkManager))
            .mapToDouble(AlchemistNetworkManager::getRetentionTime)
            .peek(d -> Assertions.assertTrue(Double.isFinite(d)))
            .count() > 0);
    }

}
