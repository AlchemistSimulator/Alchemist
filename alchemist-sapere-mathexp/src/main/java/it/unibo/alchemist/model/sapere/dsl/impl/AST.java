/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.dsl.impl;

import it.unibo.alchemist.model.sapere.dsl.ITree;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.danilopianini.lang.HashString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public final class AST implements ITree {
    private static final long serialVersionUID = 5276224537064582492L;
    private static final Logger L = LoggerFactory.getLogger(AST.class);

    private final HashString fs;
    private final ITreeNode<?> rootElement;

    private static ITreeNode<?> assign(final ITreeNode<?> node, final Map<HashString, ITreeNode<?>> matches) {
        if (node.getNumberOfChildren() == 0) {
            final Type t = node.getType();
            final ITreeNode<?> e = matches.get(node.toHashString());
            if (t.equals(Type.VAR) && e != null) {
                switch (e.getType()) {
                case LIST:
                    final Set<ITreeNode<?>> backup = new LinkedHashSet<>();
                    for (final ITreeNode<?> o : (ListTreeNode) e) {
                        backup.add(o);
                    }
                    return new ListTreeNode(backup);
                case COMPARATOR:
                case LISTCOMPARATOR:
                case OPERATOR:
                    L.error("unexpected node of type: {}", e.getType());
                    return null;
                default:
                    return e;
                }
            } else if (t.equals(Type.LIST)) {
                final Set<ITreeNode<?>> l = new LinkedHashSet<>();
                unwrapLists(l, (ListTreeNode) node, matches);
                return new ListTreeNode(l);
            }
        } else if (node.getNumberOfChildren() == 1) {
            final ITreeNode<?> root;
            final ITreeNode<?> left = assign(node.getLeftChild(), matches);
            switch (node.getType()) {
            case COMPARATOR:
                root = null;
                L.error("Comparator with single children. Something bad happened.");
                break;
            case LISTCOMPARATOR:
                root = new ListComparatorTreeNode(((ListComparatorTreeNode) node).getData(), left, null);
                break;
            case OPERATOR:
                root = new OperatorTreeNode(((OperatorTreeNode) node).getOperator(), left, null);
                break;
            case CONST:
                root = null;
                L.error("Const with children. Something bad happened.");
                break;
            case LIST:
                root = null;
                L.error("List with children. Something bad happened.");
                break;
            case NUM:
                root = null;
                L.error("Num with children. Something bad happened.");
                break;
            case VAR:
                root = null;
                L.error("Var with children. Something bad happened.");
                break;
            default:
                root = null;
                L.error("Something extremely nasty happened.");
                break;
            }
            return root;
        } else {
            final ITreeNode<?> root;
            final ITreeNode<?> left = assign(node.getLeftChild(), matches);
            final ITreeNode<?> right = assign(node.getRightChild(), matches);
            switch (node.getType()) {
            case COMPARATOR:
                root = new ComparatorTreeNode((HashString) node.getData(), left, right);
                break;
            case LISTCOMPARATOR:
                root = new ListComparatorTreeNode(((ListComparatorTreeNode) node).getData(), left, right);
                break;
            case OPERATOR:
                root = new OperatorTreeNode(((OperatorTreeNode) node).getOperator(), left, right);
                break;
            case CONST:
                root = null;
                L.error("Const with children. Something bad happened.");
                break;
            case LIST:
                root = null;
                L.error("List with children. Something bad happened.");
                break;
            case NUM:
                root = null;
                L.error("Num with children. Something bad happened.");
                break;
            case VAR:
                root = null;
                L.error("Var with children. Something bad happened.");
                break;
            default:
                root = null;
                L.error("Something extremely nasty happened.");
                break;
            }
            return root;
        }
        return node;
    }

    private static void unwrapLists(final Set<ITreeNode<?>> l, final ListTreeNode node, final Map<HashString, ITreeNode<?>> matches) {
        for (final ITreeNode<?> tree : node.getData()) {
            final ITreeNode<?> instance = assign(tree, matches);
            if (instance.getType().equals(Type.LIST)) {
                /*
                 * Lists of lists are not allowed
                 */
                l.addAll(((ListTreeNode) instance).getData());
            } else {
                l.add(instance);
            }
        }
    }

    /**
     * Builds a new AST given its root node.
     * 
     * @param root
     *            the root node
     */
    public AST(final ITreeNode<?> root) {
        rootElement = root;
        fs = root.toHashString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * alice.alchemist.expressions.interfaces.ITree#assignVarValue(java.util
     * .Map)
     */
    @Override
    public AST assignVarValue(final Map<HashString, ITreeNode<?>> matches) {
        return new AST(assign(rootElement, matches));
    }

    /**
     * This evaluates the expression. If the matches map contains values which
     * are not instanced, and the expression value cannot consequently be
     * computed, 0d is returned.
     * 
     * @param matches
     *            the map that binds each variable with its own value
     * @return A number representing the value for this expression. If the
     *         expression can't be computed, an exception is thrown.
     */
    @Override
    public double evaluation(final Map<HashString, ITreeNode<?>> matches) {
        final Object res = getRoot().getValue(matches);
        if (res instanceof Double) {
            return (Double) res;
        } else {
            return Double.NaN;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.interfaces.MathExpression.ITree#getRootElement()
     */
    @Override
    public ITreeNode<?> getRoot() {
        return this.rootElement;
    }

    @Override
    public HashString toHashString() {
        return fs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return fs.toString();
    }

}
