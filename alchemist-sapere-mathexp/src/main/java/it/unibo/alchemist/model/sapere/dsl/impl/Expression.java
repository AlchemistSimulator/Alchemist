/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.dsl.impl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import it.unibo.alchemist.model.sapere.dsl.parser.ParseException;
import org.danilopianini.lang.HashString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.ITree;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import it.unibo.alchemist.model.sapere.dsl.parser.Exp;

/**
 */
@SuppressFBWarnings
public final class Expression implements IExpression {

    private static final long serialVersionUID = 3443642011985784643L;

    private static final HashString
        EQUALS = new HashString("="),
        NOT_EQUALS = new HashString("!="),
        GREATER = new HashString(">"),
        GREATER_EQUALS = new HashString(">="),
        SMALLER = new HashString("<"),
        SMALLER_EQUALS = new HashString("<=");
    private static final Logger L = LoggerFactory.getLogger(Expression.class);

    private final ITree ast;
    private final Object astData;
    private final Type astType;
    private final ITreeNode<?> rootNode;
    private final HashString syntactic;

    private static boolean comparatorVsConst(final IExpression comparator, final IExpression constant, final Map<HashString, ITreeNode<?>> matches) {
        if (comparator.getRootNodeData().equals(EQUALS)) {
            return constant.getRootNodeData().equals(comparator.getRightChildren().getValue(matches));
        }
        if (comparator.getRootNodeData().equals(NOT_EQUALS)) {
            return !(constant.getRootNodeData().equals(comparator.getRightChildren().getValue(matches)));
        }
        L.warn("invalid comparation between comparator and constant (only = or != are admitted)");
        return false;
    }

    private static boolean listComparatorVsList(final IExpression comparator, final ITreeNode<?> list) {
        final Set<ITreeNode<?>> toMatch = ((ListTreeNode) list).getData();
        if (comparator.getRootNodeData().equals(ListComparator.EMPTY)) {
            return toMatch.isEmpty();
        }
        if (comparator.getRootNodeData().equals(ListComparator.NOT_EMPTY)) {
            return !toMatch.isEmpty();
        }
        if (comparator.getLeftChildren().getType().equals(Type.LIST) || comparator.getLeftChildren().getType().equals(Type.VAR)) {
            final Set<ITreeNode<?>> included = ((ListTreeNode) comparator.getRightChildren()).getData();
            if (comparator.getRootNodeData().equals(ListComparator.HAS)) {
                return matchesAll(toMatch, included);
            }
            if (comparator.getRootNodeData().equals(ListComparator.HAS_NOT)) {
                return !matchesAll(toMatch, included);
            }
        }
        return false;
    }

    private static boolean listVsList(final ITreeNode<?> root, final ITreeNode<?> expr) {
        final ITreeNode<Set<ITreeNode<?>>> list = (ListTreeNode) root;
        final Set<ITreeNode<?>> molList = list.getData();
        final Set<ITreeNode<?>> exprList = ((ListTreeNode) expr).getData();
        if (molList.size() == exprList.size()) {
            final Set<ITreeNode<?>> molListCpy = new HashSet<>();
            int varCount = 0;
            for (final ITreeNode<?> el : molList) {
                if (el.getType().equals(Type.VAR)) {
                    varCount++;
                } else {
                    molListCpy.add(el);
                }
            }
            if (varCount >= molList.size()) {
                return true;
            }
            final Set<ITreeNode<?>> expListCpy = new HashSet<>();
            for (final ITreeNode<?> el : exprList) {
                if (el.getType().equals(Type.VAR)) {
                    varCount++;
                } else {
                    expListCpy.add(el);
                }
            }
            if (varCount >= molList.size()) {
                return true;
            }
            for (final ITreeNode<?> cmp : molListCpy) {
                if (expListCpy.contains(cmp)) {
                    expListCpy.remove(cmp);
                } else {
                    varCount--;
                }
                if (varCount < 0) {
                    return false;
                }
            }
            for (final ITreeNode<?> cmp : expListCpy) {
                if (molListCpy.contains(cmp)) {
                    molListCpy.remove(cmp);
                } else {
                    varCount--;
                }
                if (varCount < 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean matchesAll(final Set<ITreeNode<?>> bigger, final Set<ITreeNode<?>> contained) {
        /*
         * Something still smells here. But cannot find a non-working case.
         */
        if (bigger.size() < contained.size()) {
            return false;
        }
        for (final ITreeNode<?> el : contained) {
            boolean matched = false;
            for (final ITreeNode<?> node : bigger) {
                if (matchesNodes(node, el)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesNodes(final ITreeNode<?> root, final ITreeNode<?> expr) {
        switch (root.getType()) {
        case VAR:
            switch (expr.getType()) {
            case COMPARATOR:
            case LISTCOMPARATOR:
                printErrorStaticMatches(expr.getType());
                return false;
            default:
                return true;
            }
            /*
             * It should never match a listComparator or a comparator, since
             * those can't be in the LSA Spaces
             */
        case LIST:
            switch (expr.getType()) {
            case VAR:
                return true;
            case LIST:
                return listVsList(root, expr);
            default:
                printErrorStaticMatches(expr.getType());
                return false;
            }
        case NUM:
            switch (expr.getType()) {
            case NUM:
                return expr.getData().equals(root.getData());
            case VAR:
                return true;
            default:
                printErrorStaticMatches(expr.getType());
                return false;
            }
        case CONST:
            switch (expr.getType()) {
            case VAR:
                return true;
            case CONST:
                return root.getData().equals(expr.getData());
            default:
                printErrorStaticMatches(expr.getType());
                return false;
            }
        default:
            printErrorStaticMatches(root.getType());
        }
        return false;
    }

    private static boolean numVsComparator(final IExpression num, final IExpression comp, final Map<HashString, ITreeNode<?>> matches) {
        final Object d = comp.getRootNodeData();
        final Object valObj = comp.getRightChildren().getValue(matches);
        final Double val;
        if (valObj instanceof Double) {
            /*
             * The right child is an ITreeNode<Double>
             */
            val = (Double) valObj;
        } else {
            /*
             * The right child is an ITreeNode<ITreeNode<?>>, namely a Variable
             */
            val = (Double) ((ITreeNode<?>) valObj).getValue(matches);
        }
        if (d.equals(GREATER)) {
            return (Double) num.getRootNodeData() > val;
        }
        if (d.equals(SMALLER)) {
            return (Double) num.getRootNodeData() < val;
        }
        if (d.equals(EQUALS)) {
            return num.getRootNodeData().equals(val);
        }
        if (d.equals(NOT_EQUALS)) {
            return !(num.getRootNodeData().equals(val));
        }
        if (d.equals(SMALLER_EQUALS)) {
            return (Double) num.getRootNodeData() <= val;
        }
        if (d.equals(GREATER_EQUALS)) {
            return (Double) num.getRootNodeData() >= val;
        } else {
            L.error("You must have built something which should not exist: " + comp);
        }
        return false;
    }

    private static boolean numVsOperator(final IExpression num, final IExpression op, final Map<HashString, ITreeNode<?>> matches) {
        return num.getRootNodeData().equals(op.getAST().evaluation(matches));
    }

    private static boolean operatorVsList(final IExpression operator, final IExpression list) {
        final OperatorTreeNode root = (OperatorTreeNode) operator.getAST().getRoot();
        final Set<ITreeNode<?>> listExpr = ((ListTreeNode) list.getAST().getRoot()).getData();
        final Set<ITreeNode<?>> listTemp = new LinkedHashSet<>(((ListTreeNode) operator.getRightChildren()).getData());
        if (root.getOperator().equals(Operator.ADD)) {
            listTemp.add(operator.getLeftChildren());
            /*
             * Order is irrelevant.
             */
            if (listTemp.size() == listExpr.size()) {
                for (final ITreeNode<?> el : listExpr) {
                    if (!listTemp.contains(el)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } else if (root.getOperator().equals(Operator.DEL)) {
            if (!listTemp.remove(operator.getLeftChildren())) {
                return false;
            }
            if (listTemp.size() == listExpr.size()) {
                for (final ITreeNode<?> el : listExpr) {
                    if (!listTemp.contains(el)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private static void printErrorStaticMatches(final Type t) {
        L.error(t + "s not allowed in the static version of matches.");
    }

    private static void printWarnMessage(final IExpression root, final IExpression expr) {
        L.warn("This should never happen. Check what you did: you tried to match " + root + " and " + expr);
    }

    /**
     * This constructor does not do any parsing, and thus is much faster than
     * the other one.
     * 
     * @param tree
     *            the ITree which represents this Expression
     */
    public Expression(final ITree tree) {
        ast = tree;
        rootNode = ast.getRoot();
        astType = rootNode.getType();
        astData = rootNode.getData();
        syntactic = ast.toHashString();
    }

    /**
     * @param n
     *            the root node of the expression
     */
    public Expression(final ITreeNode<?> n) {
        this(new AST(n));
    }

    /**
     * This constructor parses the String into an Expression. Due to the parsing
     * operation, it is slow compared to the other one, and should be used only
     * in the first initialization phase.
     * 
     * @param s
     *            The String representing the expression
     */
    public Expression(final String s) {
        Exp parser;
        parser = new Exp(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
        try {
            ast = parser.Init();
            rootNode = ast.getRoot();
            astType = rootNode.getType();
            astData = rootNode.getData();
            syntactic = ast.toHashString();
        } catch (ParseException e) {
            throw new IllegalStateException("Unable to parse LSA <" + s + ">", e);
        }
    }

    @Override
    public ITreeNode<?> calculate(final Map<HashString, ITreeNode<?>> map) {
        final Double val = ast.evaluation(map);
        if (val.equals(Double.NaN)) {
            return ast.assignVarValue(map).getRoot();
        }
        return new NumTreeNode(val);
    }

    @Override
    public ITree getAST() {
        return ast;
    }

    @Override
    public ITreeNode<?> getLeftChildren() {
        return getAST().getRoot().getLeftChild();

    }

    @Override
    public ITreeNode<?> getRightChildren() {
        return getAST().getRoot().getRightChild();

    }

    @Override
    public ITreeNode<?> getRootNode() {
        return rootNode;
    }

    @Override
    public Object getRootNodeData() {
        return astData;
    }

    @Override
    public Type getRootNodeType() {
        return astType;
    }

    @Override
    @SuppressFBWarnings(justification = "I need exact equality")
    public boolean matches(final IExpression expr, final Map<HashString, ITreeNode<?>> matches) {
        switch (getRootNodeType()) {
        case VAR:
            switch (expr.getRootNodeType()) {
            case COMPARATOR:
            case LISTCOMPARATOR:
                printWarnMessage(this, expr);
                return false;
            default:
                return true;
            }
            /*
             * It should never match a listComparator or a comparator, since
             * those can't be in the LSA Spaces
             */
        case LIST:
            switch (expr.getRootNodeType()) {
            case VAR:
                return true;
            case LIST:
                return listVsList(rootNode, expr.getRootNode());
            case LISTCOMPARATOR:
                return listComparatorVsList(expr, getRootNode());
            case OPERATOR:
                return operatorVsList(expr, this);
            default:
                return false;
            }
        case NUM:
            switch (expr.getRootNodeType()) {
            case OPERATOR:
                return numVsOperator(this, expr, matches);
            case NUM:
                return expr.getRootNodeData().equals(getRootNodeData());
            case VAR:
                return true;
            case COMPARATOR:
                return numVsComparator(this, expr, matches);
            default:
                return false;
            }
        case CONST:
            switch (expr.getRootNodeType()) {
            case VAR:
                return true;
            case CONST:
                return getRootNodeData().equals(expr.getRootNodeData());
            case COMPARATOR:
                return comparatorVsConst(expr, this, matches);
            default:
                return false;
            }
        case OPERATOR:
            switch (expr.getRootNodeType()) {
            case VAR:
                return true;
            case CONST:
                return false;
            case NUM:
                return numVsOperator(expr, this, matches);
            case OPERATOR:
                return ast.evaluation(matches) == expr.getAST().evaluation(matches);
            case COMPARATOR:
                return numVsComparator(new Expression(new NumTreeNode(ast.evaluation(matches))), expr, matches);
            case LIST:
                return operatorVsList(this, expr);
            case LISTCOMPARATOR:
                printWarnMessage(this, expr);
            default:
                return false;
            }
        case COMPARATOR:
            switch (expr.getRootNodeType()) {
            case OPERATOR:
                return numVsComparator(new Expression(new NumTreeNode(expr.getAST().evaluation(matches))), this, matches);
            case NUM:
                return numVsComparator(expr, this, matches);
            case CONST:
                return comparatorVsConst(this, expr, matches);
            case COMPARATOR:
            case VAR:
            case LISTCOMPARATOR:
                printWarnMessage(this, expr);
            default:
                return false;

            }
        case LISTCOMPARATOR:
            switch (expr.getRootNodeType()) {
            case LIST:
                return listComparatorVsList(this, expr.getRootNode());
            case COMPARATOR:
            case OPERATOR:
            case LISTCOMPARATOR:
            case VAR:
                printWarnMessage(this, expr);
            default:
                return false;
            }

        default:
            L.error("Unable to compare " + this + " to " + expr);
        }
        return false;
    }

    @Override
    public boolean mayMatch(final IExpression expr) {
        final Type target = expr.getRootNodeType();
        if (target.equals(Type.VAR)) {
            return true;
        }
        switch (getRootNodeType()) {
        case VAR:
            return true;
        case LIST:
            return target == Type.LIST || target == Type.LISTCOMPARATOR;
        case NUM:
            return target == Type.OPERATOR || expr.getRootNodeData().equals(getRootNodeData()) || target == Type.COMPARATOR;
        case CONST:
            return expr.getRootNodeData().equals(getRootNodeData());
        case OPERATOR:
            return target != Type.CONST;
        case COMPARATOR:
            switch (target) {
            case OPERATOR:
                return getRightChildren().getType() == Type.NUM || getRightChildren().getType() == Type.VAR || getRightChildren().getType() == Type.OPERATOR;
            case NUM:
                return getRightChildren().getType() == Type.NUM || getRightChildren().getType() == Type.VAR || getRightChildren().getType() == Type.OPERATOR;
            case COMPARATOR:
                if (getRightChildren().getType() == Type.NUM || getRightChildren().getType() == Type.OPERATOR) {
                    if (expr.getRightChildren().getType() == Type.NUM || expr.getRightChildren().getType() == Type.OPERATOR) {
                        return true;
                    }
                } else if (getRightChildren().getType() == Type.CONST && expr.getRightChildren().getType() == Type.CONST) {
                    return true;
                } else {
                    return getRightChildren().getType() == Type.VAR || expr.getRightChildren().getType() == Type.VAR;
                }
            case CONST:
                return getRightChildren().getType() == Type.CONST || getRightChildren().getType() == Type.VAR;
            default:
                L.error("ERROR with comparator: " + this + " -- " + expr);
            }
        case LISTCOMPARATOR:
            switch (target) {
            case LIST:
                return true;
            case LISTCOMPARATOR:
                return true;
            default:
                return false;
            }
        default:
            L.error("ERROR, unable to compare " + this + " to " + expr);
            return false;
        }
    }

    @Override
    public boolean syntacticMatch(final IExpression e) {
        return syntactic.equals(((Expression) e).syntactic);
    }

    @Override
    public String toString() {
        return ast.toString();
    }

    @Override
    public ITree updateMatchedVar(final Map<HashString, ITreeNode<?>> matches) {
        switch (astType) {
        case CONST:
        case NUM:
            return ast;
        default:
            ITree astModified = ast.assignVarValue(matches);
            final ITreeNode<?> elem = astModified.getRoot();
            if (elem.getType() == Type.OPERATOR) {
                final OperatorTreeNode op = (OperatorTreeNode) elem;
                if (op.getOperator().equals(Operator.ADD)) {
                    final ITreeNode<Set<ITreeNode<?>>> listNode = (ListTreeNode) elem.getLeftChild();
                    final Set<ITreeNode<?>> listVal = listNode.getData();
                    final ITreeNode<?> addElem = elem.getRightChild();
                    /*
                     * If the elem to add is a list, unwrap it inside the other.
                     * Else, just add the element.
                     */
                    if (addElem instanceof ListTreeNode) {
                        listVal.addAll(((ListTreeNode) addElem).getData());
                    } else {
                        listVal.add(addElem);
                    }
                    astModified = new AST(new ListTreeNode(listVal));
                } else if (op.getOperator().equals(Operator.DEL)) {
                    final ITreeNode<Set<ITreeNode<?>>> listNode = (ListTreeNode) elem.getRightChild();
                    final Set<ITreeNode<?>> listVal = listNode.getData();
                    final ITreeNode<?> delElem = astModified.getRoot().getLeftChild();
                    /*
                     * If the elem to delete is a list, remove every element in
                     * common. Else, just remove the single element.
                     */
                    if (delElem instanceof ListTreeNode) {
                        listVal.removeAll(((ListTreeNode) delElem).getData());
                    } else {
                        listVal.remove(delElem);
                    }
                    astModified = new AST(new ListTreeNode(listVal));
                } else {
                    final Double res = astModified.evaluation(matches);
                    astModified = new AST(new NumTreeNode(res));
                }
            }
            return astModified;
        }
    }

}
