/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.util;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Utility to translate statistics names into a {@link RealDistribution}.
 */
public final class RealDistributionUtil {

    private static final ImmutableList<Class<? extends RealDistribution>> REAL_DISTRIBUTIONS = ImmutableList.copyOf(
         ClassPathScanner.subTypesOf(RealDistribution.class));

    private RealDistributionUtil() {
    }

    /**
     * @param randomGenerator
     *            the {@link RandomGenerator}
     * @param shortname
     *            the distribution name (case insensitive). Must be mappable to
     *            an entity implementing {@link RealDistribution}
     * @param arguments
     *            the parameters for the distribution
     * @return the created {@link RealDistribution}
     * @throws IllegalArgumentException
     *             if the creation can't be completed for any reason
     */
    @SuppressWarnings("unchecked")
    public static RealDistribution makeRealDistribution(
            final RandomGenerator randomGenerator,
            final String shortname,
            final double... arguments
    ) {
        final String name = shortname + (
            shortname.endsWith("distribution") || shortname.endsWith("Distribution") ? "" : "distribution"
        );
        final var result = REAL_DISTRIBUTIONS.stream()
            .filter(stat -> stat.getSimpleName().equalsIgnoreCase(requireNonNull(name)))
            .findAny()
            .stream()
            .map(Class::getConstructors)
            .flatMap(Arrays::stream)
            .map(c -> (Constructor<? extends RealDistribution>) c)
            .filter(c -> c.getParameterTypes().length == 1 + requireNonNull(arguments).length)
            .filter(c -> c.getParameterTypes()[0].isAssignableFrom(requireNonNull(randomGenerator).getClass()))
            .findAny()
            .map(c -> {
                final Object[] actualArguments = Stream.concat(
                    Stream.of(randomGenerator),
                    Arrays.stream(arguments).boxed()
                ).toArray();
                try {
                    return c.newInstance(actualArguments);
                } catch (
                    IllegalAccessException
                    | InstantiationException
                    | IllegalArgumentException
                    | InvocationTargetException e
                ) {
                    throw new IllegalArgumentException(
                        "Could not initialize " + name + " with " + c + " and arguments " + Arrays.toString(arguments),
                        e
                    );
                }
            })
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Could not initialize " + name + " with " + randomGenerator + " and " + Arrays.toString(arguments)
                )
            );
        if (!(result instanceof Serializable)) {
            throw new IllegalStateException(
                result.getClass().getSimpleName() + " is not Serializable. This may break the simulator."
            );
        }
        return result;
    }
}
