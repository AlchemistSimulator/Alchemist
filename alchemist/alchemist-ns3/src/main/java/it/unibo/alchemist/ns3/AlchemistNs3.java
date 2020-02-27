package it.unibo.alchemist.ns3;

import com.github.gscaparrotti.ns3asybindings.bindings.NS3asy;
import com.github.gscaparrotti.ns3asybindings.communication.NS3Gateway;
import it.unibo.alchemist.ns3.utils.DefaultNs3Serializer;
import it.unibo.alchemist.ns3.utils.Serializer;

import java.util.List;

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
     * @param protocol the protocol to be used
     * @param packetSize The size of a packet (0 for default size)
     * @param errorRate The error rate of the channel
     * @param dataRate The data rate at which the nodes emit data
     *
     * @throws IllegalStateException if {@link NS3Gateway} has already been configured
     */
    public static void initWithCsma(final int nodesCount, final String protocol, final int packetSize, final double errorRate, final String dataRate) {
        initCommon(nodesCount);
        NS3asy.INSTANCE.FinalizeSimulationSetup(protocol.equalsIgnoreCase("udp"), packetSize, errorRate, dataRate);
    }

    /**
     * Creates and configures a new instance of {@link NS3Gateway}, configuring ns3asy-bindings
     * to use a Wi-Fi channel at the physical level. Every node is able to communicate
     * with every other node.
     *
     * @param nodesCount How many nodes in the network
     * @param protocol the protocol to be used
     * @param packetSize The size of a packet (0 for default size)
     * @param propagationDelay The propagation delay model
     * @param propagationLoss The propagation loss model
     * @param nodesPositions The positions of the nodes
     * @param apPosition The position of the access point
     *
     * @throws IllegalStateException if {@link NS3Gateway} has already been configured
     */
    public static void initWithWifi(final int nodesCount, final String protocol, final int packetSize, final String propagationDelay, final String propagationLoss,
                                    final List<double[]> nodesPositions, final double[] apPosition) {
        if (!nodesPositions.isEmpty() && nodesPositions.get(0).length != 2 || apPosition.length != 2) {
            throw new UnsupportedOperationException("The dimensionality of this environment cannot be handled by ns3");
        }
        final double[] xPos = new double[nodesPositions.size() + 1];
        final double[] yPos = new double[nodesPositions.size() + 1];
        for (int i = 0; i < nodesPositions.size(); i++) {
            xPos[i + 1] = nodesPositions.get(i)[0];
            yPos[i + 1] = nodesPositions.get(i)[1];
        }
        xPos[0] = apPosition[0];
        yPos[0] = apPosition[1];
        initCommon(nodesCount);
        NS3asy.INSTANCE.FinalizeWithWifiPhy(protocol.equalsIgnoreCase("udp"), packetSize, propagationDelay, propagationLoss, xPos, yPos);
    }

    private static void initCommon(final int nodesCount) {
        gateway = new NS3Gateway();
        NS3asy.INSTANCE.SetNodesCount(nodesCount);
        for (int source = 0; source < nodesCount; source++) {
            for (int destination = 0; destination < nodesCount; destination++) {
                if (source != destination) {
                    NS3asy.INSTANCE.AddLink(source, destination);
                }
            }
        }
    }

    /**
     * @param serializer the object in charge of serializing Java objects before sending it through the channel
     */
    public static void setSerializer(final Serializer serializer) {
        AlchemistNs3.serializer = serializer;
    }

}
