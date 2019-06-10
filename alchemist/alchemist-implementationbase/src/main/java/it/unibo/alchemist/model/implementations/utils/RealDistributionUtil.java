/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.utils;

import com.google.common.collect.ImmutableList;
import it.unibo.alchemist.ClassPathScanner;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

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
     * @param rng
     *            the {@link RandomGenerator}
     * @param shortname
     *            the distribution name (case insensitive). Must be mappable to
     *            an entity implementing {@link RealDistribution}
     * @param args
     *            the parameters for the distribution
     * @return the created {@link RealDistribution}
     * @throws IllegalArgumentException
     *             if the creation can't be completed for any reason
     */
    @SuppressWarnings("unchecked")
    public static RealDistribution makeRealDistribution(final RandomGenerator rng, final String shortname, final double... args) {
        final String name = shortname + (shortname.endsWith("distribution") || shortname.endsWith("Distribution") ? "" : "distribution");
        return REAL_DISTRIBUTIONS.stream()
            .filter(stat -> stat.getSimpleName().equalsIgnoreCase(requireNonNull(name)))
            .findAny()
            .map(Stream::of)
            .orElseGet(Stream::empty)
            .map(Class::getConstructors)
            .flatMap(Arrays::stream)
            .map(c -> (Constructor<? extends RealDistribution>) c)
            .filter(c -> c.getParameterTypes().length == 1 + requireNonNull(args).length)
            .filter(c -> c.getParameterTypes()[0].isAssignableFrom(requireNonNull(rng).getClass()))
            .findAny()
            .map(c -> {
                final Object[] arguments = Stream.concat(Stream.of(rng), Arrays.stream(args).boxed()).toArray();
                try {
                    return c.newInstance(arguments);
                } catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Could not initialize " + name + " with " + c + " and arguments " + Arrays.toString(arguments), e);
                }
            }).orElseThrow(() -> new IllegalArgumentException("Could not initialize " + name + " with " + rng + " and " + Arrays.toString(args)));
    }

}
