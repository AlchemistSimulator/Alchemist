/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.protelis;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.util.CodePath;

import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Emulates a {@link NetworkManager}. This particular network manager does not
 * send messages instantly. Instead, it records the last message to send, and
 * only when {@link #simulateMessageArrival(double)} is called the transfer is
 * actually done.
 */
public final class AlchemistNetworkManager implements NetworkManager, Serializable {

    private static final long serialVersionUID = -7028533174885876642L;
    private final Environment<Object, ?> env;
    private final ProtelisNode node;
    /**
     * This reaction stores the time at which the neighbor state is read.
     */
    private final Reaction<Object> event;
    private final RunProtelisProgram<?> prog;
    private final double retentionTime;
    private Map<DeviceUID, MessageInfo> msgs = new LinkedHashMap<>();
    private Map<CodePath, Object> toBeSent;

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
            final ProtelisNode local,
            final Reaction<Object> executionTime,
            final RunProtelisProgram<?> program) {
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
            final ProtelisNode local,
            final Reaction<Object> executionTime,
            final RunProtelisProgram<?> program,
            final double retentionTime) {
        env = Objects.requireNonNull(environment);
        node = Objects.requireNonNull(local);
        prog = Objects.requireNonNull(program);
        this.event = Objects.requireNonNull(executionTime);
        if (retentionTime < 0) {
            throw new IllegalArgumentException("The retention time can't be negative.");
        }
        this.retentionTime = retentionTime;
    }

    private Map<DeviceUID, Map<CodePath, Object>> convertMessages(final Predicate<MessageInfo> isValid) {
        /*
         * Using streams here doesn't make guarantees on the kind of map returned, and may break the simulator
         */
        final LinkedHashMap<DeviceUID, Map<CodePath, Object>> result = new LinkedHashMap<>(msgs.size());
        final Iterator<MessageInfo> messages = msgs.values().iterator();
        while (messages.hasNext()) {
            final MessageInfo msg = messages.next();
            if (isValid.test(msg)) {
                result.put(msg.source, msg.payload);
            } else {
                messages.remove();
            }
        }
        return result;
    }

    @Override
    public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
        /*
         * If retentionTime is a number, use it. Otherwise clean all messages
         */
        if (msgs.isEmpty()) {
            return Collections.emptyMap();
        }
        if (Double.isNaN(retentionTime)) {
            final Map<DeviceUID, Map<CodePath, Object>> res = convertMessages(m -> true);
            msgs = new LinkedHashMap<>();
            return res;
        }
        final double currentTime = event.getTau().toDouble();
        return convertMessages(m -> currentTime - m.time < retentionTime);
    }

    /**
     * @return the message retention time, or NaN if all the messages get
     *         discarded as soon as a computation cycle is concluded.
     */
    public double getRetentionTime() {
        return retentionTime;
    }

    private void receiveMessage(final MessageInfo msg) {
        msgs.put(msg.source, msg);
    }

    @Override
    public void shareState(final Map<CodePath, Object> toSend) {
        toBeSent = toSend;
    }

    /**
     * Simulates the arrival of the message to other nodes.
     * 
     * @param currentTime
     *            the current simulation time (used to understand when a message
     *            should get dropped).
     */
    public void simulateMessageArrival(final double currentTime) {
        assert toBeSent != null;
        Objects.requireNonNull(toBeSent);
        if (!toBeSent.isEmpty()) {
            final MessageInfo msg = new MessageInfo(currentTime, node, toBeSent);
            env.getNeighborhood(node).forEach(n -> {
                if (n instanceof ProtelisNode) {
                    final AlchemistNetworkManager destination = ((ProtelisNode) n).getNetworkManager(prog);
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

    private static class MessageInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private final double time;
        private final Map<CodePath, Object> payload;
        private final DeviceUID source;
        MessageInfo(final double time, final DeviceUID source, final Map<CodePath, Object> payload) {
            this.time = time;
            this.payload = payload;
            this.source = source;
        }
        @Override
        public String toString() {
            return source.toString() + '@' + time;
        }
    }

}
