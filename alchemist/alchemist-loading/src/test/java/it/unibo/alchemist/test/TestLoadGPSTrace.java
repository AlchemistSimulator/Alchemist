/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jooq.lambda.Unchecked;
import org.junit.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * A series of tests checking that our Yaml Loader is working as expected.
 */
public class TestLoadGPSTrace {

    private static final Map<GeoPosition, GeoPosition> START_ARRIVE_POSITION = new HashMap<>();
    private static final Map<Node<?>, GeoPosition> NODE_START_POSITION = new HashMap<>();

    private static final GeoPosition START_1 = new LatLongPosition(48.176559999999995, 16.36939);
    private static final GeoPosition ARRIVE_1 = new LatLongPosition(48.176567, 16.369469);

    private static final GeoPosition START_2 = new LatLongPosition(48.200253, 16.366229);
    private static final GeoPosition ARRIVE_2 = new LatLongPosition(48.219395, 16.389551);

    private static final GeoPosition START_3 = new LatLongPosition(48.250569999999996, 16.341894);
    private static final GeoPosition ARRIVE_3 = new LatLongPosition(48.19978, 16.353043);

    private static final GeoPosition START_4 = new LatLongPosition(48.20625, 16.364506);
    private static final GeoPosition ARRIVE_4 = new LatLongPosition(48.206326, 16.364582);

    private static final GeoPosition START_5 = new LatLongPosition(48.233093, 16.418);
    private static final GeoPosition ARRIVE_5 = new LatLongPosition(48.207733, 16.36331);
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
     */
    @Test
    public void testLoadGPSTrace() {
        testLoading("testgps.yml");
    }

    @SuppressWarnings("serial")
    private static <T> void testLoading(final String resource) {
        final InputStream res = ResourceLoader.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<T, GeoPosition> env = new YamlLoader(res).getDefault();
        final Simulation<T, GeoPosition> sim = new Engine<>(env, new DoubleTime(TIME_TO_REACH));
        sim.addOutputMonitor(new OutputMonitor<T, GeoPosition>() {

            @Override
            public void finished(final Environment<T, GeoPosition> env, final Time time, final long step) {
                for (final Node<T> node : env.getNodes()) {
                    final GeoPosition start = Objects.requireNonNull(NODE_START_POSITION.get(node));
                    final GeoPosition idealArrive = Objects.requireNonNull(START_ARRIVE_POSITION.get(start));
                    final GeoPosition realArrive = Objects.requireNonNull(env.getPosition(node));
                    assertEquals(0.0, idealArrive.getDistanceTo(realArrive), DELTA);
                }
            }

            @Override
            public void initialized(final Environment<T, GeoPosition> env) {
                for (final Node<T> node : env.getNodes()) {
                    final Position<?> p = env.getPosition(node);
                    NODE_START_POSITION.put(node, new LatLongPosition(p.getCoordinate(1), p.getCoordinate(0)));
                }
            }

            @Override
            public void stepDone(final Environment<T, GeoPosition> env, final Reaction<T> r, final Time time, final long step) {
            }
        });
        sim.play();
        sim.run();
        sim.getError().ifPresent(Unchecked.consumer(e ->  {
            throw e;
        }));
    }
}
