package it.unibo.alchemist.grid.simulation;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Charsets;

import it.unibo.alchemist.grid.config.SimulationConfig;
import it.unibo.alchemist.grid.exceptions.RemoteSimulationException;

/**
 * {@link RemoteResult} implementation.
 *
 */
public class RemoteResultImpl implements RemoteResult {

    private final String result;
    private final UUID workerNode;
    private final Optional<Throwable> simulationErrors;
    private final SimulationConfig config;

    /**
     * 
     * @param result Result file's content as string
     * @param workerNode UUID of worker node that has done the simulation
     * @param simulationErrors Simulation's errors
     * @param config Simulation's specific config
     */
    public RemoteResultImpl(final String result, final UUID workerNode, final Optional<Throwable> simulationErrors,
            final SimulationConfig config) {
        this.result = result;
        this.workerNode = workerNode;
        this.simulationErrors = simulationErrors;
        this.config = config;
    }

    @Override
    public void saveLocally(final String targetFile) throws RemoteSimulationException, FileNotFoundException {
        if (simulationErrors.isPresent()) {
            throw new RemoteSimulationException(this.workerNode, this.config, simulationErrors.get());
        }
        final String target = targetFile + "_" + this.config.getVariables().entrySet().stream()
                .map(e -> e.getKey() + '-' + e.getValue())
                .collect(Collectors.joining("_")) + ".txt";
        try (PrintStream out = new PrintStream(target, Charsets.UTF_8.name())) {
            out.print(result);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("There is a bug in Alchemist, in " + getClass(), e);
        }
    }

}
