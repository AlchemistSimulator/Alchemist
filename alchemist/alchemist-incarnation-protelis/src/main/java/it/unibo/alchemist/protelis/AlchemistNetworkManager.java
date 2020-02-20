/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.protelis;

import com.github.gscaparrotti.ns3asybindings.bindings.NS3asy;
import com.google.common.collect.Lists;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.github.gscaparrotti.ns3asybindings.communication.NS3Gateway;
import com.github.gscaparrotti.ns3asybindings.communication.NS3Gateway.Endpoint;
import it.unibo.alchemist.model.ProtelisIncarnation;
import it.unibo.alchemist.model.implementations.actions.AbstractProtelisNetworkAction;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.implementations.timedistributions.Trigger;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.ns3.AlchemistNs3;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.impl.IntegerUID;
import org.protelis.vm.CodePath;
import org.protelis.vm.NetworkManager;
import com.github.gscaparrotti.ns3asybindings.streams.NS3OutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Emulates a {@link NetworkManager}. This particular network manager does not
 * send messages instantly. Instead, it records the last message to send, and
 * only when {@link #simulateMessageArrival(double, boolean)} is called the transfer is
 * actually done.
 */
public final class AlchemistNetworkManager implements NetworkManager, Serializable {

    private static final long serialVersionUID = -7028533174885876642L;
    private final Environment<Object, ?> env;
    private final ProtelisNode<?> node;
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
        final Node<?> source = env.getNodeByID(msg.source.getUID());
        //ProtelisNode implements DeviceUID
        if (source instanceof DeviceUID) {
            msgs.put((DeviceUID) source, msg);
        }
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
     * @param realistic true if the message arrival should be simulated using
     *                  a dedicated network simulator
     */
    public void simulateMessageArrival(final double currentTime, final boolean realistic) {
        assert toBeSent != null;
        Objects.requireNonNull(toBeSent);
        if (!toBeSent.isEmpty()) {
            final MessageInfo msg = new MessageInfo(currentTime, new IntegerUID(node.getId()), toBeSent);
            if (realistic) {
                simulateRealisticMessageArrival(msg);
            } else {
                simulateSimpleMessageArrival(msg);
            }
        }
        toBeSent = null;
    }

    private void simulateRealisticMessageArrival(final MessageInfo msg) {
        final var gateway = AlchemistNs3.getInstance();
        if (gateway == null) {
            throw new IllegalStateException("ns3 not initialized");
        }
        if (env.getIncarnation().isEmpty() || !(env.getIncarnation().get() instanceof ProtelisIncarnation)) {
            throw new IllegalStateException();
        }
        final int intId = node.getId();
        try {
            final var ns3OutputStream = new NS3OutputStream(gateway, intId, false);
            AlchemistNs3.getSerializer().serializeAndSend(msg, ns3OutputStream);
            final var sendTimes = ns3OutputStream.getFirstSendTimesAndReset();
            //At this point every received byte is already inside ns3 gateway
            final var senderIpPointer = NS3asy.INSTANCE.getIpAddressFromIndex(intId);
            final var sender = new Endpoint(senderIpPointer.getString(0), NS3Gateway.ANY_SENDER_PORT);
            for (final var genericNeighbor : env.getNeighborhood(node)) {
                if (genericNeighbor instanceof ProtelisNode) {
                    final var neighbor = (ProtelisNode<?>) genericNeighbor;
                    final int neighborIntId = neighbor.getId();
                    final var receiverIpPointer = NS3asy.INSTANCE.getIpAddressFromIndex(neighborIntId);
                    final var receiver = new Endpoint(receiverIpPointer.getString(0), NS3Gateway.DEFAULT_PORT);
                    /*
                     * If nothing is present in ns3 gateway, it means that the message has been lost,
                     * so we must do nothing; otherwise, we read what's been received and put it
                     * inside the receiving node at the appropriate time.
                     * When using TCP, which is mandatory when using Ns3OutputStream, a packet loss
                     * can only mean that the connection failed, probably due to a
                     * very high error rate, which should be lowered consequently, if possible.
                     */
                    if (gateway.getBytesInInterval(receiver, sender, 0, 1).size() > 0) {
                        /*
                         * This should work, but for some unknown reason it doesn't.
                         * The method below is a workaround.
                         * NS3asyInputStream ns3asyInputStream = new NS3asyInputStream(gateway, sender, receiver);
                         * ObjectInputStream ois = new ObjectInputStream(ns3asyInputStream);
                         */
                        final var bytes = gateway.getBytesInInterval(receiver, sender, 0, -1);
                        final var receivedObject = AlchemistNs3.getSerializer().deserialize(new ByteArrayInputStream(NS3Gateway.convertToByteArray(bytes)));
                        //Once the object is read it must be removed from the not-read-yet bytes
                        gateway.removeBytesInInterval(receiver, sender, 0, -1);
                        if (receivedObject instanceof MessageInfo) {
                            final var rcvdMsg = (MessageInfo) receivedObject;
                            /*
                             * The reception of the message is scheduled to happen with a delay
                             * given by how much time the packets needed to go from one node to another
                             * inside ns3. This is the whole point of using ns3.
                             */
                            final var delta = bytes.get(bytes.size() - 1).getRight() - sendTimes.get(receiver.getIp());
                            final var trigger = new Trigger<>(env.getSimulation().getTime().plus(new DoubleTime(delta)));
                            final var reaction = new Event<>(neighbor, trigger);
                            final var neighborProgram = neighbor.getReactions().stream()
                                    .flatMap(r -> r.getActions().stream())
                                    .filter(a -> a instanceof RunProtelisProgram)
                                    .findFirst();
                            if (neighborProgram.isPresent()) {
                                reaction.setActions(Lists.newArrayList(new ReceiveFromNetwork(neighbor, reaction, (RunProtelisProgram<?>) neighborProgram.get(), rcvdMsg)));
                                neighbor.addReaction(reaction);
                                env.getSimulation().reactionAdded(reaction);
                            } else {
                                throw new IllegalStateException("The destination node is not running a Protelis program");
                            }
                        } else {
                            throw new IOException("Error while receiving Java object");
                        }
                    }
                    Native.free(Pointer.nativeValue(receiverIpPointer));
                }
            }
            Native.free(Pointer.nativeValue(senderIpPointer));
        } catch (final IOException | ClassNotFoundException e) {
            /*
             * since we're writing inside a "fake" stream, this should not happen,
             * unless something bad happens in ns3 (maybe a programming error)
             */
            throw new IllegalStateException("ns3 was unable to deliver the message", e);
        }

    }

    private void simulateSimpleMessageArrival(final MessageInfo msg) {
        env.getNeighborhood(node).forEach(n -> {
            if (n instanceof ProtelisNode) {
                final AlchemistNetworkManager destination = ((ProtelisNode<?>) n).getNetworkManager(prog);
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

    private static final class ReceiveFromNetwork extends AbstractProtelisNetworkAction {

        private static final long serialVersionUID = 1L;
        private final MessageInfo msg;

        private ReceiveFromNetwork(final ProtelisNode<?> node, final Reaction<Object> reaction, final RunProtelisProgram<?> program, final MessageInfo msg) {
            super(node, reaction, program);
            this.msg = msg;
        }

        @Override
        public void execute() {
            final AlchemistNetworkManager destination = this.getNode().getNetworkManager(this.getProtelisProgram());
            if (destination != null) {
                destination.receiveMessage(msg);
            }
        }

        @Override
        public Action<Object> cloneAction(final Node<Object> n, final Reaction<Object> r) {
            throw new UnsupportedOperationException("Action cloning is not currently supported for ReceiveFromNetwork");
        }

        @Override
        public String toString() {
            return "receive " + this.getProtelisProgram().asMolecule().getName() + " data";
        }
    }

    private static final class MessageInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private final double time;
        private final Map<CodePath, Object> payload;
        private final IntegerUID source;
        private MessageInfo(final double time, final IntegerUID source, final Map<CodePath, Object> payload) {
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
