/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.interfaces.properties.CellProperty;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 */
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
class TestJunction {

    private Node<Double> node1;
    private Node<Double> node2;
    private Node<Double> node3;

    private CellProperty<Euclidean2DPosition> getCell(final Node<Double> node) {
        return node.asProperty(CellProperty.class);
    }

    /**
     */
    @BeforeEach
    public void setUp() {
        final var incarnation = new BiochemistryIncarnation();
        final Environment<Double, Euclidean2DPosition> environment = new BioRect2DEnvironment(incarnation);
        node1 = incarnation.createNode(new MersenneTwister(), environment, null);
        node2 = incarnation.createNode(new MersenneTwister(), environment, null);
        node3 = incarnation.createNode(new MersenneTwister(), environment, null);
    }

    /**
     * Various test cases for junctions management.
     */
    @Test
    void test() {
        final Map<Biomolecule, Double> map1 = new HashMap<>(1);
        final Map<Biomolecule, Double> map2 = new HashMap<>(1);
        map1.put(new Biomolecule("A"), 1d);
        map1.put(new Biomolecule("B"), 1d);
        final Junction jBase = new Junction("A-B", map1, map2);
        final Junction j1 = new Junction(jBase);
        getCell(node1).addJunction(j1, node2);
        assertTrue(getCell(node1).containsJunction(j1));
        assertTrue(getCell(node1).containsJunction(jBase)); // same name here
        assertFalse(getCell(node2).containsJunction(j1)); // this is just for this test, normally node2 contain j1
        assertFalse(getCell(node3).containsJunction(j1));

        assertEquals(getCell(node1).getJunctionsCount(), 1);
        assertEquals(getCell(node2).getJunctionsCount(), 0);
        assertEquals(getCell(node3).getJunctionsCount(), 0);

        final Junction j2 = new Junction(jBase);
        getCell(node1).addJunction(j2, node3);
        assertTrue(getCell(node1).containsJunction(j1));
        assertTrue(getCell(node1).containsJunction(j2)); // same name here
        assertFalse(getCell(node2).containsJunction(j2));
        assertFalse(getCell(node3).containsJunction(j2)); // this is just for this test, normally node3 contains j2

        assertEquals(getCell(node1).getJunctionsCount(), 2);
        assertEquals(getCell(node2).getJunctionsCount(), 0);
        final CellProperty<Euclidean2DPosition> b = getCell(node3);
        assertEquals(b.getJunctionsCount(), 0);
        //CHECKSTYLE:OFF magicnumber
        final int totJ = 123;
        //CHECKSTYLE:ON magicnumber
        for (int i = 0; i < totJ; i++) { // add many identical junction to node 2
            final Junction jtmp = new Junction(jBase);
            getCell(node2).addJunction(jtmp, node3);
        }
        /* Situation Summary: 
         * node1: 1 junction A-B with node2, 1 junction A-B with node3
         * node2: totJ junction A-B with node3
         * node3: nothing
         */
        assertEquals(getCell(node1).getJunctionsCount(), 2);
        assertEquals(getCell(node2).getJunctionsCount(), totJ);
        assertEquals(getCell(node3).getJunctionsCount(), 0);
        /* **** Remove junctions **** */
        // TODO ? note that molecule in the junction is not placed in cell after destruction. It is not implemented yet.
        getCell(node1).removeJunction(jBase, node2); // remove a junction of the type A-B which has node2 as neighbor
        assertEquals(getCell(node1).getJunctionsCount(), 1);
        assertEquals(getCell(node2).getJunctionsCount(), totJ);
        assertEquals(getCell(node3).getJunctionsCount(), 0);
        getCell(node1).removeJunction(jBase, node2); // do nothing, because node1 hasn't any junction with node2 now
        assertEquals(getCell(node1).getJunctionsCount(), 1);
        assertEquals(getCell(node2).getJunctionsCount(), totJ);
        assertEquals(getCell(node3).getJunctionsCount(), 0);
        getCell(node1).removeJunction(jBase, node3); // remove the last junction of node1
        assertEquals(getCell(node1).getJunctionsCount(), 0);
        assertEquals(getCell(node2).getJunctionsCount(), totJ);
        assertEquals(getCell(node3).getJunctionsCount(), 0);

        final Map<Biomolecule, Double> mapD1 = new HashMap<>(1);
        final Map<Biomolecule, Double> mapD2 = new HashMap<>(1);
        map1.put(new Biomolecule("C"), 1d);
        map1.put(new Biomolecule("D"), 1d);
        // a new junction that is not present in any node
        final Junction jDiff = new Junction("C-D", mapD1, mapD2);

        getCell(node2).removeJunction(jDiff, node3); // do nothing because node2 hasn't a junction C-D
        assertEquals(getCell(node2).getJunctionsCount(), totJ);
    }

}
