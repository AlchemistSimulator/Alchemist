/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jooq.lambda.Unchecked;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * This enum interfaces the generic components of the graphical interface with
 * the specific incarnation details.
 * 
 */
public final class SupportedIncarnations {

    @SuppressWarnings("rawtypes")
    private static final Map<String, Class<? extends Incarnation>> INCARNATIONS;

    static {
        INCARNATIONS = ClassPathScanner.subTypesOf(Incarnation.class).stream()
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
     *            {@link it.unibo.alchemist.model.interfaces.Concentration} type
     * @param <P>
     *            {@link it.unibo.alchemist.model.interfaces.Position} type
     *
     * @return an {@link Optional} containing the incarnation, if one with a
     *         matching name exists
     */
    @SuppressWarnings("rawtypes")
    public static <T, P extends Position<? extends P>> Optional<Incarnation<T, P>> get(final String s) {
        final String cmp = preprocess(s);
        return Optional.ofNullable(INCARNATIONS.get(cmp))
                .map(Unchecked.<Class<? extends Incarnation>, Incarnation<T, P>>function(Class::newInstance));
    }

    private static String preprocess(final String s) {
        return s.toLowerCase(Locale.ENGLISH).replace("incarnation", "");
    }

}

