package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import it.unibo.alchemist.loader.variables.Variable;

public class SimulationConfigImpl implements SimulationConfig {
    private final Map<String, ? extends Serializable> variables;

    public SimulationConfigImpl(List<Entry<String, ? extends Serializable>> variables) {
        this.variables = variables.stream().collect(Collectors.toMap(e -> e.getKey(), e-> e.getValue()));
    }

    @Override
    public Map<String, ? extends Serializable> getVariables() {
        return this.variables;
    }

}
