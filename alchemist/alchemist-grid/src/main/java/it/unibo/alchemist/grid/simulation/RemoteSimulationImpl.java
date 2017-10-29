package it.unibo.alchemist.grid.simulation;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.SimulationConfig;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.loader.export.Exporter;
import it.unibo.alchemist.model.interfaces.Environment;

public class RemoteSimulationImpl<T> implements RemoteSimulation<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 8206545835842309336L;
    private final GeneralSimulationConfig<T> generalConfig;
    private final SimulationConfig config;

    public RemoteSimulationImpl(GeneralSimulationConfig<T> generalConfig, SimulationConfig config) {
        this.generalConfig = generalConfig;
        this.config = config;
    }



    @Override
    public RemoteResult call() throws Exception {
        Loader loader = new YamlLoader(this.generalConfig.getYaml());
        final Environment<T> env = loader.getWith(this.config.getVariables());
        final Simulation<T> sim = new Engine<>(env, this.generalConfig.getEndStep(), this.generalConfig.getEndTime());

        final Map<String, Object> defaultVars = loader.getVariables().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDefault()));
        defaultVars.putAll(this.config.getVariables());
        final String header = this.config.getVariables().entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining(", "));
        final String filename = "test" + "_" + this.config.getVariables().entrySet().stream()
                .map(e -> e.getKey() + '-' + e.getValue())
                .collect(Collectors.joining("_")) + ".txt";
        try {
            final Exporter<T> exp = new Exporter<>(filename, 1, header, loader.getDataExtractors());
            sim.addOutputMonitor(exp);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        //sim.play();
        //sim.run();
        //sim.getError();

        System.out.println(this.config.getVariables());
        return null;
    }

}
