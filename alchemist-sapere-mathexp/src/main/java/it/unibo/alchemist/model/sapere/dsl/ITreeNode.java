/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.dsl;

import it.unibo.alchemist.model.sapere.dsl.impl.Type;

import java.io.Serializable;
import java.util.Map;

import org.danilopianini.lang.HashString;


/**
 * Represents an interface for node of the Tree class.
 * 
 * @param <T> concentration type
 */
public interface ITreeNode<T> extends Serializable {

    /**
     * @return the object embedded in this node
     */
    T getData();

    /**
     * @return the left child (if any)
     */
    ITreeNode<?> getLeftChild();

    /**
     * @return the number of first level children.
     */
    int getNumberOfChildren();

    /**
     * @return the righr child (if any)
     */
    ITreeNode<?> getRightChild();

    /**
     * @return the type of this node
     */
    Type getType();

    /**
     * @param mp
     *            the matches map. If it is not available or not required (e.g.
     *            for evaluating a number or a const), then null values can be
     *            used
     * @return the value of the Node. (Double if it's a Num or a Var of type
     *         Num, String if it's a Const or a Var of type Const) the method
     *         return null if there are some variable not yet allocated in the
     *         expression.
     * 
     */
    T getValue(Map<HashString, ITreeNode<?>> mp);

    /**
     * Similar to toString(), but returns a HashString.
     * 
     * @return a HashString representation of this Object
     */
    HashString toHashString();

    /**
     * @return a String representation of this Object
     */
    @Override
    String toString();

}
