package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * {@link SimulationConfig} implementation.
 *
 */
public class SimulationConfigImpl implements SimulationConfig {
    /**
     * 
     */
    private static final long serialVersionUID = 3086808700926081922L;
    private final Map<String, ? extends Serializable> variables;

    /**
     * 
     * @param variables Simulation's inizializzation variables
     */
    public SimulationConfigImpl(final List<Entry<String, ? extends Serializable>> variables) {
        this.variables = variables.stream().collect(Collectors.toMap(e -> e.getKey(), e-> e.getValue()));
    }

    @Override
    public Map<String, ? extends Serializable> getVariables() {
        return this.variables;
    }

}
