package it.unibo.alchemist.ns3;

import com.github.gscaparrotti.ns3asybindings.bindings.NS3asy;
import com.github.gscaparrotti.ns3asybindings.communication.NS3Gateway;
import it.unibo.alchemist.ns3.utils.DefaultNs3Serializer;
import it.unibo.alchemist.ns3.utils.Serializer;

@SuppressWarnings("PMD.NonThreadSafeSingleton")
public final class AlchemistNs3 {

    private static NS3Gateway gateway;
    private static Serializer serializer;

    private AlchemistNs3() {}

    public static NS3Gateway getInstance() {
        return gateway;
    }

    public static Serializer getSerializer() {
        if (serializer == null) {
            serializer = new DefaultNs3Serializer();
        }
        return serializer;
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

    public static void setSerializer(final Serializer serializer) {
        if (AlchemistNs3.serializer != null) {
            throw new IllegalStateException("You cannot initialize ns3 serializer more than once");
        }
        AlchemistNs3.serializer = serializer;
    }

}
