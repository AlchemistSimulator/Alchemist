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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((variables == null) ? 0 : variables.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimulationConfigImpl other = (SimulationConfigImpl) obj;
        if (variables == null) {
            if (other.variables != null)
                return false;
        } else if (!variables.equals(other.variables))
            return false;
        return true;
    }
}
