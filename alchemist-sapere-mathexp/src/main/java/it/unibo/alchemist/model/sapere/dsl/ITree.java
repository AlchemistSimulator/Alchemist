/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.dsl;

import java.io.Serializable;
import java.util.Map;

import org.danilopianini.lang.HashString;


/**
 * 
 * Represents a Tree of Objects(Node) of generic type T. The Tree is represented
 * as a single rootElement which points to a List<Node<T>> of children. There is
 * no restriction on the number of children that a particular node may have.
 * This Tree provides a method to serialize the Tree into a List by doing a
 * pre-order traversal.
 * 
 */
public interface ITree extends Serializable {

    /**
     * This method substitutes variables present in matches map with their
     * values. It must involve only node of Typ=Var. The method must also
     * recognize the values type stored in the map (they can be Const or Num).
     * 
     * @param matches
     *            the map with variable values already assigned.
     * @return a new ITree containing the instantiated variable
     */
    ITree assignVarValue(Map<HashString, ITreeNode<?>> matches);

    /**
     * This evaluates the expression. If the matches map contains values which
     * are not instanced, and the expression value cannot consequently be
     * computed, 0d is returned.
     * 
     * @param matches
     *            the map that binds each variable with its own value
     * @return A number representing the value for this expression. If the
     *         expression can't be computed, NaN is returned.
     */
    double evaluation(Map<HashString, ITreeNode<?>> matches);

    /**
     * Return the root Node of the tree.
     * 
     * @return the root element.
     */
    ITreeNode<?> getRoot();

    /**
     * Similar to toString(), but returns a HashString.
     * 
     * @return a HashString representation of this Object
     */
    HashString toHashString();
}
