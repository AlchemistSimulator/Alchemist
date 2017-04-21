/**
 * 
 */
package it.unibo.alchemist.protelis;

import java.util.Optional;
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

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Molecule;
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
    /**
     * Put this {@link Molecule} inside nodes that should compute distances using routes approximating them. It only makes sense in case the environment is a {@link MapEnvironment}
     */
    public static final Molecule APPROXIMATE_NBR_RANGE = new SimpleMolecule("APPROXIMATE_NBR_RANGE");
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
    private final ProtelisNode node;
    private final Environment<Object> env;
    private final Reaction<Object> react;
    private final RandomGenerator rand;
    private int hash;
    private double nbrRangeTimeout;
    private Optional<Double> precalcdRoutingDistance = Optional.empty();

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

    @Override
    public DeviceUID getDeviceUID() {
        return node;
    }

    @Override
    public Number getCurrentTime() {
        return react.getTau().toDouble();
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
     * @return the device position, in form of {@link Position}
     */
    public Position getDevicePosition() {
        return env.getPosition(node);
    }

    @Override
    public double nextRandomDouble() {
        return rand.nextDouble();
    }

    @Override
    protected AbstractExecutionContext instance() {
        return new AlchemistExecutionContext(env, node, react, rand, (AlchemistNetworkManager) getNetworkManager());
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
    public int hashCode() {
        if (hash == 0) {
            hash = HashUtils.hash32(node, env, react);
        }
        return hash;
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment() {
        return node;
    }

    @Override
    public Field nbrLag() {
        return buildField(time -> getCurrentTime().doubleValue() - time, getCurrentTime().doubleValue());
    }

    /**
     * @return The same behavior of MIT Proto's nbrdelay (forward view).
     */
    public Field nbrDelay() {
        return buildField(Functions.identity(), getDeltaTime());
    }

    @Override
    public Tuple getCoordinates() {
        return DatatypeFactory.createTuple(getDevicePosition().getCartesianCoordinates());
    }

    @Override
    public Field nbrVector() {
        return buildFieldWithPosition(p -> getDevicePosition().subtract(p));
    }

    @Override
    public Field nbrRange() {
        return buildFieldWithPosition(p -> {
            if (env instanceof MapEnvironment<?> && node.contains(USE_ROUTES_AS_DISTANCES)) {
                if (node.contains(APPROXIMATE_NBR_RANGE)) {
                    try {
                        final double tolerance = (double) node.getConcentration(APPROXIMATE_NBR_RANGE);
                        final double currTime = env.getSimulation().getTime().toDouble();
                        if (currTime > nbrRangeTimeout) {
                            nbrRangeTimeout = currTime + tolerance;
                            precalcdRoutingDistance = Optional.of(routingDistance(p));
                        }
                        return precalcdRoutingDistance.orElse(routingDistance(p));
                    } catch (final ClassCastException e) {
                        throw new IllegalStateException(APPROXIMATE_NBR_RANGE + " should be associated with a double concentration", e);
                    }
                }
                return routingDistance(p);
            }
            return getDevicePosition().getDistanceTo(p);
        });
    }

    private Field buildFieldWithPosition(final Function<Position, ?> fun) {
        return buildField(fun, getDevicePosition());
    }

}
