package it.unibo.alchemist.grid.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.model.interfaces.Time;

public class LocalGeneralSimulationConfig<T> extends LightInfoGeneralSimulationConfig<T> {

    private final String yaml;
    private final Map<String, String> dependencies;

    public LocalGeneralSimulationConfig(Loader loader, long endStep, Time endTime) {
        super(endStep, endTime);

        this.yaml = loader.getYamlAsString();
        this.dependencies = new HashMap<>();
        for (final String file : loader.getDependencies()) {
            try {
                final URL dependency = LocalGeneralSimulationConfig.class.getResource(file);
                if (dependency != null) {
                    dependencies.put(file, new String(Files.readAllBytes(Paths.get(dependency.toURI()))));
                } else {
                    throw new IllegalArgumentException("Dependency non exixts: " + file);
                }
            } catch (IOException e) {
                //TODO pre controllo nel loader per l'esistenza???
                throw new IllegalArgumentException("Dependency non exixts: " + file);
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Failed to get resource URI: " + file);
            }
        }
    }

    @Override
    public String getYaml() {
        return this.yaml;
    }

    @Override
    public Map<String, String> getYamlDependencies() {
        return this.dependencies;
    }

}
