/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.debatty.java.stringsimilarity.Levenshtein;
import info.debatty.java.stringsimilarity.interfaces.StringDistance;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Incarnation;

/**
 * This enum interfaces the generic components of the graphical interface with
 * the specific incarnation details.
 * 
 */
public final class SupportedIncarnations {

    @SuppressWarnings("rawtypes")
    private static final Set<Class< ? extends Incarnation>> INCARNATIONS;
    private static final Logger L = LoggerFactory.getLogger(SupportedIncarnations.class);
    private static final StringDistance METRIC = new Levenshtein();

    static {
        final Reflections reflections = new Reflections();
        INCARNATIONS = Collections.unmodifiableSet(reflections.getSubTypesOf(Incarnation.class));
    }

    private SupportedIncarnations() {
    }

    /**
     * @return The set of incarnations currently available.
     */
    public static Set<String> getAvailableIncarnations() {
        return INCARNATIONS.stream()
                .map(Class::getSimpleName)
                .map(SupportedIncarnations::preprocess)
                .collect(Collectors.toSet());
    }

    /**
     * Fetches an incarnation whose name matches the supplied string.
     * 
     * @param s
     *            the name of the {@link Incarnation}
     * @param <T>
     *            {@link Concentration} type
     * @return an {@link Optional} containing the incarnation, if one with a
     *         matching name exists
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<Incarnation<T>> get(final String s) {
        final String cmp = preprocess(s);
        return INCARNATIONS.stream()
                .map(clazz -> new Pair<>(METRIC.distance(preprocess(clazz.getSimpleName()), cmp), clazz))
                .min((p1, p2) -> Double.compare(p1.getFirst(), p2.getFirst()))
                .map(Pair::getSecond)
                .flatMap(clazz -> {
                    try {
                        return Optional.of(clazz.newInstance());
                    } catch (Exception e) {
                        L.error("Unable to instance incarnation " + clazz + " (closest match to " + s + " among " + INCARNATIONS + ")", e);
                        return Optional.empty();
                    }
                });
    }

    private static String preprocess(final String s) {
        return s.toLowerCase(Locale.ENGLISH).replace("incarnation", "");
    }

}

