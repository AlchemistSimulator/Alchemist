/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.grid.config;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link SimulationConfig} implementation.
 *
 */
public final class SimulationConfigImpl implements SimulationConfig {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final ImmutableMap<String, ? extends Serializable> variables;

    /**
     * 
     * @param variables Simulation's initialization variables
     */
    public SimulationConfigImpl(final Map<String, ? extends Serializable> variables) {
        this.variables = ImmutableMap.copyOf(variables);
    }

    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "The field is immutable")
    public ImmutableMap<String, ? extends Serializable> getVariables() {
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
