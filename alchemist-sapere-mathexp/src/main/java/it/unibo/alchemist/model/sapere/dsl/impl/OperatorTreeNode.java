/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.dsl.impl;

import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import org.danilopianini.lang.HashString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 */
public class OperatorTreeNode extends ATreeNode<Double> {

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
            case PLUS, MINUS:
                return null;
                case MIN:
                Double min = Double.POSITIVE_INFINITY;
                for (final ITreeNode<?> el : son.getData()) {
                    if (el instanceof final NumTreeNode numel) {
                        if (numel.getData() < min) {
                            min = numel.getData();
                        }
                    }
                }
                return min;
            case MAX:
                Double max = Double.NEGATIVE_INFINITY;
                for (final ITreeNode<?> el : son.getData()) {
                    if (el instanceof final NumTreeNode numel) {
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
                return switch (operator) {
                    case PLUS -> leftVal + rightVal;
                    case MINUS -> leftVal - rightVal;
                    case TIMES -> leftVal * rightVal;
                    case DIV -> leftVal / rightVal;
                    case MIN -> Math.min(leftVal, rightVal);
                    case MAX -> Math.max(leftVal, rightVal);
                    default -> Double.NaN;
                };
            }
        }
    }

    private static Double computeVal(final ITreeNode<?> child, final Map<HashString, ITreeNode<?>> matches) {
        return switch (child.getType()) {
            case VAR -> (Double) ((ITreeNode<?>) child.getValue(matches)).getData();
            case NUM, OPERATOR -> (Double) child.getValue(matches);
            default -> {
                L.error("ERROR: unexpected type {}", child.getType());
                yield null;
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.implementations.ATreeNode#toString()
     */
    @Override
    public String toString() {
        return switch (getNumberOfChildren()) {
            case 0 -> operator.toString();
            case 1 -> switch (operator) {
                case MAX, MIN -> operator + "(" + getLeftChild() + ")";
                case MOD -> "|" + getLeftChild() + "|";
                default -> "error";
            };
            case 2 -> switch (operator) {
                case ADD, DEL -> operator + " " + getLeftChild() + " from " + getRightChild();
                case DIV, PLUS, TIMES, MINUS -> getLeftChild().toString() + operator + getRightChild();
                case MAX, MIN -> operator + "(" + getLeftChild() + "," + getRightChild() + ")";
                default -> "error";
            };
            default -> "error";
        };
    }

}
