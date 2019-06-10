/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;
import java.awt.Component;

import org.junit.jupiter.api.Test;

import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.Environment;

/**
 * Test for bugs in {@link Wormhole2D}.
 */
public class TestWormhole2D {

    /**
     * Ensure that no exception is thrown when a zero-sized environment is
     * requested.
     */
    @Test
    public void testZeroSizeEnvironment() {
        final Environment<Object, Euclidean2DPosition> env = new Continuous2DEnvironment<>();
        final Wormhole2D<Euclidean2DPosition> worm = new Wormhole2D<>(env, new Component() {
            private static final long serialVersionUID = 1L;
        });
        worm.center();
    }

}
