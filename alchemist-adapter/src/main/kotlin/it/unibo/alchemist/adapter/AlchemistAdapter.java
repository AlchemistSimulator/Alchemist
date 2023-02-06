/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.adapter;

import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.export.exporters.MultiVestaExporter;

public class AlchemistAdapter implements SimultationAdapter {

    private final Simulation<?, ?> simulation;

    public AlchemistAdapter(final Simulation<?, ?> simulation) {
        this.simulation = simulation;
    }

    @Override
    public double getTime() {
        return simulation.getStep();
    }

    @Override
    public double rval(final String obs) {
        return obsValueToDouble(MultiVestaExporter.Companion.getValue(simulation, obs));
    }

    @Override
    public double rval(final int obsId) {
        return obsValueToDouble(MultiVestaExporter.Companion.getValue(simulation, obsId));
    }

    @Override
    public void doStep() {
        simulation.goToStep(simulation.getStep() + 1);
    }

    @Override
    public void performWholeSimulation() {
        simulation.goToStep(simulation.getFinalStep());
    }

    private double obsValueToDouble(final Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
