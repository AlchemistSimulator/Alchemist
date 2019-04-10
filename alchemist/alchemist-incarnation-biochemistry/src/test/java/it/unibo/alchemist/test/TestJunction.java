/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.CellNode;

/**
 */
public class TestJunction {

    private CellNode<Euclidean2DPosition> node1;
    private CellNode<Euclidean2DPosition> node2;
    private CellNode<Euclidean2DPosition> node3;

    /**
     */
    @Before
    public void setUp() {
        final Environment<Double, Euclidean2DPosition> env = new BioRect2DEnvironment();
        node1 = new CellNodeImpl<>(env);
        node2 = new CellNodeImpl<>(env);
        node3 = new CellNodeImpl<>(env);
    }

    /**
     * Various test cases for junctions management.
     */
    @Test
    public void test() {
        final Map<Biomolecule, Double> map1 = new HashMap<>(1);
        final Map<Biomolecule, Double> map2 = new HashMap<>(1);
        map1.put(new Biomolecule("A"), 1d);
        map1.put(new Biomolecule("B"), 1d);
        final Junction jBase = new Junction("A-B", map1, map2);
        final Junction j1 = new Junction(jBase);
        node1.addJunction(j1, node2);
        assertTrue(node1.containsJunction(j1));
        assertTrue(node1.containsJunction(jBase)); // same name here
        assertFalse(node2.containsJunction(j1)); // this is just for this test, normally node2 contain j1
        assertFalse(node3.containsJunction(j1));

        assertEquals(node1.getJunctionsCount(), 1);
        assertEquals(node2.getJunctionsCount(), 0);
        assertEquals(node3.getJunctionsCount(), 0);

        final Junction j2 = new Junction(jBase);
        node1.addJunction(j2, node3);
        assertTrue(node1.containsJunction(j1));
        assertTrue(node1.containsJunction(j2)); // same name here
        assertFalse(node2.containsJunction(j2));
        assertFalse(node3.containsJunction(j2)); // this is just for this test, normally node3 contains j2

        assertEquals(node1.getJunctionsCount(), 2);
        assertEquals(node2.getJunctionsCount(), 0);
        assertEquals(node3.getJunctionsCount(), 0);
        //CHECKSTYLE:OFF magicnumber
        final int totJ = 123;
        //CHECKSTYLE:ON magicnumber
        for (int i = 0; i < totJ; i++) { // add many identical junction to node 2
            final Junction jtmp = new Junction(jBase);
            node2.addJunction(jtmp, node3);
        }
        /* Situation Summary: 
         * node1: 1 junction A-B with node2, 1 junction A-B with node3
         * node2: totJ junction A-B with node3
         * node3: nothing
         */
        assertEquals(node1.getJunctionsCount(), 2);
        assertEquals(node2.getJunctionsCount(), totJ);
        assertEquals(node3.getJunctionsCount(), 0);
        /* **** Remove junctions **** */
        // TODO ? note that molecule in the junction is not placed in cell after destruction. It is not implemented yet.
        node1.removeJunction(jBase, node2); // remove a junction of the type A-B which has node2 as neighbor
        assertEquals(node1.getJunctionsCount(), 1);
        assertEquals(node2.getJunctionsCount(), totJ);
        assertEquals(node3.getJunctionsCount(), 0);
        node1.removeJunction(jBase, node2); // do nothing, because node1 hasn't any junction with node2 now
        assertEquals(node1.getJunctionsCount(), 1);
        assertEquals(node2.getJunctionsCount(), totJ);
        assertEquals(node3.getJunctionsCount(), 0);
        node1.removeJunction(jBase, node3); // remove the last junction of node1
        assertEquals(node1.getJunctionsCount(), 0);
        assertEquals(node2.getJunctionsCount(), totJ);
        assertEquals(node3.getJunctionsCount(), 0);

        final Map<Biomolecule, Double> mapD1 = new HashMap<>(1);
        final Map<Biomolecule, Double> mapD2 = new HashMap<>(1);
        map1.put(new Biomolecule("C"), 1d);
        map1.put(new Biomolecule("D"), 1d);
        final Junction jDiff = new Junction("C-D", mapD1, mapD2); // a new junction that is not present in any node

        node2.removeJunction(jDiff, node3); // do nothing because node2 hasn't a junction C-D
        assertEquals(node2.getJunctionsCount(), totJ);
    }

}
