/**
 * 
 */
package it.unibo.alchemist.protelis;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.lang.HashUtils;
import org.protelis.lang.datatype.DatatypeFactory;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Field;
import org.protelis.lang.datatype.Tuple;
import org.protelis.vm.ExecutionEnvironment;
import org.protelis.vm.LocalizedDevice;
import org.protelis.vm.SpatiallyEmbeddedDevice;
import org.protelis.vm.TimeAwareDevice;
import org.protelis.vm.impl.AbstractExecutionContext;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import java8.util.function.Function;
import java8.util.function.Functions;

/**
 */
public class AlchemistExecutionContext extends AbstractExecutionContext implements SpatiallyEmbeddedDevice, LocalizedDevice, TimeAwareDevice {

    /**
     * Put this {@link Molecule} inside nodes that should compute distances using routes. It only makes sense in case the environment is a {@link MapEnvironment}
     */
    public static final Molecule USE_ROUTES_AS_DISTANCES = new SimpleMolecule("ROUTES_AS_DISTANCE");
    private final LoadingCache<Position, Double> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<Position, Double>() {
                @Override
                public Double load(final Position dest) {
                    if (env instanceof MapEnvironment<?>) {
                        return ((MapEnvironment<Object>) env).computeRoute(node, dest).getDistance();
                    }
                    return getDevicePosition().getDistanceTo(dest);
                }
            });
    private final Environment<Object> env;
    private int hash;
    private final ProtelisNode node;
    private final RandomGenerator rand;
    private final Reaction<Object> react;

    /**
     * @param environment
     *            the simulation {@link Environment}
     * @param localNode
     *            the local {@link ProtelisNode}
     * @param reaction
     *            the {@link Reaction} hosting the program
     * @param random
     *            the {@link RandomGenerator} for this simulation
     * @param netmgr
     *            the {@link AlchemistNetworkManager} to be used
     */
    public AlchemistExecutionContext(final Environment<Object> environment, final ProtelisNode localNode,
            final Reaction<Object> reaction, final RandomGenerator random, final AlchemistNetworkManager netmgr) {
        super(localNode, netmgr);
        env = environment;
        node = localNode;
        react = reaction;
        rand = random;
    }

    private Field buildFieldWithPosition(final Function<Position, ?> fun) {
        return buildField(fun, getDevicePosition());
    }

    /**
     * Computes the distance between two nodes, through
     * {@link Environment#getDistanceBetweenNodes(Node, Node)}.
     * 
     * @param target
     *            the target device
     * @return the distance
     */
    public double distanceTo(final DeviceUID target) {
        assert target instanceof ProtelisNode;
        return env.getDistanceBetweenNodes(node, (ProtelisNode) target);
    }

    /**
     * Computes the distance between two nodes, through
     * {@link Environment#getDistanceBetweenNodes(Node, Node)}.
     * 
     * @param target
     *            the target device
     * @return the distance
     */
    public double distanceTo(final int target) {
        return distanceTo((ProtelisNode) env.getNodeByID(target));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AlchemistExecutionContext) {
            final AlchemistExecutionContext ctx = (AlchemistExecutionContext) obj;
            return node.equals(ctx.node) && env.equals(ctx.env) && react.equals(ctx.react) && rand.equals(ctx.rand);
        }
        return false;
    }

    @Override
    public Tuple getCoordinates() {
        return DatatypeFactory.createTuple(getDevicePosition().getCartesianCoordinates());
    }

    @Override
    public Number getCurrentTime() {
        return react.getTau().toDouble();
    }

    /**
     * @return the device position, in form of {@link Position}
     */
    public Position getDevicePosition() {
        return env.getPosition(node);
    }

    @Override
    public DeviceUID getDeviceUID() {
        return node;
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment() {
        return node;
    }

    /**
     * @return experimental access to the simulated environment, for building oracles
     */
    public Environment<Object> getEnvironmentAccess() {
        return new Environment<Object>() {
            private static final long serialVersionUID = 1L;
            private <X> X noAccess() {
                throw new IllegalStateException("This method is not accessible to prevent disruptive modifications to the simulation flow");
            }
            @Override
            public Iterator<Node<Object>> iterator() {
                return noAccess();
            }
            @Override
            public void addLayer(final Molecule m, final Layer<Object> l) {
                noAccess();
            }
            @Override
            public void addNode(final Node<Object> node, final Position p) {
                noAccess();
            }
            @Override
            public int getDimensions() {
                return env.getDimensions();
            }
            @Override
            public double getDistanceBetweenNodes(final Node<Object> n1, final Node<Object> n2) {
                return env.getDistanceBetweenNodes(n1, n2);
            }
            @Override
            public Optional<Layer<Object>> getLayer(final Molecule m) {
                return env.getLayer(m);
            }
            @Override
            public Set<Layer<Object>> getLayers() {
                return env.getLayers();
            }

            @Override
            public LinkingRule<Object> getLinkingRule() {
                return env.getLinkingRule();
            }
            @Override
            public Neighborhood<Object> getNeighborhood(final Node<Object> center) {
                return env.getNeighborhood(center);
            }
            @Override
            public Node<Object> getNodeByID(final int id) {
                return env.getNodeByID(id);
            }
            @Override
            public Collection<Node<Object>> getNodes() {
                return env.getNodes();
            }
            @Override
            public int getNodesNumber() {
                return env.getNodesNumber();
            }
            @Override
            public Set<Node<Object>> getNodesWithinRange(final Node<Object> center, final double range) {
                return env.getNodesWithinRange(center, range);
            }
            @Override
            public Set<Node<Object>> getNodesWithinRange(final Position center, final double range) {
                return env.getNodesWithinRange(center, range);
            }
            @Override
            public double[] getOffset() {
                return env.getOffset();
            }
            @Override
            public Position getPosition(final Node<Object> node) {
                return env.getPosition(node);
            }
            @Override
            public String getPreferredMonitor() {
                return env.getPreferredMonitor();
            }
            @Override
            public Simulation<Object> getSimulation() {
                return noAccess();
            }
            @Override
            public double[] getSize() {
                return env.getSize();
            }
            @Override
            public double[] getSizeInDistanceUnits() {
                return env.getSizeInDistanceUnits();
            }
            @Override
            public Position makePosition(final Number... coordinates) {
                return env.makePosition(coordinates);
            }
            @Override
            public void moveNode(final Node<Object> node, final Position direction) {
                noAccess();
            }
            @Override
            public void moveNodeToPosition(final Node<Object> node, final Position position) {
                noAccess();
            }
            @Override
            public void removeNode(final Node<Object> node) {
                noAccess();
            }
            @Override
            public void setLinkingRule(final LinkingRule<Object> rule) {
                noAccess();
            }
            @Override
            public void setSimulation(final Simulation<Object> s) {
                noAccess();
            }
        };
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = HashUtils.hash32(node, env, react);
        }
        return hash;
    }

    @Override
    protected AbstractExecutionContext instance() {
        return new AlchemistExecutionContext(env, node, react, rand, (AlchemistNetworkManager) getNetworkManager());
    }

    /**
     * @return The same behavior of MIT Proto's nbrdelay (forward view).
     */
    public Field nbrDelay() {
        return buildField(Functions.identity(), getDeltaTime());
    }

    @Override
    public Field nbrLag() {
        return buildField(time -> getCurrentTime().doubleValue() - time, getCurrentTime().doubleValue());
    }

    @Override
    public Field nbrRange() {
        return buildFieldWithPosition(p -> {
            if (env instanceof MapEnvironment<?> && node.contains(USE_ROUTES_AS_DISTANCES)) {
                return routingDistance(p);
            }
            return getDevicePosition().getDistanceTo(p);
        });
    }

    @Override
    public Field nbrVector() {
        return buildFieldWithPosition(p -> getDevicePosition().subtract(p));
    }

    @Override
    public double nextRandomDouble() {
        return rand.nextDouble();
    }

    /**
     * Computes the distance along a map. Requires a {@link MapEnvironment}.
     * 
     * @param dest
     *            the destination, in form of a destination node
     * @return the distance on a map
     */
    public double routingDistance(final Node<Object> dest) {
        return routingDistance(env.getPosition(dest));
    }

    /**
     * Computes the distance along a map. Requires a {@link MapEnvironment}.
     * 
     * @param dest
     *            the destination, in form of {@link ProtelisNode} ID. Non
     *            integer numbers will be cast to integers by
     *            {@link Number#intValue()}.
     * @return the distance on a map
     */
    public double routingDistance(final Number dest) {
        return routingDistance(env.getNodeByID(dest.intValue()));
    }

    /**
     * Computes the distance along a map. Requires a {@link MapEnvironment}.
     * 
     * @param dest
     *            the destination
     * @return the distance on a map
     */
    public double routingDistance(final Position dest) {
        try {
            return cache.get(dest);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Computes the distance along a map. Requires a {@link MapEnvironment}.
     * 
     * @param dest
     *            the destination, as a {@link Tuple} of two values: [latitude,
     *            longitude]
     * @return the distance on a map
     * @throws ExecutionException 
     */
    public double routingDistance(final Tuple dest) throws ExecutionException {
        if (dest.size() == 2) {
            return routingDistance(new LatLongPosition((Number) dest.get(0), (Number) dest.get(1)));
        }
        throw new IllegalArgumentException(dest + " is not a coordinate I can understand.");
    }

}
