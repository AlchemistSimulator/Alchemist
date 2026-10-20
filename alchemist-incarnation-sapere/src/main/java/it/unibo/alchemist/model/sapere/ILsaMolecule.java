/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere;

import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import org.danilopianini.lang.HashString;

import java.util.List;
import java.util.Map;

/**
 * A molecule of the SAPERE incarnation (LSA), whose arguments are expressions.
 */
public interface ILsaMolecule extends Molecule, Iterable<IExpression>, Comparable<ILsaMolecule> {

    /**
     * Allocates the variables of this molecule using the provided bindings.
     *
     * @param matches
     *            the map with the variable / value bindings
     * @return the list of the arguments updated
     */
    List<IExpression> allocateVar(Map<HashString, ITreeNode<?>> matches);

    /**
     * Returns the number of arguments of this molecule.
     *
     * @return the arguments number of the LSA structure.
     */
    int argsNumber();

    /**
     * Checks whether this molecule is equal to the provided object.
     *
     * @param mol
     *            the ILsaMolecule to compare.
     * @return true if the molecule arguments match with mol arguments.
     */
    @Override
    boolean equals(Object mol);

    /**
     * Generalizes this molecule by replacing its non-atomic and non-numeric values with variables.
     *
     * @return a new {@link ILsaMolecule} produced by replacing all non-atomic
     *         and non-numeric values with variables.
     */
    ILsaMolecule generalize();

    /**
     * Returns the argument at the provided position.
     *
     * @param i
     *            : position of the argument to get
     * @return the Expression represents the argument i.
     */
    IExpression getArg(int i);

    /**
     * Checks whether this molecule reuses its own variables.
     *
     * @return true if this variable makes use of variables defined within the molecule itself
     */
    boolean hasDuplicateVariables();

    /**
     * Checks whether this molecule is identical to the provided one.
     *
     * @param mol
     *            the molecule to compare to
     * @return true if lsaMolecule is identical to mol, namely that all the
     *         arguments are equal
     **/
    boolean isIdenticalTo(ILsaMolecule mol);

    /**
     * Checks whether this molecule is an instance, namely it has no variables in its arguments.
     *
     * @return true if the molecule is an instance (not variable in the arguments).
     */
    boolean isIstance();

    /**
     * Checks whether this molecule matches the provided one.
     *
     * @param mol
     *            the LsaMolecule to try to match with.
     * @return true if the two molecules match
     */
    boolean matches(ILsaMolecule mol);

    /**
     * Checks whether this molecule matches the provided list of arguments.
     *
     * @param mol
     *            the LsaMolecule to try to match with
     * @param duplicateVariables
     *            if true, the matching of variables reused within the same
     *            tuple is enabled
     * @return true if the two molecules match
     */
    boolean matches(List<? extends IExpression> mol, boolean duplicateVariables);

    /**
     * Checks whether this molecule is more generic than the provided one.
     *
     * @param mol
     *            the molecule to compare to
     * @return true if the molecule is more generic than the one passed
     */
    boolean moreGenericOf(ILsaMolecule mol);

    /**
     * Returns the number of arguments of this molecule.
     *
     * @return the number of arguments
     */
    int size();

    /**
     * Returns a {@link HashString} representation of this molecule.
     *
     * @return the string representing the molecule, in a faster implementation.
     */
    HashString toHashString();

    /**
     * Returns a string representation of this molecule.
     *
     * @return the string representing the molecule.
     */
    @Override
    String toString();

}
