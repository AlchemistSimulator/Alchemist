/**
 * 
 */
package it.unibo.alchemist.protelis;

import org.danilopianini.lang.HashUtils;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Tuple;
import org.protelis.vm.ExecutionEnvironment;
import org.protelis.vm.impl.AbstractExecutionContext;

import org.apache.commons.math3.random.RandomGenerator;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.IMapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 */
public class AlchemistExecutionContext extends AbstractExecutionContext {

    private final ProtelisNode node;
    private final Environment<Object> env;
    private final Reaction<Object> react;
    private final RandomGenerator rand;
    private int hash;

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
     * Computes the distance along a map. Requires a {@link IMapEnvironment}.
     * 
     * @param dest
     *            the destination, as a {@link Tuple} of two values: [latitude,
     *            longitude]
     * @return the distance on a map
     */
    public double routingDistance(final Tuple dest) {
        if (dest.size() == 2) {
            return routingDistance(new LatLongPosition((Number) dest.get(0), (Number) dest.get(1)));
        }
        throw new IllegalArgumentException(dest + " is not a coordinate I can understand.");
    }

    /**
     * Computes the distance along a map. Requires a {@link IMapEnvironment}.
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
     * Computes the distance along a map. Requires a {@link IMapEnvironment}.
     * 
     * @param dest
     *            the destination, in form of a destination node
     * @return the distance on a map
     */
    public double routingDistance(final Node<Object> dest) {
        return routingDistance(env.getPosition(dest));
    }

    /**
     * Computes the distance along a map. Requires a {@link IMapEnvironment}.
     * 
     * @param dest
     *            the destination
     * @return the distance on a map
     */
    public double routingDistance(final Position dest) {
        if (env instanceof IMapEnvironment<?>) {
            return ((IMapEnvironment<Object>) env).computeRoute(node, dest).getDistance();
        }
        return getDevicePosition().getDistanceTo(dest);
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

}
