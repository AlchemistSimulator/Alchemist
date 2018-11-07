/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.grid.config;

import java.util.Map;
import java.util.Set;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

/**
 * Remote {@link GeneralSimulationConfig} that stores big informations in Ignite's cache.
 *
 */
public class RemoteGeneralSimulationConfig extends LightInfoGeneralSimulationConfig implements AutoCloseable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String cacheName;
    private final Set<String> keys;

    /**
     * 
     * @param sc A general simulation config to clone
     * @param ignite An Ignite instance for cache creation
     */
    public RemoteGeneralSimulationConfig(final GeneralSimulationConfig sc, final Ignite ignite) {
        super(sc.getLoader(), sc.getEndStep(), sc.getEndTime());
        this.cacheName = ignite.cluster().localNode().id().toString();

        this.keys = sc.getDependencies().keySet();

        final CacheConfiguration<String, byte[]> cacheCfg = new CacheConfiguration<>(this.cacheName);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        final IgniteCache<String, byte[]> cache = ignite.getOrCreateCache(cacheCfg);
        cache.putAll(sc.getDependencies());
    }

    @Override
    public Map<String, byte[]> getDependencies() {
        final IgniteCache<String, byte[]> cache = Ignition.ignite().cache(this.cacheName);
        return cache.getAll(this.keys);
    }

    @Override
    public void close() {
        Ignition.ignite().cache(this.cacheName).clear();
        Ignition.ignite().cache(this.cacheName).destroy();
    }

}
