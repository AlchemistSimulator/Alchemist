package it.unibo.alchemist.ns3;

import com.github.gscaparrotti.ns3asybindings.bindings.NS3asy;
import com.github.gscaparrotti.ns3asybindings.communication.NS3Gateway;
import it.unibo.alchemist.ns3.utils.DefaultNs3Serializer;
import it.unibo.alchemist.ns3.utils.Serializer;

/**
 * This class allows the configuration of ns3asy-bindings, which is a library that allows
 * the use of ns3 from a Java environment, and provides access to the object which serializes
 * the Java objects that should be sent through ns3asy-bindings.
 * Only one instance of ns3asy-bindings should be used, so this class
 * is implemented as a "configurable" singleton.
 */
public final class AlchemistNs3 {

    private static volatile NS3Gateway gateway;
    private static volatile Serializer serializer = new DefaultNs3Serializer();

    private AlchemistNs3() { }

    /**
     * @return the configured instance of {@link NS3Gateway}, or null if it hasn't been configured.
     */
    public static NS3Gateway getInstance() {
        return gateway;
    }

    /**
     * @return the configured instance of {@link Serializer}. If it hasn't been configured,
     * an instance of {@link DefaultNs3Serializer} is returned.
     */
    public static Serializer getSerializer() {
        return serializer;
    }

    /**
     * Creates and configures a new instance of {@link NS3Gateway}, configuring ns3asy-bindings
     * to use a CSMA (Ethernet-like) channel at the physical level. Every node is able to communicate
     * with every other node.
     *
     * @param nodesCount How many nodes in the network
     * @param isUdp true if UDP should be used, false is TCP should be used
     * @param packetSize The size of a packet (0 for default size)
     * @param errorRate The error rate of the channel
     * @param dataRate The data rate at which the nodes emit data
     *
     * @throws IllegalStateException if {@link NS3Gateway} has already been configured
     */
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

    /**
     * @param serializer the object in charge of serializing Java objects before sending it through the channel
     */
    public static void setSerializer(final Serializer serializer) {
        AlchemistNs3.serializer = serializer;
    }

}
