/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import it.unibo.alchemist.util.ClassPathScanner;
import org.jooq.lambda.Unchecked;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This enum interfaces the generic components of the graphical interface with
 * the specific incarnation details.
 * 
 */
@SuppressWarnings("unchecked")
public final class SupportedIncarnations {

    private static final Map<String, Class<? extends Incarnation<?, ?>>> INCARNATIONS;

    static {
        INCARNATIONS = ClassPathScanner.subTypesOf(
            Incarnation.class,
            "it.unibo.alchemist"
        ).stream()
            .map(it -> (Class<Incarnation<?, ?>>) it)
            .collect(Collectors.toMap(c -> preprocess(c.getSimpleName()), Function.identity()));
    }

    private SupportedIncarnations() {
    }

    /**
     * @return The set of incarnations currently available.
     */
    public static Set<String> getAvailableIncarnations() {
        return Collections.unmodifiableSet(INCARNATIONS.keySet());
    }

    /**
     * Fetches an incarnation whose name matches the supplied string.
     * 
     * @param s
     *            the name of the {@link Incarnation}
     * @param <T>
     *            {@link it.unibo.alchemist.model.Concentration} type
     * @param <P>
     *            {@link Position} type
     *
     * @return an {@link Optional} containing the incarnation, if one with a
     *         matching name exists
     */
    public static <T, P extends Position<? extends P>> Optional<Incarnation<T, P>> get(final String s) {
        final String cmp = preprocess(s);
        return Optional.ofNullable(INCARNATIONS.get(cmp))
                .map(Unchecked.function(it -> (Incarnation<T, P>) it.getDeclaredConstructor().newInstance()));
    }

    private static String preprocess(final String s) {
        return s.toLowerCase(Locale.ENGLISH).replace("incarnation", "");
    }

}

