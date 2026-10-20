/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.timedistributions;

import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import org.danilopianini.lang.HashString;

import java.util.List;
import java.util.Map;

/**
 * Interface for TimeDistribution that need matches.
 *
 */
public interface SAPERETimeDistribution extends TimeDistribution<List<ILsaMolecule>> {

    /**
     * Returns the rate of this time distribution.
     *
     * @return the rate computed against the currently installed SAPERE match token
     */
    double getRate();

    /**
     * Checks whether the rate of this time distribution is constant.
     *
     * @return true if the equation is actually a number
     */
    boolean isStatic();

    /**
     * Sets the matches used to compute the rate.
     *
     * @param match the map of matches
     */
    void setMatches(Map<HashString, ITreeNode<?>> match);

    /**
     * Returns the equation used to compute the rate.
     *
     * @return the rate equation
     */
    IExpression getRateEquation();

}
