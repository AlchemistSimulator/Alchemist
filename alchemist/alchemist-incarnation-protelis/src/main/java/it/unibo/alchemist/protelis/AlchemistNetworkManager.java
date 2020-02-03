/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.protelis;

import bindings.NS3asy;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import communication.NS3Gateway;
import communication.NS3Gateway.Endpoint;
import it.unibo.alchemist.model.implementations.actions.AbstractProtelisNetworkAction;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.implementations.timedistributions.Trigger;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.impl.IntegerUID;
import org.protelis.vm.CodePath;
import org.protelis.vm.NetworkManager;
import streams.NS3OutputStream;

import java.io.*;
import java.util.*;
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
        final var gateway = ProtelisNs3.getInstance();
        if (gateway == null) {
            throw new IllegalStateException("ns3 not initialized");
        }
        if (env.getIncarnation().isPresent()) {
            final var incarnation = env.getIncarnation().get();
            //Each node must contain a molecule whose concentration is its id in ns3.
            //If it's not present, it's not possible to use the ns3 backend.
            final var id = node.getConcentration(incarnation.createMolecule("ns3id"));
            if (id instanceof Double && DoubleMath.isMathematicalInteger((Double) id)) {
                final int intId = ((Double) id).intValue();
                try {
                    final var oos = new ObjectOutputStream(new NS3OutputStream(intId, false));
                    oos.writeObject(msg);
                    oos.close();
                    //At this point every received byte is already inside ns3 gateway
                    final var senderIpPointer = NS3asy.INSTANCE.getIpAddressFromIndex(intId);
                    final var sender = new Endpoint(senderIpPointer.getString(0), NS3Gateway.ANY_SENDER_PORT);
                    for (final var genericNeighbor : env.getNeighborhood(node)) {
                        if (genericNeighbor instanceof ProtelisNode) {
                            final var neighbor = (ProtelisNode<?>) genericNeighbor;
                            final var neighborId = neighbor.getConcentration(incarnation.createMolecule("ns3id"));
                            if (neighborId instanceof Double && DoubleMath.isMathematicalInteger((Double) neighborId)) {
                                final int neighborIntId = ((Double) neighborId).intValue();
                                final var receiverIpPointer = NS3asy.INSTANCE.getIpAddressFromIndex(neighborIntId);
                                final var receiver = new Endpoint(receiverIpPointer.getString(0), NS3Gateway.DEFAULT_PORT);
                                //If nothing is present in ns3 gateway, it means that the message has been lost,
                                //so we must do nothing; otherwise, we read what's been received and put it
                                //inside the receiving node at the appropriate time.
                                //When using TCP, which is mandatory when using Ns3OutputStream, a packet loss
                                //can only mean that the connection failed, probably due to a
                                //very high error rate, which should be lowered consequently, if possible.
                                if (gateway.getBytesInInterval(receiver, sender, 0, 1).size() > 0) {
                                    //This should work, but for some unknown reason it doesn't.
                                    //The method below is a workaround.
                                    //NS3asyInputStream ns3asyInputStream = new NS3asyInputStream(gateway, sender, receiver);
                                    //ObjectInputStream ois = new ObjectInputStream(ns3asyInputStream);
                                    final var bytes = gateway.getBytesInInterval(receiver, sender, 0, -1);
                                    final var ois = new ObjectInputStream(new ByteArrayInputStream(NS3Gateway.convertToByteArray(bytes)));
                                    final Object receivedObject = ois.readObject();
                                    ois.close();
                                    //Once the object is read it must be removed from the not-read-yet bytes
                                    gateway.removeBytesInInterval(receiver, sender, 0, -1);
                                    if (receivedObject instanceof MessageInfo) {
                                        final var rcvdMsg = (MessageInfo) receivedObject;
                                        //The reception of the message is scheduled to happen with a delay
                                        //given by how much time the packets needed to go from one node to another
                                        //inside ns3. This is the whole point of using ns3.
                                        final var delta = bytes.get(bytes.size() - 1).getRight() - bytes.get(0).getRight();
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
                                        //this should not happen
                                        throw new IOException("Error while receiving Java object");
                                    }
                                }
                                Native.free(Pointer.nativeValue(receiverIpPointer));
                            } else {
                                throw new IllegalArgumentException("A ns3id molecule must have an integer value");
                            }
                        }
                    }
                    Native.free(Pointer.nativeValue(senderIpPointer));
                } catch (final IOException | ClassNotFoundException e) {
                    //since we're writing inside a "fake" stream, this should not happen
                    e.printStackTrace();
                    System.exit(859965);
                }
            } else {
                throw new IllegalArgumentException("A ns3id molecule must have an integer value");
            }
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

    public static final class ProtelisNs3 {

        private static NS3Gateway gateway = null;

        private ProtelisNs3() {}

        private static NS3Gateway getInstance() {
            return gateway;
        }

        public static void init(final int nodesCount, final boolean isUdp, final int packetSize, final double errorRate, final String dataRate) {
            if (gateway != null) {
                throw new IllegalStateException("You cannot initialize ns3 more than once");
            }
            gateway = new NS3Gateway();
            NS3asy.INSTANCE.SetNodesCount(nodesCount);
            for (int source = 0; source < nodesCount; source++) {
                for (int destination = 0; destination < nodesCount; destination++) {
                    if (source != destination) {
                        NS3asy.INSTANCE.AddLink(source, destination);
                    }
                }
            }
            NS3asy.INSTANCE.FinalizeSimulationSetup(isUdp, packetSize, errorRate, dataRate);
        }

    }

    private final class ReceiveFromNetwork extends AbstractProtelisNetworkAction {

        private final MessageInfo msg;

        public ReceiveFromNetwork(final ProtelisNode<?> node, final Reaction<Object> reaction, final RunProtelisProgram<?> program, final MessageInfo msg) {
            super(node, reaction, program);
            this.msg = msg;
        }

        @Override
        public void execute() {
            final AlchemistNetworkManager destination = this.getNode().getNetworkManager(prog);
            if (destination != null) {
                destination.receiveMessage(msg);
            }
        }

        @Override
        public String toString() {
            return "receive " + prog.asMolecule().getName() + " data";
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
