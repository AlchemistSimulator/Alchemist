/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.protelis;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import org.apache.commons.math3.distribution.RealDistribution;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.CodePath;
import org.protelis.vm.NetworkManager;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Emulates a {@link NetworkManager}. This particular network manager does not
 * send messages instantly. Instead, it records the last message to send, and
 * only when {@link #simulateMessageArrival(double)} is called the transfer is
 * actually done.
 */
public final class AlchemistNetworkManager implements NetworkManager, Serializable {

    private static final long serialVersionUID = 1L;
    private final Environment<Object, ?> environment;
    private final ProtelisNode<?> node;
    /**
     * This reaction stores the time at which the neighbor state is read.
     */
    private final Reaction<Object> event;
    private final RunProtelisProgram<?> program;
    private final double retentionTime;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All implementations are actually Serializable")
    @Nullable private final RealDistribution distanceLossDistribution;
    private final Map<DeviceUID, MessageInfo> messages = new LinkedHashMap<>();
    private Map<CodePath, Object> toBeSent;
    private Map<DeviceUID, Map<CodePath, Object>> neighborState = Collections.emptyMap();
    private double timeAtLastValidityCheck = Double.NEGATIVE_INFINITY;

    /**
     * @param executionTime
     *            the reaction hosting the {@link NetworkManager}
     * @param program
     *            the {@link RunProtelisProgram}
     */
    public AlchemistNetworkManager(
            final Reaction<Object> executionTime,
            final RunProtelisProgram<?> program
    ) {
        this(executionTime, program, Double.NaN);
    }

    /**
     * @param executionTime
     *            the reaction hosting the {@link NetworkManager}
     * @param program
     *            the {@link RunProtelisProgram}
     * @param retentionTime
     *            how long the messages will be stored. Pass {@link Double#NaN}
     *            to mean that they should get eliminated upon node awake.
     */
    public AlchemistNetworkManager(
            final Reaction<Object> executionTime,
            final RunProtelisProgram<?> program,
            final double retentionTime
    ) {
        this(executionTime, program, retentionTime, null);
    }

    /**
     * @param executionTime
     *            the reaction hosting the {@link NetworkManager}
     * @param program
     *            the {@link RunProtelisProgram}
     * @param retentionTime
     *            how long the messages will be stored. Pass {@link Double#NaN}
     *            to mean that they should get eliminated upon node awake.
     * @param distanceLossDistribution
     *            the package loss probability, scaling with distance.
     *            This {@link RealDistribution} will be used as follows:
     *            its PDF will be computed with {@link RealDistribution#density(double)},
     *            and will be fed the distance between the current node and the neighbor;
     *            the generated probability will in turn be used to determine the probability of the package to be
     *            successfully delivered. Can be null, in which case packets always arrive to neighbors.
     */
    public AlchemistNetworkManager(
            final Reaction<Object> executionTime,
            final RunProtelisProgram<?> program,
            final double retentionTime,
            final RealDistribution distanceLossDistribution
    ) {
        this.environment = Objects.requireNonNull(program.getEnvironment());
        node = Objects.requireNonNull(program.getNode());
        this.program = Objects.requireNonNull(program);
        this.event = Objects.requireNonNull(executionTime);
        if (retentionTime < 0) {
            throw new IllegalArgumentException("The retention time can't be negative.");
        }
        this.retentionTime = retentionTime;
        this.distanceLossDistribution = distanceLossDistribution;
    }

    public RealDistribution getDistancePacketLossDistribution() {
        return distanceLossDistribution;
    }

    @Override
    public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
        final double currentTime = event.getTau().toDouble();
        /*
         * If no time has passed, the last result is still valid, otherwise needs to be recomputed
         */
        if (timeAtLastValidityCheck != currentTime) {
            if (messages.isEmpty()) {
                neighborState = Collections.emptyMap();
            } else {
                /*
                 * If retentionTime is a number, use it. Otherwise clean messages of lost neighbors
                 */
                neighborState = new LinkedHashMap<>(messages.size());
                final Iterator<MessageInfo> messagesIterator = this.messages.values().iterator();
                final boolean retainsNeighbors = Double.isNaN(retentionTime);
                final Set<?> neighbors = retainsNeighbors
                        ? environment.getNeighborhood(node).getNeighbors()
                        : Collections.emptySet();
                while (messagesIterator.hasNext()) {
                    final MessageInfo message = messagesIterator.next();
                    if (retainsNeighbors && neighbors.contains(message.source) || currentTime - message.time < retentionTime) {
                        neighborState.put(message.source, message.payload);
                    } else {
                        // Removes from this.messages as well
                        messagesIterator.remove();
                    }
                }
                neighborState = Collections.unmodifiableMap(neighborState);
            }
            timeAtLastValidityCheck = currentTime;
        }
        return neighborState;
    }

    /**
     * @return the message retention time, or NaN if all the messages get
     *         discarded as soon as a computation cycle is concluded.
     */
    public double getRetentionTime() {
        return retentionTime;
    }

    private void receiveMessage(final MessageInfo msg) {
        messages.put(msg.source, msg);
    }

    @Override
    public void shareState(final Map<CodePath, Object> toSend) {
        toBeSent = Objects.requireNonNull(toSend);
    }

    /**
     * Simulates the arrival of the message to other nodes.
     *
     * @param currentTime
     *            the current simulation time (used to understand when a message
     *            should get dropped).
     */
    public void simulateMessageArrival(final double currentTime) {
        Objects.requireNonNull(toBeSent);
        if (!toBeSent.isEmpty()) {
            final MessageInfo msg = new MessageInfo(currentTime, node, toBeSent);
            environment.getNeighborhood(node).forEach(n -> {
                if (n instanceof ProtelisNode) {
                    final AlchemistNetworkManager destination = ((ProtelisNode<?>) n).getNetworkManager(program);
                    if (destination != null) {
                        boolean packetArrives = true;
                        if (distanceLossDistribution != null) {
                            final var distance = environment.getDistanceBetweenNodes(node, n);
                            final var random = program.getRandomGenerator().nextDouble();
                            packetArrives = random < distanceLossDistribution.cumulativeProbability(distance);
                        }
                        if (packetArrives) {
                            /*
                             * The node is running the program, and the loss model actually makes the packet arrive.
                             * Otherwise, the message is discarded
                             */
                            destination.receiveMessage(msg);
                        }
                    }
                }
            });
        }
        toBeSent = null;
    }

    private static final class MessageInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private final double time;
        private final Map<CodePath, Object> payload;
        private final DeviceUID source;
        private MessageInfo(final double time, final DeviceUID source, final Map<CodePath, Object> payload) {
            this.time = Objects.requireNonNull(time);
            this.payload = Objects.requireNonNull(payload);
            this.source = Objects.requireNonNull(source);
        }
        @Override
        public String toString() {
            return source.toString() + '@' + time;
        }
    }
}
