package it.unibo.alchemist.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.actions.TargetWalker;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.nodes.GenericNode;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.interfaces.IMapEnvironment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;


/**
 */
public class TestTargetWalker {

    private static final String TESTMAP = "/maps/cesena.pbf";
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
    private static final Position STARTPOSITION = new LatLongPosition(STARTLAT, STARTLON);
    /*
     * Near Montefiore
     */
    private static final Position ENDPOSITION = new LatLongPosition(ENDLAT, ENDLON);
    private IMapEnvironment<Object> env;
    private Node<Object> node;
    private Reaction<Object> reaction;

    /**
     * @throws ClassNotFoundException if test fails
     * @throws IOException if test fails
     */
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings(value = {"DMI_HARDCODED_ABSOLUTE_FILENAME", "SIC_INNER_SHOULD_BE_STATIC_ANON"},
        justification = "It is a resource path, not an absolute pathname.")
    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        try {
            env = new OSMEnvironment<>(TESTMAP, true, true);
            env.setLinkingRule(new NoLinks<>());
            node = new GenericNode<Object>(env) {
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
        final Position start = env.getPosition(node);
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
        final Position start = env.getPosition(node);
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
        final Position start = env.getPosition(node);
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
        final Position start = env.getPosition(node);
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
        final Position start = env.getPosition(node);
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
        final Position start = env.getPosition(node);
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
        final Position start = env.getPosition(node);
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
        final Position start = env.getPosition(node);
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
