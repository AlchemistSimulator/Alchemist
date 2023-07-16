/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.maps.movestrategies.target;

import it.unibo.alchemist.model.maps.positions.LatLongPosition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.movestrategies.target.FollowTarget;

/**
 * This strategy reads the value of a "target" molecule and tries to interpret it as a coordinate.
 * 
 * @param <T> concentration type
 */
public class FollowTargetOnMap<T> extends FollowTarget<T, GeoPosition> {

    private static final long serialVersionUID = 0L;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param targetMolecule
     *            the target molecule
     */
    public FollowTargetOnMap(
        final Environment<T, GeoPosition> environment,
        final Node<T> node,
        final Molecule targetMolecule
    ) {
        super(environment, node, targetMolecule);
    }

    @Override
    protected GeoPosition createPosition(final double latitude, final double longitude) {
        return new LatLongPosition(latitude, longitude);
    }

    @Override
    public FollowTargetOnMap<T> cloneIfNeeded(final Node<T> destination, final Reaction<T> reaction) {
        return new FollowTargetOnMap<>(getEnvironment(), destination, getTargetMolecule());
    }
}
