package it.unibo.alchemist.grid.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import it.unibo.alchemist.loader.Loader;
import scala.util.control.Exception;

public class LocalGeneralSimulationConfig implements GeneralSimulationConfig {

    private final String yaml;
    private final Map<String, String> dependencies;

    public LocalGeneralSimulationConfig(Loader loader) {
        this.yaml = loader.getYamlAsString();
        this.dependencies = new HashMap<>();
        for (String file : loader.getDependencies()) {
            try {
                this.dependencies.put(file, new String(Files.readAllBytes(Paths.get(file))));
            } catch (IOException e) {
                //TODO pre controllo nel loader per l'esistenza???
                throw new IllegalArgumentException("Dependency non exixts: " + file);
            }
        }
        /*this.dependencies = loader.getDependencies().stream().collect(Collectors.toMap(e -> e, e-> {
            byte[] encoded = null;
            try {
                encoded = Files.readAllBytes(Paths.get(e));
            } catch (IOException e1) {
                throw new FileNotFoundException();
            }
            return new String(encoded);
        }));*/
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
