package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

public interface Dependency extends Serializable {

    public static Dependency EVERYTHING = new Dependency() {
        @Override
        public boolean dependsOn(final Dependency dependency) { return true; }

        @Override
        public String toString() {
            return "Everything";
        }
    };

    public static Dependency EVERY_MOLECULE = new Dependency() {
        @Override
        public boolean dependsOn(final Dependency dependency) {
            return dependency == this || dependency instanceof Molecule;
        }
        @Override
        public String toString() {
            return "Every_molecule";
        }
    };

    public static Dependency MOVEMENT = new Dependency() {
        @Override
        public String toString() {
            return "Movement";
        }
    };

    public static Dependency NEIGHBORHOOD_CHANGE = new Dependency() {
        @Override
        public boolean dependsOn(Dependency dependency) {
            return MOVEMENT.equals(dependency) || equals(dependency);
        }
        @Override
        public boolean makesDependent(Dependency dependency) {
            return equals(dependency);
        }
        @Override
        public String toString() {
            return "Neighborhood_change";
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
    default boolean dependsOn(Dependency dependency) {
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
    default boolean makesDependent(Dependency dependency) {
        return dependsOn(dependency);
    }

}
