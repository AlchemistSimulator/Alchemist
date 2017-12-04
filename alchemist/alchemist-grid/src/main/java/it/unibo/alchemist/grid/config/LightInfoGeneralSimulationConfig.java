package it.unibo.alchemist.grid.config;

import java.util.Map;

import it.unibo.alchemist.loader.Loader;
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
    private static final long serialVersionUID = 1L;
    private final long endStep;
    private final Time endTime;
    private final Loader loader;

    /**
     * 
     * @param endStep Simulation's end step
     * @param endTime Simulation's end time
     * @param loader Simulation's loader
     */
    public LightInfoGeneralSimulationConfig(final Loader loader, final long endStep, final Time endTime) {
        this.endStep = endStep;
        this.endTime = endTime;
        this.loader = loader;
    }

    @Override
    public Loader getLoader() {
        return this.loader;
    }

    @Override
    public abstract Map<String, byte[]> getDependencies();
    @Override
    public long getEndStep() {
        return endStep;
    }
    @Override
    public Time getEndTime() {
        return endTime;
    }
}
