package it.unibo.alchemist.grid.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

public class RemoteGeneralSimulationConfig<T> extends LightInfoGeneralSimulationConfig<T> implements AutoCloseable {

    /**
     * 
     */
    private static final long serialVersionUID = 6793498536768599629L;
    private final String cacheName;
    private final Set<String> keys;
    private static final String YAML_KEY = "yaml";

    public RemoteGeneralSimulationConfig(GeneralSimulationConfig<T> sc, Ignite ignite) {
        super(sc.getEndStep(), sc.getEndTime());
        //TODO mettere nome simulazione sulla GeneralSimulationConfig
        this.cacheName = "prova";

        this.keys = sc.getYamlDependencies().keySet();

        CacheConfiguration<String, String> cacheCfg = new CacheConfiguration<>("prova");
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        IgniteCache<String, String> cache = ignite.getOrCreateCache(cacheCfg);
        cache.putAll(sc.getYamlDependencies());
        cache.put(YAML_KEY, sc.getYaml());
    }

    @Override
    public String getYaml() {
        //TODO annotazioni ignite per evitare Ignition.something???
        IgniteCache<String, String> cache = Ignition.ignite().cache(this.cacheName);
        return cache.get(YAML_KEY);
    }

    @Override
    public Map<String, String> getYamlDependencies() {
        IgniteCache<String, String> cache = Ignition.ignite().cache(this.cacheName);
        return cache.getAll(this.keys);
    }

    @Override
    public void close() {
        Ignition.ignite().cache(this.cacheName).clear();
        Ignition.ignite().cache(this.cacheName).destroy();
    }

}
