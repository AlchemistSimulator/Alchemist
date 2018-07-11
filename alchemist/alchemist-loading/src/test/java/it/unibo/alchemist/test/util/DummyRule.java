package it.unibo.alchemist.test.util;

import java.util.List;

import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * @param <T>
 */
public final class DummyRule<T> extends NoLinks<T, Position<?>> {

    private static final long serialVersionUID = 1L;

    /**
     * @param param1
     *            p1
     * @param param2
     *            p2
     */
    public DummyRule(final List<Double> param1, final List<Double> param2) {
        check(param1);
        check(param2);
    }

    private static void checkNot(final boolean condition) {
        if (condition) {
            throw new IllegalArgumentException();
        }
    }

    private static void check(final List<Double> l) {
        checkNot(l.isEmpty());
        for (final Object d: l) {
            checkNot(!(d instanceof Number));
        }
    }
}
