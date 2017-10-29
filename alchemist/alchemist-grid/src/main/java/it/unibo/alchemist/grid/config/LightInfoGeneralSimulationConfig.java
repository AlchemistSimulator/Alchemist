package it.unibo.alchemist.grid.config;

import java.util.Map;

import it.unibo.alchemist.model.interfaces.Time;

public abstract class LightInfoGeneralSimulationConfig<T> implements GeneralSimulationConfig<T> {

    private final long endStep;
    private final Time endTime;

    public LightInfoGeneralSimulationConfig(final long endStep, final Time endTime) {
        this.endStep = endStep;
        this.endTime = endTime;
    }

    @Override
    public abstract String getYaml();

    @Override
    public abstract Map<String, String> getYamlDependencies();
    @Override
    public long getEndStep() {
        return endStep;
    }
    @Override
    public Time getEndTime() {
        return endTime;
    }
}
