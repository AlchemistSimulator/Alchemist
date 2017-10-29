package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.Map;

import it.unibo.alchemist.model.interfaces.Time;

public interface GeneralSimulationConfig<T> extends Serializable{
    public String getYaml();
    public Map<String, String> getYamlDependencies();
    public long getEndStep();
    public Time getEndTime();
}
