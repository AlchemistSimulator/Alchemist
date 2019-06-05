/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.actions.TargetWalker;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.nodes.AbstractNode;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;


/**
 */
public class TestTargetWalker {

    private static final String TESTMAP = "maps/cesena.pbf";
    private static final Molecule TRACK = new SimpleMolecule("track");
    private static final Molecule INTERACTING = new SimpleMolecule("interacting");
    private static final int STEPS = 2000;
    private static final double STARTLAT = 44.13581;
    private static final double STARTLON = 12.2403;
    private static final double ENDLAT = 44.143493;
    private static final double ENDLON = 12.260879;
    /*
     * Rocca Malatestiana
     */
    private static final GeoPosition STARTPOSITION = new LatLongPosition(STARTLAT, STARTLON);
    /*
     * Near Montefiore
     */
    private static final GeoPosition ENDPOSITION = new LatLongPosition(ENDLAT, ENDLON);
    private MapEnvironment<Object> env;
    private Node<Object> node;
    private Reaction<Object> reaction;

    /**
     * @throws ClassNotFoundException if test fails
     * @throws IOException if test fails
     */
    @SuppressFBWarnings(value = {"DMI_HARDCODED_ABSOLUTE_FILENAME", "SIC_INNER_SHOULD_BE_STATIC_ANON"},
        justification = "It is a resource path, not an absolute pathname.")
    @BeforeEach
    public void setUp() throws ClassNotFoundException, IOException {
        try {
            env = new OSMEnvironment<>(TESTMAP, true, true);
            env.setLinkingRule(new NoLinks<>());
            node = new AbstractNode<Object>(env) {
                private static final long serialVersionUID = -3982001064673078159L;
                @Override
                protected Object createT() {
                    return null;
                }
            };
            reaction = new Event<Object>(node, new DiracComb<>(1));
            reaction.setActions(Lists.newArrayList(new TargetWalker<Object>(env, node, reaction, TRACK, INTERACTING)));
            node.addReaction(reaction);
            env.addNode(node, STARTPOSITION);
        } catch (IllegalStateException e) {
            e.printStackTrace(); // NOPMD
            fail(e.getMessage());
        }
    }

    private void run() {
        IntStream.range(0, STEPS).forEach(i -> {
            reaction.execute();
            reaction.update(reaction.getTau(), true, env);
        });
    }

    /**
     * 
     */
    @Test
    public void testNoPosition() {
        final GeoPosition start = env.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.getDistanceTo(start) < 10);
        run();
        /*
         * Node should not move at all
         */
        assertEquals(start, env.getPosition(node));
    }

    /**
     * 
     */
    @Test
    public void testPosition() {
        final GeoPosition start = env.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.getDistanceTo(start) < 10);
        node.setConcentration(TRACK, new LatLongPosition(ENDLAT, ENDLON));
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, env.getPosition(node));
    }

    /**
     * 
     */
    @Test
    public void testIterableDouble() {
        final GeoPosition start = env.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.getDistanceTo(start) < 10);
        node.setConcentration(TRACK, Lists.newArrayList(ENDLAT, ENDLON));
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, env.getPosition(node));
    }

    /**
     * 
     */
    @Test
    public void testIterableStrings() {
        final GeoPosition start = env.getPosition(node);
        assertNotNull(start);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.getDistanceTo(start) < 10);
        node.setConcentration(TRACK, Lists.newArrayList(Double.toString(ENDLAT), Double.toString(ENDLON)));
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, env.getPosition(node));
    }

    /**
     * 
     */
    @Test
    public void testStrings01() {
        final GeoPosition start = env.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.getDistanceTo(start) < 10);
        node.setConcentration(TRACK, Lists.newArrayList(ENDLAT, ENDLON).toString());
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, env.getPosition(node));
    }

    /**
     * 
     */
    @Test
    public void testStrings02() {
        final GeoPosition start = env.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.getDistanceTo(start) < 10);
        node.setConcentration(TRACK, ENDPOSITION.toString());
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, env.getPosition(node));
    }

    /**
     * 
     */
    @Test
    public void testStrings03() {
        final GeoPosition start = env.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.getDistanceTo(start) < 10);
        node.setConcentration(TRACK, "<" + ENDLAT + " " + ENDLON + ">");
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, env.getPosition(node));
    }

    /**
     * 
     */
    @Test
    public void testStrings04() {
        final GeoPosition start = env.getPosition(node);
        /*
         * Should not be more than 10 meters afar the suggested start
         */
        assertTrue(STARTPOSITION.getDistanceTo(start) < 10);
        node.setConcentration(TRACK, "sakldaskld" + ENDLAT + "fmekfjr" + ENDLON + "sdsad32d");
        run();
        /*
         * Node should get to the final position
         */
        assertEquals(ENDPOSITION, env.getPosition(node));
    }



}
