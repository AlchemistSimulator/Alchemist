/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.protelis;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.implementations.properties.ProtelisDevice;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Position2D;
import it.unibo.alchemist.model.Reaction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.math3.random.RandomGenerator;
import org.protelis.lang.datatype.DatatypeFactory;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Field;
import org.protelis.lang.datatype.Tuple;
import org.protelis.vm.LocalizedDevice;
import org.protelis.vm.SpatiallyEmbeddedDevice;
import org.protelis.vm.TimeAwareDevice;
import org.protelis.vm.impl.AbstractExecutionContext;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @param <P> position type
 */
public final class AlchemistExecutionContext<P extends Position<P>>
    extends AbstractExecutionContext<AlchemistExecutionContext<P>>
    implements SpatiallyEmbeddedDevice<Double>, LocalizedDevice, TimeAwareDevice<Number> {

    private static final String INTENTIONAL = "This is intentional";
    /**
     * Put this {@link Molecule} inside nodes that should compute distances using routes.
     * It only makes sense in case the environment is a {@link MapEnvironment}
     */
    public static final Molecule USE_ROUTES_AS_DISTANCES = new SimpleMolecule("ROUTES_AS_DISTANCE");
    /**
     * Put this {@link Molecule} inside nodes that should compute distances using routes approximating them.
     * It only makes sense in case the environment is a {@link MapEnvironment}
     */
    public static final Molecule APPROXIMATE_NBR_RANGE = new SimpleMolecule("APPROXIMATE_NBR_RANGE");

    private final LoadingCache<P, Double> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .maximumSize(100)
        .build(new CacheLoader<>() {
            @Nonnull
            @Override
            public Double load(@Nonnull final P dest) {
            if (environment instanceof MapEnvironment) {
                if (dest instanceof GeoPosition) {
                    return ((MapEnvironment<Object, ?, ?>) environment).computeRoute(node, (GeoPosition) dest).length();
                } else {
                    throw new IllegalStateException("Illegal position type: " + dest.getClass() + " " + dest);
                }
            }
            return getDevicePosition().distanceTo(dest);
        }
    });
    private final Environment<Object, P> environment;
    private int hash;
    private double nbrRangeTimeout;
    private double precalcdRoutingDistance = Double.NaN;
    private final Node<Object> node;
    private final RandomGenerator randomGenerator;
    private final Reaction<Object> reaction;
    private final ProtelisDevice protelisDevice;

    /**
     * @param environment
     *            the simulation {@link Environment}
     * @param localNode
     *            the local {@link Node}
     * @param reaction
     *            the {@link Reaction} hosting the program
     * @param random
     *            the {@link RandomGenerator} for this simulation
     * @param networkManager
     *            the {@link AlchemistNetworkManager} to be used
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = INTENTIONAL)
    public AlchemistExecutionContext(
            final Environment<Object, P> environment,
            final Node<Object> localNode,
            final Reaction<Object> reaction,
            final RandomGenerator random,
            final AlchemistNetworkManager networkManager) {
        this(environment, localNode, localNode.asProperty(ProtelisDevice.class), reaction, random, networkManager);
    }

    /**
     * @param environment
     *            the simulation {@link Environment}
     * @param localNode
     *            the local {@link Node}
     * @param protelisDevice
     *            the local {@link ProtelisDevice}
     * @param reaction
     *            the {@link Reaction} hosting the program
     * @param random
     *            the {@link RandomGenerator} for this simulation
     * @param networkManager
     *            the {@link AlchemistNetworkManager} to be used
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = INTENTIONAL)
    public AlchemistExecutionContext(
            final Environment<Object, P> environment,
            final Node<Object> localNode,
            final ProtelisDevice protelisDevice,
            final Reaction<Object> reaction,
            final RandomGenerator random,
            final AlchemistNetworkManager networkManager) {
        super(protelisDevice, networkManager);
        this.environment = environment;
        node = localNode;
        this.protelisDevice = protelisDevice;
        this.reaction = reaction;
        randomGenerator = random;
    }

    private <X> Field<X> buildFieldWithPosition(final Function<? super P, X> fun) {
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
        if (target instanceof ProtelisDevice) {
            return environment.getDistanceBetweenNodes(node, ((ProtelisDevice) target).getNode());
        }
        throw new IllegalArgumentException("Not a valid " + ProtelisDevice.class.getSimpleName() + ": " + target);
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
        return environment.getDistanceBetweenNodes(node, environment.getNodeByID(target));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AlchemistExecutionContext) {
            final AlchemistExecutionContext<?> ctx = (AlchemistExecutionContext<?>) obj;
            return node.equals(ctx.node)
                && environment.equals(ctx.environment)
                && reaction.equals(ctx.reaction)
                && randomGenerator.equals(ctx.randomGenerator);
        }
        return false;
    }

    @Override
    public Tuple getCoordinates() {
        return DatatypeFactory.createTuple((Object[]) ArrayUtils.toObject(getDevicePosition().getCoordinates()));
    }

    @Override
    public Number getCurrentTime() {
        return reaction.getTau().toDouble();
    }

    /**
     * @return the device position, in form of {@link Position}
     */
    public P getDevicePosition() {
        return environment.getPosition(node);
    }

    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = INTENTIONAL)
    public DeviceUID getDeviceUID() {
        return protelisDevice;
    }

    /**
     * @return experimental access to the simulated environment, for building oracles
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = INTENTIONAL)
    public Environment<Object, P> getEnvironmentAccess() {
        return environment;
    }

    /**
     * @return the internal {@link RandomGenerator}
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = INTENTIONAL)
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Hashing.murmur3_32_fixed().newHasher()
                .putInt(node.getId())
                .putInt(environment.hashCode())
                .putInt(reaction.hashCode())
                .hash().asInt();
        }
        return hash;
    }

    @Override
    protected AlchemistExecutionContext<P> instance() {
        return new AlchemistExecutionContext<>(
            environment,
            node,
            reaction,
            randomGenerator,
            (AlchemistNetworkManager) getNetworkManager()
        );
    }

    /**
     * @return The same behavior of MIT Proto's nbrdelay (forward view).
     */
    @Override
    public Field<Number> nbrDelay() {
        return buildField(Function.identity(), getDeltaTime());
    }

    @Override
    public Field<Number> nbrLag() {
        return buildField(time -> getCurrentTime().doubleValue() - time, getCurrentTime().doubleValue());
    }

    @Override
    public Field<Double> nbrRange() {
        final boolean useRoutesAsDistances = environment instanceof MapEnvironment && node.contains(USE_ROUTES_AS_DISTANCES);
        return buildFieldWithPosition(p -> {
            if (useRoutesAsDistances) {
                if (p instanceof GeoPosition) {
                    final GeoPosition destination = (GeoPosition) p;
                    if (node.contains(APPROXIMATE_NBR_RANGE)) {
                        try {
                            final double tolerance = (double) node.getConcentration(APPROXIMATE_NBR_RANGE);
                            final double currTime = environment.getSimulation().getTime().toDouble();
                            if (currTime > nbrRangeTimeout) {
                                nbrRangeTimeout = currTime + tolerance;
                                precalcdRoutingDistance = routingDistance(destination);
                            }
                            assert !Double.isNaN(precalcdRoutingDistance);
                            return precalcdRoutingDistance;
                        } catch (final ClassCastException e) {
                            throw new IllegalStateException(
                                    APPROXIMATE_NBR_RANGE + " should be associated with a double concentration",
                                    e
                            );
                        }
                    }
                    return routingDistance(destination);
                } else {
                    throw new IllegalStateException("Inconsistent position types");
                }
            }
            return getDevicePosition().distanceTo(p);
        });
    }

    @Override
    public Field<Tuple> nbrVector() {
        return buildFieldWithPosition(p -> {
            final P diff = getDevicePosition().minus(p.getCoordinates());
            if (diff instanceof Position2D) {
                final Position2D<?> vector = (Position2D<?>) diff;
                return DatatypeFactory.createTuple(vector.getX(), vector.getY());
            }
            throw new NotImplementedException("Protelis support for 3D environments not ready yet.");
        });
    }

    @Override
    public double nextRandomDouble() {
        return randomGenerator.nextDouble();
    }

    /**
     * Computes the distance along a map. Requires a {@link MapEnvironment}.
     * 
     * @param dest
     *            the destination, in form of a destination node
     * @return the distance on a map
     */
    public double routingDistance(final Node<Object> dest) {
        return routingDistance((GeoPosition) environment.getPosition(dest));
    }

    /**
     * Computes the distance along a map. Requires a {@link MapEnvironment}.
     * 
     * @param dest
     *            the destination, in form of {@link Node} ID. Non
     *            integer numbers will be cast to integers by
     *            {@link Number#intValue()}.
     * @return the distance on a map
     */
    public double routingDistance(final Number dest) {
        return routingDistance(environment.getNodeByID(dest.intValue()));
    }

    /**
     * Computes the distance along a map. Requires a {@link MapEnvironment}.
     * 
     * @param dest
     *            the destination
     * @return the distance on a map
     */
    @SuppressWarnings("unchecked")
    public double routingDistance(final GeoPosition dest) {
        try {
            return cache.get((P) dest);
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
     */
    public double routingDistance(final Tuple dest) {
        if (dest.size() == 2) {
            return routingDistance(new LatLongPosition((Number) dest.get(0), (Number) dest.get(1)));
        }
        throw new IllegalArgumentException(dest + " is not a coordinate I can understand.");
    }
}
