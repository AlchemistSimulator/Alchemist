/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import it.unibo.alchemist.boundary.LoadAlchemist;
import it.unibo.alchemist.boundary.OutputMonitor;
import it.unibo.alchemist.core.Engine;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.model.maps.positions.LatLongPosition;
import it.unibo.alchemist.model.times.DoubleTime;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A series of tests checking that our Yaml Loader is working as expected.
 */
@SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
class TestLoadGPSTrace {

    private static final Map<LatLongPosition, LatLongPosition> START_ARRIVE_POSITION = new HashMap<>();
    private static final Map<Node<?>, LatLongPosition> NODE_START_POSITION = new HashMap<>();

    private static final LatLongPosition START_1 = new LatLongPosition(48.176559999999995, 16.36939);
    private static final LatLongPosition ARRIVE_1 = new LatLongPosition(48.176567, 16.369469);

    private static final LatLongPosition START_2 = new LatLongPosition(48.200253, 16.366229);
    private static final LatLongPosition ARRIVE_2 = new LatLongPosition(48.219395, 16.389551);

    private static final LatLongPosition START_3 = new LatLongPosition(48.250569999999996, 16.341894);
    private static final LatLongPosition ARRIVE_3 = new LatLongPosition(48.19978, 16.353043);

    private static final LatLongPosition START_4 = new LatLongPosition(48.20625, 16.364506);
    private static final LatLongPosition ARRIVE_4 = new LatLongPosition(48.206326, 16.364582);

    private static final LatLongPosition START_5 = new LatLongPosition(48.233093, 16.418);
    private static final LatLongPosition ARRIVE_5 = new LatLongPosition(48.207733, 16.36331);
    static {
        START_ARRIVE_POSITION.put(START_1, ARRIVE_1);
        START_ARRIVE_POSITION.put(START_2, ARRIVE_2);
        START_ARRIVE_POSITION.put(START_3, ARRIVE_3);
        START_ARRIVE_POSITION.put(START_4, ARRIVE_4);
        START_ARRIVE_POSITION.put(START_5, ARRIVE_5);
    }
    /*
     * max distance allowed between real and ideal arrive (1cm)
     */
    private static final double DELTA = 1e-2;
    private static final int TIME_TO_REACH = 30550;
    /**
     * Test the ability to inject variables.
     *
     * @param <T> Used for internal consistency
     */
    @Test
    <T> void testLoadGPSTrace() {
        final var res = ResourceLoader.getResource("testgps.yml");
        assertNotNull(res, "Missing test resource " + "testgps.yml");
        final Environment<T, GeoPosition> environment = LoadAlchemist.from(res).<T, GeoPosition>getDefault().getEnvironment();
        assertTrue(environment.getNodeCount() > 0);
        environment.getNodes().forEach(node -> {
            final var reactions = node.getReactions();
            assertFalse(reactions.isEmpty());
            reactions.forEach(reaction -> {
                assertTrue(reaction.getConditions().isEmpty());
                assertEquals(1, reaction.getActions().size());
            });
        });
        final Simulation<T, GeoPosition> sim = new Engine<>(environment, new DoubleTime(TIME_TO_REACH));
        sim.addOutputMonitor(new OutputMonitor<>() {

            @Override
            public void finished(
                @Nonnull final Environment<T, GeoPosition> environment,
                @Nonnull final Time time,
                final long step
            ) {
                for (final Node<T> node : environment.getNodes()) {
                    final GeoPosition start = Objects.requireNonNull(NODE_START_POSITION.get(node));
                    final GeoPosition idealArrive = Objects.requireNonNull(START_ARRIVE_POSITION.get(start));
                    final GeoPosition realArrive = Objects.requireNonNull(environment.getPosition(node));
                    assertEquals(
                            0.0,
                            idealArrive.distanceTo(realArrive),
                            DELTA,
                            "simulation completed at time " + time + " after " + step + " steps.\n"
                                    + "Start at " + start + ", ideal arrive " + idealArrive + ", actual arrive " + realArrive
                    );
                }
            }

            @Override
            public void initialized(@Nonnull final Environment<T, GeoPosition> environment) {
                for (final Node<T> node : environment.getNodes()) {
                    final GeoPosition position = environment.getPosition(node);
                    /*
                     * We don't know the actual type of position, we use LatLongPosition here, so we need to make sure
                     * that types match, or the map won't return what we expect
                     */
                    NODE_START_POSITION.put(node, new LatLongPosition(position.getLatitude(), position.getLongitude()));
                }
            }

            @Override
            public void stepDone(
                @Nonnull final Environment<T, GeoPosition> environment, final Actionable<T> r,
                @Nonnull final Time time,
                final long step
            ) { }
        });
        sim.play();
        sim.run();
        sim.getError().ifPresent(Unchecked.consumer(e ->  {
            throw e;
        }));
    }

}
