/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.api.SupportedIncarnations;
import it.unibo.alchemist.model.implementations.actions.TargetMapWalker;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 */
class TestTargetMapWalker {

    private static final Incarnation<Object, GeoPosition> INCARNATION =
            SupportedIncarnations.<Object, GeoPosition>get("protelis").orElseThrow();
    private static final String TESTMAP = "maps/cesena.pbf";
    private static final Molecule TRACK = new SimpleMolecule("track");
    private static final Molecule INTERACTING = new SimpleMolecule("interacting");
    private static final int STEPS = 2000;
    private static final double STARTLAT = 44.135_81;
    private static final double STARTLON = 12.240_3;
    private static final double ENDLAT = 44.143_493;
    private static final double ENDLON = 12.260_879;
    /*
     * Rocca Malatestiana
     */
    private static final GeoPosition STARTPOSITION = new LatLongPosition(STARTLAT, STARTLON);
    /*
     * Near Montefiore
     */
    private static final GeoPosition ENDPOSITION = new LatLongPosition(ENDLAT, ENDLON);
    private MapEnvironment<Object, ?, ?> environment;
    private Node<Object> node;
    private Reaction<Object> reaction;

    /**
     */
    @SuppressFBWarnings(value = {"DMI_HARDCODED_ABSOLUTE_FILENAME", "SIC_INNER_SHOULD_BE_STATIC_ANON"},
        justification = "It is a resource path, not an absolute pathname.")
    @BeforeEach
    public void setUp() {
        try {
            environment = new OSMEnvironment<>(INCARNATION, TESTMAP, true, true);
            environment.setLinkingRule(new NoLinks<>());
            node = INCARNATION.createNode(new MersenneTwister(), environment, null);
            reaction = new Event<>(node, new DiracComb<>(1));
            reaction.setActions(Lists.newArrayList(new TargetMapWalker<>(environment, node, reaction, TRACK, INTERACTING)));
            node.addReaction(reaction);
            environment.addNode(node, STARTPOSITION);
        } catch (IllegalStateException e) {
            e.printStackTrace(); // NOPMD
            fail(e.getMessage());
        }
    }

    private void run() {
        IntStream.range(0, STEPS).forEach(i -> {
            reaction.execute();
            reaction.update(reaction.getTau(), true, environment);
        });
    }

    /**
     * 
     */
    @Test
    void testNoPosition() {
        final GeoPosition start = environment.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.distanceTo(start) < 10);
        run();
        /*
         * Node should not move at all
         */
        assertEquals(start, environment.getPosition(node));
    }

    /**
     * 
     */
    @Test
    void testPosition() {
        final GeoPosition start = environment.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.distanceTo(start) < 10);
        node.setConcentration(TRACK, new LatLongPosition(ENDLAT, ENDLON));
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, environment.getPosition(node));
    }

    /**
     * 
     */
    @Test
    void testIterableDouble() {
        final GeoPosition start = environment.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.distanceTo(start) < 10);
        node.setConcentration(TRACK, Lists.newArrayList(ENDLAT, ENDLON));
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, environment.getPosition(node));
    }

    /**
     * 
     */
    @Test
    void testIterableStrings() {
        final GeoPosition start = environment.getPosition(node);
        assertNotNull(start);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.distanceTo(start) < 10);
        node.setConcentration(TRACK, Lists.newArrayList(Double.toString(ENDLAT), Double.toString(ENDLON)));
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, environment.getPosition(node));
    }

    /**
     * 
     */
    @Test
    void testStrings01() {
        final GeoPosition start = environment.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.distanceTo(start) < 10);
        node.setConcentration(TRACK, Lists.newArrayList(ENDLAT, ENDLON).toString());
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, environment.getPosition(node));
    }

    /**
     * 
     */
    @Test
    void testStrings02() {
        final GeoPosition start = environment.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.distanceTo(start) < 10);
        node.setConcentration(TRACK, ENDPOSITION.toString());
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, environment.getPosition(node));
    }

    /**
     * 
     */
    @Test
    void testStrings03() {
        final GeoPosition start = environment.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.distanceTo(start) < 10);
        node.setConcentration(TRACK, "<" + ENDLAT + " " + ENDLON + ">");
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, environment.getPosition(node));
    }

    /**
     * 
     */
    @Test
    void testStrings04() {
        final GeoPosition start = environment.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.distanceTo(start) < 10);
        node.setConcentration(TRACK, "sakldaskld" + ENDLAT + "fmekfjr" + ENDLON + "sdsad32d");
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, environment.getPosition(node));
    }



}
