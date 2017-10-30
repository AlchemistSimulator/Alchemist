package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.Map;

import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.model.interfaces.Time;
/**
 * Simulation's configs valid for more than one simulation.
 *
 * @param <T> the concentration type
 */
public interface GeneralSimulationConfig<T> extends Serializable {
    /**
     * 
     * @return simulation's yaml as string
     */
    Loader getLoader();
    /**
     * 
     * @return Map with dependencies files path as key and their content as value
     */
    Map<String, String> getDependencies();
    /**
     * 
     * @return Simulation's end step
     */
    long getEndStep();
    /**
     * 
     * @return Simulation's end time
     */
    Time getEndTime();
}
