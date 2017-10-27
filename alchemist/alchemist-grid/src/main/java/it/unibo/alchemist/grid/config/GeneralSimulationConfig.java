package it.unibo.alchemist.grid.config;

import java.util.Map;

public interface GeneralSimulationConfig {
    public String getYaml();
    public Map<String, String> getYamlDependencies();
}
