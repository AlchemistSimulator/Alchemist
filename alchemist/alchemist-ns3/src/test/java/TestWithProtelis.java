import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.io.InputStream;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestWithProtelis {

    /**
     * Tests the integration with ns3 via ns3asy by running a simulation consisting in some nodes
     * executing a Protelis program which makes them send messages to each other. These messages
     * are delivered using ns3.
     *
     * @param <T> Position type
     * @param <P> Concentration type
     */
    @Test
    public <T, P extends Position<P>> void testNs3asy() {
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            final InputStream res = ResourceLoader.getResourceAsStream("ns3asy.yml");
            final Environment<T, P> env = new YamlLoader(res).getWith(Collections.emptyMap());
            final Simulation<T, P> sim = new Engine<>(env, 50);
            sim.play();
            sim.run();
            if (env.getIncarnation().isPresent()) {
                for (final var node : env.getNodes()) {
                    int received = ((Double) node.getConcentration(env.getIncarnation().get().createMolecule("msgs_received"))).intValue();
                    assertTrue(received > 0);
                    System.out.println("The node " + node.toString() + " received " + received + " messages");
                }
                System.out.println("Time is " + env.getSimulation().getTime());
            } else {
                fail("Incarnation not present");
            }
            sim.getError().ifPresent(Unchecked.consumer(e ->  {
                throw e;
            }));
        }

    }
}
