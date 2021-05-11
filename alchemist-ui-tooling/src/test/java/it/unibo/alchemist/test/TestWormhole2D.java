/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.boundary.wormhole.implementation.AbstractWormhole2D;
import it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter;
import it.unibo.alchemist.boundary.wormhole.interfaces.ViewPort;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.junit.jupiter.api.Test;

/**
 * Test for bugs in {@link AbstractWormhole2D}.
 */
public class TestWormhole2D {

    /**
     * Ensure that no exception is thrown when a zero-sized environment is
     * requested.
     */
    @Test
    public void testZeroSizeEnvironment() {
        final var incarnation = SupportedIncarnations.<Object, Euclidean2DPosition>get("protelis").orElseThrow();
        final Environment<Object, Euclidean2DPosition> env = new Continuous2DEnvironment<>(incarnation);
        final AbstractWormhole2D<Euclidean2DPosition> worm = new TestPurposeWormhole<>(env);
        worm.center();
    }

    private static class TestPurposeWormhole<P extends Position2D<? extends P>> extends AbstractWormhole2D<P> {
        TestPurposeWormhole(final Environment<?, P> env) {
            super(
                    env,
                    new ViewPort() {
                        @Override
                        public double getWidth() {
                            return 0;
                        }

                        @Override
                        public double getHeight() {
                            return 0;
                        }
                    },
                    viewType -> PointAdapter.from(viewType.getWidth() / 2.0, viewType.getHeight() / 2.0)
            );
        }
    }
}
