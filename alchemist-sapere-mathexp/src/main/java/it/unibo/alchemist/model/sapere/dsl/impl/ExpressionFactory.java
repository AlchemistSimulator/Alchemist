/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.dsl.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import org.danilopianini.lang.HashString;

/**
 * This utility class provides methods to ease the building and usage of
 * {@link IExpression} without parsing.
 * 
 */
public final class ExpressionFactory {

    /**
     * Given a single literal (either variable or constant), builds the
     * corresponding expression.
     * 
     * @param s
     *            MUST BE A SINGLE LITERAL
     * @return a new expression
     */
    public static IExpression buildComplexGroundExpression(final String s) {
        if (s.length() == 0) {
            throw new IllegalArgumentException("The given String can't be empty.");
        }
        return new Expression(new ConstTreeNode(new HashString(s)));
    }

    /**
     * Given a number, builds a numeric expression.
     * 
     * @param n
     *            a numeric value
     * @return a new expression
     */
    public static IExpression buildExpression(final double n) {
        return new Expression(new NumTreeNode(n));
    }

    /**
     * Given a single literal (either variable or constant), builds the
     * corresponding expression.
     * 
     * @param s
     *            MUST BE A SINGLE LITERAL
     * @return a new expression
     */
    public static IExpression buildExpression(final HashString s) {
        return new Expression(buildLiteralNode(s));
    }

    /**
     * Given a single literal (either variable or constant), builds the
     * corresponding expression.
     * 
     * @param s
     *            MUST BE A SINGLE LITERAL
     * @return a new expression
     */
    public static IExpression buildExpression(final String s) {
        return buildExpression(new HashString(s));
    }

    /**
     * Given a single literal (either variable or constant), builds the
     * corresponding {@link ITreeNode}.
     * 
     * @param s
     *            MUST BE A SINGLE LITERAL
     * @return a new {@link ITreeNode}
     */
    public static ITreeNode<?> buildLiteralNode(final HashString s) {
        if (s.length() == 0) {
            throw new IllegalArgumentException("The given String can't be empty.");
        }
        return Character.isUpperCase(s.charAt(0)) ? new VarTreeNode(s) : new ConstTreeNode(s);
    }

    /**
     * Given a double, creates a NumTreeNode and wraps it into a
     * {@link ListTreeNode}.
     * 
     * @param n
     *            the node to wrap
     * @return an IExpression containing the passed element in a set
     */
    public static IExpression wrap(final double n) {
        final Set<ITreeNode<?>> content = new LinkedHashSet<>();
        content.add(new NumTreeNode(n));
        return new Expression(new ListTreeNode(content));
    }

    /**
     * Given a {@link HashString}, creates the corresponding {@link ITreeNode}
     * and wraps it into a {@link ListTreeNode}.
     * 
     * @param s
     *            the node to wrap
     * @return an IExpression containing the passed element in a set
     */
    public static IExpression wrap(final HashString s) {
        final Set<ITreeNode<?>> content = new LinkedHashSet<>();
        content.add(buildLiteralNode(s));
        return new Expression(new ListTreeNode(content));
    }

    /**
     * Wraps a collection of {@link ITreeNode} into a new List IExpression.
     * 
     * @param nodes
     *            the nodes to wrap
     * @return an IExpression containing all the elements in a set
     */
    public static IExpression wrap(final Iterable<ITreeNode<?>> nodes) {
        final Set<ITreeNode<?>> content = new LinkedHashSet<>();
        for (final ITreeNode<?> node : nodes) {
            content.add(node);
        }
        return new Expression(new ListTreeNode(content));
    }

    /**
     * Given a node, wraps it into a {@link ListTreeNode}.
     * 
     * @param node
     *            the node to wrap
     * @return an IExpression containing the passed element in a set
     */
    public static IExpression wrap(final ITreeNode<?> node) {
        final Set<ITreeNode<?>> content = new LinkedHashSet<>();
        content.add(node);
        return new Expression(new ListTreeNode(content));
    }

    private ExpressionFactory() {
    }

}
