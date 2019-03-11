/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import com.google.common.collect.ImmutableMap;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

public class TestNodeCloning<P extends Position<P>> {

    private static final Molecule SOURCEMOL = new SimpleMolecule("source");
    private static final Molecule ENABLEDMOL = new SimpleMolecule("enabled");
    private static final Molecule DATAMOL = new SimpleMolecule("data");
    private static final long SIMULATED_STEPS = 5000;
    private static final long ENABLE_STEP = 50;
    private static final long ENABLE_CHECKS = ENABLE_STEP + 10;
    private Environment<Object, P> env;
    private Simulation<Object, P> sim;

    /***
     * Prepare the simulation.
     */
    @Before
    public void setUp() {
        final String pathYaml = "gradient.yml";
        final YamlLoader loader = new YamlLoader(ResourceLoader.getResourceAsStream(pathYaml));
        env = loader.getWith(Collections.emptyMap());
        sim = new Engine<>(env, SIMULATED_STEPS);
    }

    private void makeNode(final double x, final double y, final boolean enabled, final boolean source) {
        final Node<Object> node1 = env.getNodeByID(0).cloneNode(DoubleTime.ZERO_TIME);
        node1.setConcentration(SOURCEMOL, source);
        node1.setConcentration(ENABLEDMOL, enabled);
        env.addNode(node1, env.makePosition(x, y));
    }

    /***
     * Tests that gradient values are consistent and stable.
     * 
     * @throws Throwable in case of simulation errors
     */
    @Test
    public void test() throws Throwable {
        sim.schedule(()-> {
            final Node<Object> node0 = env.getNodeByID(0);
            node0.setConcentration(SOURCEMOL, false);
            node0.setConcentration(ENABLEDMOL, true);
            // CHECKSTYLE:OFF - positions are copied from a real experiment
            env.moveNodeToPosition(node0, env.makePosition(-30.72191619873047, -9.75));
            makeNode(-34.62321853637695, -6.039149761199951, true, false);
            makeNode(-33.585994720458987, -1.3899999856948853, true, true);
            makeNode(-26.3700008392334, -9.899999618530274, false, false);
            //CHECKSTYLE:ON
        });
        // 2(S) -- 1 -- 0 -- 3
        final Function<Integer, Node<Object>> nid = i -> env.getNodeByID(i);
        final BiFunction<Integer, Integer, Double> dist = (a, b) -> env.getDistanceBetweenNodes(nid.apply(a), nid.apply(b));
        sim.addOutputMonitor(new OutputMonitor<Object, P>() {
            private static final long serialVersionUID = 1L;
            @Override
            public void stepDone(final Environment<Object, P> env, final Reaction<Object> r, final Time time, final long step) {
                final ImmutableMap<Node<Object>, Double> expectations = ImmutableMap.of(
                        nid.apply(2), 0d,
                        nid.apply(1), dist.apply(2, 1),
                        nid.apply(0), dist.apply(2, 1) + dist.apply(1, 0),
                        nid.apply(3), dist.apply(2, 1) + dist.apply(1, 0) + dist.apply(0, 3));
//                System.out.println("step: " + step + ", time: " + time + " --- " + expectations);
//                System.out.println("Just executed: " + r.getClass().getSimpleName() + "@" + r.getNode());
//                for (Node<Object> n: env) {
//                    System.out.println(n + ": " + n.getConcentration(DATAMOL));
//                }
                if (step == ENABLE_STEP) {
                    sim.schedule(() -> env.getNodeByID(3).setConcentration(ENABLEDMOL, true));
                }
                if (step > ENABLE_CHECKS) {
                    expectations.forEach((node, expected) -> assertEquals(expected, node.getConcentration(DATAMOL)));
                }
            }
            @Override
            public void initialized(final Environment<Object, P> env) { }
            @Override
            public void finished(final Environment<Object, P> env, final Time time, final long step) { }
        });
        sim.play();
        sim.run();
        if (sim.getError().isPresent()) {
            throw sim.getError().get();
        }
    }

}
