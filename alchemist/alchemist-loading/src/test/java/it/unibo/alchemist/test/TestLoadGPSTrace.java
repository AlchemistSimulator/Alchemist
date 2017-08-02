package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jooq.lambda.Unchecked;
import org.junit.Test;

import com.google.common.collect.Maps;

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

    private final static Map<GeoPosition, GeoPosition> START_ARRIVE_POSITION = new HashMap<>();
    private final static Map<Node<?>, GeoPosition> NODE_START_POSITION = new HashMap<>();
    
    private final static GeoPosition START_1 = new LatLongPosition(48.176559999999995, 16.36939);
    private final static GeoPosition ARRIVE_1 = new LatLongPosition(48.176567, 16.369469);
    
    private final static GeoPosition START_2 = new LatLongPosition(48.200253, 16.366229);
    private final static GeoPosition ARRIVE_2 = new LatLongPosition(48.219395, 16.389551);
    
    private final static GeoPosition START_3 = new LatLongPosition(48.250569999999996, 16.341894);
    private final static GeoPosition ARRIVE_3 = new LatLongPosition(48.19978, 16.353043);
    
    private final static GeoPosition START_4 = new LatLongPosition(48.20625, 16.364506);
    private final static GeoPosition ARRIVE_4 = new LatLongPosition(48.206326, 16.364582);
    
    private final static GeoPosition START_5 = new LatLongPosition(48.233093, 16.418);
    private final static GeoPosition ARRIVE_5 = new LatLongPosition(48.207733, 16.36331);
    static {
        START_ARRIVE_POSITION.put(START_1, ARRIVE_1);
        START_ARRIVE_POSITION.put(START_2, ARRIVE_2);
        START_ARRIVE_POSITION.put(START_3, ARRIVE_3);
        START_ARRIVE_POSITION.put(START_4, ARRIVE_4);
        START_ARRIVE_POSITION.put(START_5, ARRIVE_5);
    }
    /**
     * Test the ability to inject variables.
     */
    @Test
    public void testLoadWIthVariable() {
        final Map<String, Double> map = Maps.newLinkedHashMap();
        map.put("random", 1d);
        testLoading("/testgps.yml", map);
    }

    @SuppressWarnings("serial")
    private static <T> void testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = TestLoadGPSTrace.class.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<T> env = new YamlLoader(res).getWith(vars);
        final Simulation<T> sim = new Engine<>(env, new DoubleTime(30550));
        sim.addOutputMonitor(new OutputMonitor<T>() {

            @Override
            public void finished(Environment<T> env, Time time, long step) {
                for (Node<T> node : env.getNodes()) {
                    GeoPosition start = Objects.requireNonNull(NODE_START_POSITION.get(node));
                    GeoPosition idealArrive = Objects.requireNonNull(START_ARRIVE_POSITION.get(start));
                    Position realArrive = Objects.requireNonNull(env.getPosition(node));
                    assertEquals(0.0, idealArrive.getDistanceTo(realArrive), 0.1);
                }
            }

            @Override
            public void initialized(Environment<T> env) {
                for (Node<T> node : env.getNodes()) {
                    Position p = env.getPosition(node);
                    NODE_START_POSITION.put(node, new LatLongPosition(p.getCoordinate(1), p.getCoordinate(0)));
                }
            }

            @Override
            public void stepDone(Environment<T> env, Reaction<T> r, Time time, long step) {
                
            }
        });
        sim.play();
        sim.run();
        sim.getError().ifPresent(Unchecked.consumer(e ->  {
            throw e;
        }));
    }
}
