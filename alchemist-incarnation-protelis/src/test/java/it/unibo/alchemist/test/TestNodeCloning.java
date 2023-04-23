/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.OutputMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.loader.LoadAlchemist;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.model.molecules.SimpleMolecule;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Time;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests node cloning.
 *
 * @param <P> position type
 */
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
@SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
class TestNodeCloning<P extends Position<P>> {

    private static final Molecule SOURCEMOL = new SimpleMolecule("source");
    private static final Molecule ENABLEDMOL = new SimpleMolecule("enabled");
    private static final Molecule DATAMOL = new SimpleMolecule("data");
    private static final long SIMULATED_STEPS = 5000;
    private static final long ENABLE_STEP = 50;
    private static final long ENABLE_CHECKS = ENABLE_STEP + 10;
    private Environment<Object, P> environment;
    private Simulation<Object, P> simulation;

    /***
     * Prepare the simulation.
     */
    @BeforeEach
    public void setUp() {
        final String pathYaml = "gradient.yml";
        final Loader loader = LoadAlchemist.from(ResourceLoader.getResource(pathYaml));
        environment = loader.<Object, P>getWith(Collections.emptyMap()).getEnvironment();
        simulation = new Engine<>(environment, SIMULATED_STEPS);
    }

    private void makeNode(final double x, final double y, final boolean enabled, final boolean source) {
        final Node<Object> node1 = environment.getNodeByID(0).cloneNode(Time.ZERO);
        node1.setConcentration(SOURCEMOL, source);
        node1.setConcentration(ENABLEDMOL, enabled);
        environment.addNode(node1, environment.makePosition(x, y));
    }

    /***
     * Tests that gradient values are consistent and stable.
     * 
     * @throws Throwable in case of simulation errors
     */
    @Test
    void test() throws Throwable {
        // CHECKSTYLE: MagicNumber OFF - values are taken from a real experiment causing the bug.
        simulation.schedule(() -> {
            final Node<Object> node0 = environment.getNodeByID(0);
            node0.setConcentration(SOURCEMOL, false);
            node0.setConcentration(ENABLEDMOL, true);
            // CHECKSTYLE:OFF - positions are copied from a real experiment
            environment.moveNodeToPosition(node0, environment.makePosition(-30.72191619873047, -9.75));
            makeNode(-34.62321853637695, -6.039149761199951, true, false);
            makeNode(-33.585994720458987, -1.3899999856948853, true, true);
            makeNode(-26.3700008392334, -9.899999618530274, false, false);
            //CHECKSTYLE:ON
        });
        // 2(S) -- 1 -- 0 -- 3
        final Function<Integer, Node<Object>> nid = i -> environment.getNodeByID(i);
        final BiFunction<Integer, Integer, Double> distance =
                (a, b) -> environment.getDistanceBetweenNodes(nid.apply(a), nid.apply(b));
        simulation.addOutputMonitor(new OutputMonitor<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public void stepDone(
                    @Nonnull final Environment<Object, P> environment,
                    final Actionable<Object> reaction,
                    @Nonnull final Time time,
                    final long step
            ) {
                final ImmutableMap<Node<Object>, Double> expectations = ImmutableMap.of(
                        nid.apply(2), 0d,
                        nid.apply(1), distance.apply(2, 1),
                        nid.apply(0), distance.apply(2, 1) + distance.apply(1, 0),
                        nid.apply(3), distance.apply(2, 1) + distance.apply(1, 0) + distance.apply(0, 3));
//                System.out.println("step: " + step + ", time: " + time + " --- " + expectations);
//                System.out.println("Just executed: " + r.getClass().getSimpleName() + "@" + r.getNode());
//                for (Node<Object> n: env) {
//                    System.out.println(n + ": " + n.getConcentration(DATAMOL));
//                }
                if (step == ENABLE_STEP) {
                    simulation.schedule(() -> environment.getNodeByID(3).setConcentration(ENABLEDMOL, true));
                }
                if (step > ENABLE_CHECKS) {
                    expectations.forEach((node, expected) -> assertEquals(expected, node.getConcentration(DATAMOL)));
                }
            }
        });
        simulation.play();
        simulation.run();
        if (simulation.getError().isPresent()) {
            throw simulation.getError().get();
        }
    }

}
