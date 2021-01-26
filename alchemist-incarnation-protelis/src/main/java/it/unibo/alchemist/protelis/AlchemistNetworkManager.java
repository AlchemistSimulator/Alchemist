/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.protelis;

import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.CodePath;
import org.protelis.vm.NetworkManager;

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
    private final Environment<Object, ?> env;
    private final ProtelisNode<?> node;
    /**
     * This reaction stores the time at which the neighbor state is read.
     */
    private final Reaction<Object> event;
    private final RunProtelisProgram<?> program;
    private final double retentionTime;
    private final Map<DeviceUID, MessageInfo> messages = new LinkedHashMap<>();
    private Map<CodePath, Object> toBeSent;
    private Map<DeviceUID, Map<CodePath, Object>> neighborState = Collections.emptyMap();
    private double timeAtLastValidityCheck = Double.NEGATIVE_INFINITY;

    /**
     * @param environment
     *            the environment
     * @param local
     *            the node
     * @param executionTime
     *            the reaction hosting the {@link NetworkManager}
     * @param program
     *            the {@link RunProtelisProgram}
     */
    public AlchemistNetworkManager(
            final Environment<Object, ?> environment,
            final ProtelisNode<?> local,
            final Reaction<Object> executionTime,
            final RunProtelisProgram<?> program
    ) {
        this(environment, local, executionTime, program, Double.NaN);
    }

    /**
     * @param environment
     *            the environment
     * @param local
     *            the node
     * @param executionTime
     *            the reaction hosting the {@link NetworkManager}
     * @param program
     *            the {@link RunProtelisProgram}
     * @param retentionTime
     *            how long the messages will be stored. Pass {@link Double#NaN}
     *            to mean that they should get eliminated upon node awake.
     */
    public AlchemistNetworkManager(
            final Environment<Object, ?> environment,
            final ProtelisNode<?> local,
            final Reaction<Object> executionTime,
            final RunProtelisProgram<?> program,
            final double retentionTime
    ) {
        env = Objects.requireNonNull(environment);
        node = Objects.requireNonNull(local);
        this.program = Objects.requireNonNull(program);
        this.event = Objects.requireNonNull(executionTime);
        if (retentionTime < 0) {
            throw new IllegalArgumentException("The retention time can't be negative.");
        }
        this.retentionTime = retentionTime;
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
                        ? env.getNeighborhood(node).getNeighbors()
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
            env.getNeighborhood(node).forEach(n -> {
                if (n instanceof ProtelisNode) {
                    final AlchemistNetworkManager destination = ((ProtelisNode<?>) n).getNetworkManager(program);
                    if (destination != null) {
                        /*
                         * The node is running the program. Otherwise, the
                         * program is discarded
                         */
                        destination.receiveMessage(msg);
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
