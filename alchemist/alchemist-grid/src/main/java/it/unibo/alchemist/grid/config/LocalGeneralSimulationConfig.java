package it.unibo.alchemist.grid.config;

import java.util.HashMap;
import java.util.Map;

public class LocalGeneralSimulationConfig implements GeneralSimulationConfig {

    private final String yaml;
    private final Map<String, String> dependencies;
    
    //TODO meglio loader
    public LocalGeneralSimulationConfig(String yaml) {
        this.yaml = yaml;
        this.dependencies = new HashMap<>();
    }
    
    @Override
    public String getYaml() {
        // TODO Auto-generated method stub
        return this.yaml;
    }

    @Override
    public Map<String, String> getYamlDependencies() {
        // TODO Auto-generated method stub
        return this.dependencies;
    }

}
