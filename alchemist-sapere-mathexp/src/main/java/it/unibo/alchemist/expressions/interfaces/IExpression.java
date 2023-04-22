/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.expressions.interfaces;

import it.unibo.alchemist.expressions.implementations.Type;

import java.io.Serializable;
import java.util.Map;

import org.danilopianini.lang.HashString;


public interface IExpression extends Serializable, Cloneable {

    /**
     * @param map
     *            the map of the matches. If no matches are available or
     *            required, null values are accepted.
     * @return The evaluation of the expression
     */
    ITreeNode<?> calculate(Map<HashString, ITreeNode<?>> map);

    /**
     * @return the ast representing the IExpression
     */
    ITree getAST();

    /**
     * @return the left children of the root element.
     */
    ITreeNode<?> getLeftChildren();

    /**
     * @return the right children of the root element.
     */
    ITreeNode<?> getRightChildren();

    /**
     * @return the root note of the AST of this expression
     */
    ITreeNode<?> getRootNode();

    /**
     * @return the first AST Node Data.
     */
    Object getRootNodeData();

    /**
     * @return the first AST Node Data.
     */
    Type getRootNodeType();

    /**
     * 
     * Tries to match this expression with expr. The matching rules are: (i) a
     * variable matches everything; (ii) a constant value matches an identical
     * constant value; (iii) a number matches an identical number or an
     * operator, (iv) operators match everything but constants, (v) comparators
     * match numbers and operators (verifying the values); (vi) expr type can't
     * be comparator; (vii) add and rem operators work only with lists.
     * 
     * @param expr
     *            the expression to match
     * @param map
     *            the matches map
     * @return true if this expression can "match" with expr.
     */
    boolean matches(IExpression expr, Map<HashString, ITreeNode<?>> map);

    /**
     * This match method test whether or not two expressions might match. It can
     * be used to evaluate dependencies in a general fashion, it does not check
     * if all the relations are satisfied (e.g. if applying the comparators) but
     * makes a sort of "type checking". If you want to compare two templates,
     * this is the way to go.
     * 
     * @param expr
     *            the expression to verify the possibility of future matches
     * @return true if this expression can "match" with expr.
     */
    boolean mayMatch(IExpression expr);

    /**
     * Runs a syntactic match against the e.
     * 
     * @param e
     *            the expression to match with
     * @return true if the passed IExpression matches this one
     */
    boolean syntacticMatch(IExpression e);

    // void setVarMap(Map<HashString, IExpression> map);

    /**
     * @param map
     *            the matches map
     * @return the new ast with variable instantiated.
     */
    ITree updateMatchedVar(Map<HashString, ITreeNode<?>> map);

}
