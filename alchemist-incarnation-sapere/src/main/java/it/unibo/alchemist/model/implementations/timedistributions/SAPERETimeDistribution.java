/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.timedistributions;

import java.util.List;
import java.util.Map;

import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.TimeDistribution;
import org.danilopianini.lang.HashString;

/**
 * Interface for TimeDistribution that need matches.
 * 
 */
public interface SAPERETimeDistribution extends TimeDistribution<List<ILsaMolecule>> {

    /**
     * @return true if the equation is actually a number
     */
    boolean isStatic();

    /**
     * @param match the map of matches
     */
    void setMatches(Map<HashString, ITreeNode<?>> match);

    /**
     * @return the rate equation
     */
    IExpression getRateEquation();

}
