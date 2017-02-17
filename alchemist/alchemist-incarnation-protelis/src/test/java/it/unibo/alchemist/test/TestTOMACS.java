package it.unibo.alchemist.test;

import java.util.Collection;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Test;

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
        final Loader loader = new YamlLoader(TestTOMACS.class.getResourceAsStream("/tomacs.yml"));
        Assert.assertTrue(StreamSupport.stream(loader.getDefault().spliterator(), false)
            .map(n -> (ProtelisNode) n)
            .flatMap(n -> n.getReactions().stream()
                    .map(Reaction::getActions)
                    .flatMap(Collection::stream)
                    .filter(a -> a instanceof RunProtelisProgram)
                    .map(a -> (RunProtelisProgram) a)
                    .map(n::getNetworkManager))
            .mapToDouble(AlchemistNetworkManager::getRetentionTime)
            .peek(d -> Assert.assertTrue(Double.isFinite(d)))
            .count() > 0);
    }

}
