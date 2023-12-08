/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.dsl.impl;

import java.util.Map;

import org.danilopianini.lang.HashString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.model.sapere.dsl.ITreeNode;


/**
 */
public class OperatorTreeNode extends ATreeNode<Double> {

    private static final long serialVersionUID = 4946572123219713415L;
    private static final Logger L = LoggerFactory.getLogger(OperatorTreeNode.class);

    private final Operator operator;

    /**
     * @param op
     *            the operator
     * @param left
     *            left side of the expression
     * @param right
     *            right side of the expression
     */
    public OperatorTreeNode(final Operator op, final ITreeNode<?> left, final ITreeNode<?> right) {
        super(0d, left, right);
        this.operator = op;
    }

    /**
     * @return the operator in use
     */
    public Operator getOperator() {
        return operator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.interfaces.ITreeNode#getType()
     */
    @Override
    public Type getType() {
        return Type.OPERATOR;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * alice.alchemist.expressions.implementations.ATreeNode#getValue(java.util
     * .Map)
     */
    @Override
    public Double getValue(final Map<HashString, ITreeNode<?>> matches) {
        final ITreeNode<?> child = getLeftChild();
        /*
         * Operation on Lists
         */
        if (child.getType().equals(Type.LIST)) {
            final ListTreeNode son = (ListTreeNode) child;
            switch (operator) {
            case PLUS:
                return null;
            case MINUS:
                return null;
            case MIN:
                Double min = Double.POSITIVE_INFINITY;
                for (final ITreeNode<?> el : son.getData()) {
                    if (el instanceof NumTreeNode) {
                        final NumTreeNode numel = (NumTreeNode) el;
                        if (numel.getData() < min) {
                            min = numel.getData();
                        }
                    }
                }
                return min;
            case MAX:
                Double max = Double.NEGATIVE_INFINITY;
                for (final ITreeNode<?> el : son.getData()) {
                    if (el instanceof NumTreeNode) {
                        final NumTreeNode numel = (NumTreeNode) el;
                        if (numel.getData() > max) {
                            max = numel.getData();
                        }
                    }
                }
                return max;
            default:
                return Double.NaN;
            }
            /*
             * Operation on Numbers
             */
        } else {
            /*
             * Unary
             */
            if (operator == Operator.MOD) {
                return Math.abs((Double) child.getValue(matches));
                /*
                 * Binary
                 */
            } else {
                final Double leftVal = computeVal(child, matches);
                final Double rightVal = computeVal(getRightChild(), matches);
                switch (operator) {
                case PLUS:
                    return leftVal + rightVal;
                case MINUS:
                    return leftVal - rightVal;
                case TIMES:
                    return leftVal * rightVal;
                case DIV:
                    return leftVal / rightVal;
                case MIN:
                    return Math.min(leftVal, rightVal);
                case MAX:
                    return Math.max(leftVal, rightVal);
                default:
                    return Double.NaN;
                }
            }
        }
    }

    private static Double computeVal(final ITreeNode<?> child, final Map<HashString, ITreeNode<?>> matches) {
        switch (child.getType()) {
        case VAR:
            return (Double) ((ITreeNode<?>) child.getValue(matches)).getData();
        case NUM:
        case OPERATOR:
            return (Double) child.getValue(matches);
        default:
            L.error("ERROR: unexpected type " + child.getType());
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.implementations.ATreeNode#toString()
     */
    @Override
    public String toString() {
        switch (getNumberOfChildren()) {
        case 0:
            return operator.toString();
        case 1:
            switch (operator) {
            case MAX:
            case MIN:
                return operator + "(" + getLeftChild() + ")";
            case MOD:
                return "|" + getLeftChild() + "|";
            default:
                return "error";
            }
        case 2:
            switch (operator) {
            case ADD:
            case DEL:
                return operator + " " + getLeftChild() + " from " + getRightChild();
            case DIV:
            case PLUS:
            case TIMES:
            case MINUS:
                return getLeftChild().toString() + operator + getRightChild();
            case MAX:
            case MIN:
                return operator + "(" + getLeftChild() + "," + getRightChild() + ")";
            default:
                return "error";
            }
        default:
            return "error";
        }
    }

}
