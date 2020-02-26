/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.ns3;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests the ns3 module by using it with the Protelis incarnation.
 */
public class TestWithProtelis {

    /**
     * Tests the integration with ns3 via ns3asy by running a simulation consisting in some nodes
     * executing a Protelis program which makes them send messages to each other. These messages
     * are delivered using ns3.
     */
    @Test
    public void testNs3asySimple() {
        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("linux")) {
            final var env = testInSimulator("ns3asy.yml", 50);
            if (env.getIncarnation().isPresent()) {
                for (final var node : env.getNodes()) {
                    final var received = ((Double) node.getConcentration(env.getIncarnation().get().createMolecule("msgs_received"))).intValue();
                    assertTrue(received > 0);
                    //System.out.println("The node " + node.toString() + " received " + received + " messages");
                }
                //System.out.println("Time is " + env.getSimulation().getTime());
            } else {
                fail("Incarnation not present");
            }
        }
    }

    /**
     * Tests the integration with ns3 via ns3asy by running a simulation consisting in some nodes
     * executing the 'channel' Protelis program.
     */
    @Test
    public void testChannel() {
        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("linux")) {
            testInSimulator("channel.yml", Long.MAX_VALUE);
            //System.out.println("Step is " + env.getSimulation().getStep());
            //System.out.println("Time is " + env.getSimulation().getTime());
        }
    }

    private <T, P extends Position<P>> Environment<T, P> testInSimulator(final String ymlName, final long steps) {
        final InputStream res = ResourceLoader.getResourceAsStream(ymlName);
        final Environment<T, P> env = new YamlLoader(res).getWith(Collections.emptyMap());
        final Simulation<T, P> sim = new Engine<>(env, steps);
        sim.play();
        sim.run();
        sim.getError().ifPresent(Unchecked.consumer(e -> {
            throw e;
        }));
        return env;
    }
}
