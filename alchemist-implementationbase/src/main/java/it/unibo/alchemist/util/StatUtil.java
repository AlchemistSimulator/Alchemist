/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.util;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;

/**
 * Utility to translate statistics names into a {@link UnivariateStatistic}.
 */
public final class StatUtil {

    private static final Set<Class<? extends UnivariateStatistic>> STATISTICS = ImmutableSet.copyOf(
        ClassPathScanner.subTypesOf(
            UnivariateStatistic.class,
            "org.apache.commons.math3",
            "it.unibo.alchemist"
        )
    );

    private StatUtil() {
    }

    /**
     * @param name
     *            the statistic
     * @return a new instance of the corresponding {@link UnivariateStatistic}
     *         wrapped in a {@link Optional}, if one exists;
     *         {@link Optional#empty()} otherwise.
     */
    public static Optional<UnivariateStatistic> makeUnivariateStatistic(final String name) {
        return STATISTICS.stream()
            .filter(stat -> stat.getSimpleName().equalsIgnoreCase(name))
            .findAny()
            .map(clazz -> {
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    throw new IllegalStateException("Could not initialize with empty constructor " + clazz, e);
                }
            });
    }

}
