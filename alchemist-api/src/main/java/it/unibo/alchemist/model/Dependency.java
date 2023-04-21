/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model;

import java.io.Serializable;

/**
 * This interface represents a token that may generate a dependency between two reactions. Some special built-in tokens
 * are EVERYTHING, EVERY_MOLECULE, MOVEMENT, and NEIGHBORHOOD_CHANGE. Molecules are dependencies as well.
 */
public interface Dependency extends Serializable {
    /**
     * Declares a dependency towards any other reaction in the reachable scope.
     */
    Dependency EVERYTHING = new Dependency() {
        @Override
        public boolean dependsOn(final Dependency dependency) {
            return true;
        }

        @Override
        public String toString() {
            return "Everything";
        }
    };

    /**
     * Declares a dependency towards any modified molecule in the reachable scope.
     */
    Dependency EVERY_MOLECULE = new Dependency() {
        @Override
        public boolean dependsOn(final Dependency dependency) {
            return equals(dependency) || dependency instanceof Molecule;
        }
        @Override
        public String toString() {
            return "Every_molecule";
        }
    };

    /**
     * Declares a dependency on movement of nodes in the reachable scope.
     */
    Dependency MOVEMENT = new Dependency() {
        @Override
        public String toString() {
            return "Movement";
        }
    };

    /**
     * Determines whether this dependency depends on the provided dependency.
     * The default behavior requires equality.
     * 
     * @param dependency
     *            the dependency
     * @return true if this dependency generates a dependency with the provided one
     */
    default boolean dependsOn(final Dependency dependency) {
        return equals(dependency);
    }

    /**
     * Determines whether the provided dependency depends on this dependency.
     * The default behavior calls {@link #dependsOn(Dependency)}, and provides a bidirectional dependency.
     *
     * @param dependency
     *            the dependency
     * @return true if this dependency generates a dependency with the provided one
     */
    default boolean makesDependent(final Dependency dependency) {
        return dependsOn(dependency);
    }

}
