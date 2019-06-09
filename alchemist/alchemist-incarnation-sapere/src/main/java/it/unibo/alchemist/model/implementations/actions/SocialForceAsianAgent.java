/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Position;

import java.util.List;

/**
 *
 * @param <P> position type
 */
public class SocialForceAsianAgent<P extends Position<P>> extends SocialForceAgent<P> {

    /**
     * 
     */
    private static final long serialVersionUID = 4486294475660423922L;
    /**
     * Minimum speed of a pedestrian.
     */
    public static final double VMIN = 1.0;
    /**
     * Dimension of a pedestrian.
     */
    public static final double MY_DIMENSION = 0.25;
    /**
     * Desired space of a pedestrian: minimum "vital" space that the agent wants
     * to maintain around himself.
     */
    public static final double DESIRED_SPACE = 0.03;
    /**
     * The probability of turn to the right if another pedestrian is too near.
     */
    public static final double TURN_RIGHT_PROBABILITY = 0.05;

    /**
     * Construct an Asian pedestrian. with
     * 
     * @param environment
     *            the current environment
     * @param node
     *            the current node
     * @param random
     *            the current random engine
     * @param molecule
     *            the LSA to inspect once moving (typically a gradient)
     * @param pos
     *            the position in the LSA of the value to read for identifying
     *            the new position
     * @param group
     *            the group identifier: if equals to 0 there is no group
     * @param stopAtTarget
     *               flag used to let the user chose to make pedestrians to stop once the target is reached
     */
    public SocialForceAsianAgent(final Environment<List<ILsaMolecule>, P> environment, final ILsaNode node, final RandomGenerator random, final LsaMolecule molecule, final int pos, final int group, final boolean stopAtTarget) {
        super(environment, node, random, molecule, pos, group, stopAtTarget, VMIN + (random.nextDouble() * 2.0), MY_DIMENSION, DESIRED_SPACE, TURN_RIGHT_PROBABILITY);
    }

}
