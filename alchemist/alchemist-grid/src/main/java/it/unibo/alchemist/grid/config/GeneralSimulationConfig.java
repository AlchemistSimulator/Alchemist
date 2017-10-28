package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.Map;

public interface GeneralSimulationConfig extends Serializable{
    public String getYaml();
    public Map<String, String> getYamlDependencies();
}
