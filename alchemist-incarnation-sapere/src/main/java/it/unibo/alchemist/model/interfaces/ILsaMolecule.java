/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;

import java.util.List;
import java.util.Map;

import it.unibo.alchemist.model.Molecule;
import org.danilopianini.lang.HashString;


/**
 */
public interface ILsaMolecule extends Molecule, Iterable<IExpression>, Comparable<ILsaMolecule> {

    /**
     * @param matches
     *            the map with the variable / value bindings
     * @return the list of the arguments updated
     */
    List<IExpression> allocateVar(Map<HashString, ITreeNode<?>> matches);

    /**
     * @return the arguments number of the LSA structure.
     */
    int argsNumber();

    /**
     * @return true if the molecule arguments match with mol arguments.
     * @param mol
     *            the ILsaMolecule to compare.
     */
    @Override
    boolean equals(Object mol);

    /**
     * @return a new {@link ILsaMolecule} produced by replacing all non-atomic
     *         and non-numeric values with variables.
     */
    ILsaMolecule generalize();

    /**
     * @param i
     *            : position of the argument to get
     * @return the Expression represent the argument i.
     */
    IExpression getArg(int i);

    /**
     * @return true if this variable makes use of variables defined within the molecule itself
     */
    boolean hasDuplicateVariables();

    /**
     * @param mol
     *            the molecule to compare to
     * @return true if lsaMolecule is identical to mol, namely that all the
     *         arguments are equal
     **/
    boolean isIdenticalTo(ILsaMolecule mol);

    /**
     * @return true if the molecule is an instance (not variable in the
     *         argouments).
     */
    boolean isIstance();

    /**
     * @param mol
     *            the LsaMolecule to try to match with.
     * @return true if the two molecules match
     */
    boolean matches(ILsaMolecule mol);

    /**
     * @param mol
     *            the LsaMolecule to try to match with
     * @param duplicateVariables
     *            if true, the matching of variables reused within the same
     *            tuple is enabled
     * @return true if the two molecules match
     */
    boolean matches(List<? extends IExpression> mol, boolean duplicateVariables);

    /**
     * @param mol
     *            the molecule to compare to
     * @return true if the molecule is more generic than the one passed
     */
    boolean moreGenericOf(ILsaMolecule mol);

    /**
     * @return the number of arguments
     */
    int size();

    /**
     * @return the string representing the molecule, in a faster implementation.
     */
    HashString toHashString();

    /**
     * @return the string representing the molecule.
     */
    @Override
    String toString();

}
