package it.unibo.alchemist.grid.config;

import java.util.Map;

import it.unibo.alchemist.model.interfaces.Time;

/**
 *  Abstract simulation config that contains small serializable informations.
 * 
 * @param <T> the concentration type
 */
public abstract class LightInfoGeneralSimulationConfig<T> implements GeneralSimulationConfig<T> {

    /**
     * 
     */
    private static final long serialVersionUID = -3109924486818903310L;
    private final long endStep;
    private final Time endTime;

    /**
     * 
     * @param endStep Simulation's end step
     * @param endTime Simulation's end time
     */
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
