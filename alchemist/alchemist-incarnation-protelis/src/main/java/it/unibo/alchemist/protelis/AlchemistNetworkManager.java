/**
 * 
 */
package it.unibo.alchemist.protelis;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.util.CodePath;

import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Environment;

/**
 * Emulates a {@link NetworkManager}. This particular network manager does not
 * send messages istantly. Instead, it records the last message to send, and
 * only when {@link #simulateMessageArrival()} is called the transfer is
 * actually done.
 * 
 */
public final class AlchemistNetworkManager implements NetworkManager, Serializable {

    private static final long serialVersionUID = -7028533174885876642L;
    private final Environment<Object> env;
    private final ProtelisNode node;
    private final RunProtelisProgram prog;
    private Map<DeviceUID, Map<CodePath, Object>> msgs = new LinkedHashMap<>();
    private Map<CodePath, Object> toBeSent;

    /**
     * @param environment
     *            the environment
     * @param local
     *            the node
     * @param program
     *            the {@link RunProtelisProgram}
     */
    public AlchemistNetworkManager(final Environment<Object> environment, final ProtelisNode local, final RunProtelisProgram program) {
        env = environment;
        node = local;
        prog = program;
    }

    @Override
    public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
        final Map<DeviceUID, Map<CodePath, Object>> res = msgs;
        msgs = new LinkedHashMap<>();
        return res;
    }

    @Override
    public void shareState(final Map<CodePath, Object> toSend) {
        toBeSent = toSend;
    }

    /**
     * 
     */
    public void simulateMessageArrival() {
        assert toBeSent != null;
        Objects.requireNonNull(toBeSent);
        if (!toBeSent.isEmpty()) {
            env.getNeighborhood(node).forEach(n -> {
                if (n instanceof ProtelisNode) {
                    final AlchemistNetworkManager destination = ((ProtelisNode) n).getNetworkManager(prog);
                    if (destination != null) {
                        /*
                         * The node is running the program. Otherwise, the
                         * program is discarded
                         */
                        destination.msgs.put(node, toBeSent);
                    }
                }
            });
        }
        toBeSent = null;
    }

}
