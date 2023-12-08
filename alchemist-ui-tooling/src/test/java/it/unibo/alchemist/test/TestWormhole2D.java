/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.boundary.ui.api.ViewPort;
import it.unibo.alchemist.boundary.ui.impl.AbstractWormhole2D;
import it.unibo.alchemist.boundary.ui.impl.PointAdapter;
import it.unibo.alchemist.model.SupportedIncarnations;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position2D;
import it.unibo.alchemist.model.environments.Continuous2DEnvironment;
import org.junit.jupiter.api.Test;

/**
 * Test for bugs in {@link AbstractWormhole2D}.
 */
class TestWormhole2D {

    /**
     * Ensure that no exception is thrown when a zero-sized environment is
     * requested.
     */
    @Test
    void testZeroSizeEnvironment() {
        final var incarnation = SupportedIncarnations.<Object, Euclidean2DPosition>get("protelis").orElseThrow();
        final Environment<Object, Euclidean2DPosition> environment = new Continuous2DEnvironment<>(incarnation);
        final AbstractWormhole2D<Euclidean2DPosition> worm = new TestPurposeWormhole<>(environment);
        worm.center();
    }

    private static class TestPurposeWormhole<P extends Position2D<? extends P>> extends AbstractWormhole2D<P> {
        TestPurposeWormhole(final Environment<?, P> environment) {
            super(
                    environment,
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
