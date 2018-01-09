package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

/**
 * {@link SimulationConfig} implementation.
 *
 */
public final class SimulationConfigImpl implements SimulationConfig {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Map<String, ? extends Serializable> variables;

    /**
     * 
     * @param variables Simulation's initialization variables
     */
    public SimulationConfigImpl(final List<Entry<String, ? extends Serializable>> variables) {
        this.variables = Objects.requireNonNull(variables).stream()
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public Map<String, ? extends Serializable> getVariables() {
        return this.variables;
    }

    @Override
    public int hashCode() {
        return variables.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SimulationConfigImpl) {
            final SimulationConfigImpl other = (SimulationConfigImpl) obj;
            return variables.equals(other.variables);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.variables.entrySet().stream()
                .map(e -> e.getKey() + '-' + e.getValue())
                .collect(Collectors.joining("_"));
    }
}
