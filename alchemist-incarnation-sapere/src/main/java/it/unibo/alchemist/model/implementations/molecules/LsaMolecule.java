/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.molecules;

import it.unibo.alchemist.expressions.implementations.Expression;
import it.unibo.alchemist.expressions.implementations.ExpressionFactory;
import it.unibo.alchemist.expressions.implementations.ListTreeNode;
import it.unibo.alchemist.expressions.implementations.Type;
import it.unibo.alchemist.expressions.implementations.VarTreeNode;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.Dependency;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import org.danilopianini.lang.HashString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *         This class realizes an LsaMolecule, where arguments are of type
 *         Expression.
 * 
 */
public final class LsaMolecule extends SimpleMolecule implements ILsaMolecule {

    private static final String OPEN_SYMBOL = "<", CLOSE_SYMBOL = ">", SEPARATOR = ", ";
    private static final long serialVersionUID = -2727376723102146271L;
    private static final Map<HashString, ITreeNode<?>> SMAP =
            Collections.unmodifiableMap(new LinkedHashMap<>(0, 1f));
    /**
     * Synthetic property representing the distance.
     */
    public static final HashString SYN_D = new HashString("#D");
    /**
     * Synthetic property representing an LSA ID.
     */
    public static final HashString SYN_MOL_ID = new HashString("#ID");
    /**
     * Synthetic property representing the current neighborhood.
     */
    public static final HashString SYN_NEIGH = new HashString("#NEIGH");
    /**
     * Synthetic property representing the local node id.
     */
    public static final HashString SYN_NODE_ID = new HashString("#NODE");
    /**
     * Synthetic property representing the orientation.
     */
    public static final HashString SYN_O = new HashString("#O");
    /**
     * Synthetic property representing a random value.
     */
    public static final HashString SYN_RAND = new HashString("#RANDOM");
    /**
     * Synthetic property representing the distance. If the environment does not
     * support route computation, it falls back to SYN_D.
     */
    public static final HashString SYN_ROUTE = new HashString("#ROUTE");
    /**
     * Synthetic property representing the current selected neighbor ("+"
     * conditions on the left).
     */
    public static final HashString SYN_SELECTED = new HashString("#SELECTEDNEIGH");
    /**
     * Synthetic property representing the current simulation time.
     */
    public static final HashString SYN_T = new HashString("#T");

    private final List<IExpression> args;
    private final boolean duplicateVars, instance;
    @Nullable
    private HashString repr;

    /**
     * Empty molecule, no arguments.
     */
    public LsaMolecule() {
        this(new ArrayList<>(0));
    }

    /**
     * Builds a new LsaMolecule by interpreting a list of IExpressions.
     * Dramatically faster than parsing, slower than copy.
     * 
     * @param listArgs
     *            the list of IExpressions
     */
    public LsaMolecule(@Nonnull final List<IExpression> listArgs) {
        this(listArgs, buildString(listArgs));
    }

    private LsaMolecule(final List<IExpression> listArgs, @Nonnull final HashString hash) {
        this(listArgs, hash, selfVariableUsed(listArgs), computeInstance(listArgs));
    }

    private LsaMolecule(
            final List<IExpression> argumentList,
            @Nonnull final HashString hash,
            final boolean duplicateVariables,
            final boolean isInstance
    ) {
        super(hash);
        this.repr = hash;
        args = Collections.unmodifiableList(argumentList);
        duplicateVars = duplicateVariables;
        instance = isInstance;
    }

    /**
     * Very fast constructor, produces a copy of an LsaMolecule. Use whenever
     * possible.
     * 
     * @param m
     *            the LsaMolecule to copy
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public LsaMolecule(final LsaMolecule m) {
        this(m.args, m.toHashString(), m.duplicateVars, m.instance);
    }

    /**
     * Builds a LsaMolecule by parsing the passed String. Slow, use only if
     * strictly needed.
     * 
     * @param argsString
     *            the String to parse
     */
    public LsaMolecule(final String argsString) {
        this(argsString, null);
    }

    /**
     * Builds a LsaMolecule by parsing the passed String. Slow, use only if
     * strictly needed.
     * 
     * @param argsString
     *            the String to parse
     * @param description
     *            a String to append at the end of the LSA. This is a special
     *            item, and can carry any type of String. It will be treated
     *            internally as a single literal or variable
     */
    public LsaMolecule(final String argsString, final String description) {
        super(buildString(buildArgsDesc(argsString, description)));
        args = Collections.unmodifiableList(buildArgsDesc(argsString, description));
        duplicateVars = selfVariableUsed(args);
        instance = computeInstance(args);
    }

    @Override
    public List<IExpression> allocateVar(final Map<HashString, ITreeNode<?>> matches) {
        if (matches == null) {
            return new ArrayList<>(args);
        }
        final List<IExpression> l = new ArrayList<>(args.size());
        for (final IExpression arg : args) {
            /*
             * Try to instance every part of the molecule
             */
            l.add(new Expression(arg.updateMatchedVar(matches)));
        }
        return l;
    }

    @Override
    public int argsNumber() {
        return args.size();
    }

    @Override
    public int compareTo(final ILsaMolecule o) {
        return args.size() - o.argsNumber();
    }

    @Override
    public boolean dependsOn(final Dependency m) {
        if (m instanceof ILsaMolecule) {
            final ILsaMolecule mol = (ILsaMolecule) m;
            if (mol.argsNumber() != argsNumber()) {
                return false;
            }
            for (int i = 0; i < argsNumber(); i++) {
                if (!args.get(i).mayMatch(mol.getArg(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof LsaMolecule && super.equals(o);
    }

    @Override
    public void forEach(final Consumer<? super IExpression> action) {
        args.forEach(action);
    }

    @Override
    public ILsaMolecule generalize() {
        final List<IExpression> nl = new ArrayList<>(size());
        int i = 0;
        for (final IExpression e : this) {
            switch (e.getRootNodeType()) {
            case NUM:
            case VAR:
            case CONST:
                nl.add(e);
                break;
            default:
                nl.add(new Expression(new VarTreeNode(new HashString("VAR" + i++))));
            }
        }
        return new LsaMolecule(nl);
    }

    @Override
    public IExpression getArg(final int i) {
        return args.get(i);
    }

    /**
     * @return the list of the arguments. Warning: this backs the internal
     *         representation of the LsaMolecule (which should be stateless), so
     *         be sure that your subclass does not change the contents. Copying
     *         it is mandatory before modifying.
     */
    protected List<IExpression> getArgList() {
        return args;
    }

    @Override
    public boolean hasDuplicateVariables() {
        return duplicateVars;
    }

    @Override
    public int hashCode() {
        return ~super.hashCode();
    }

    @Override
    public boolean isIdenticalTo(final ILsaMolecule mol) {
        for (int i = 0; i < argsNumber(); i++) {
            final IExpression argument = args.get(i);
            final IExpression molArgument = mol.getArg(i);
            final boolean isComparator = argument.getRootNodeType() == Type.COMPARATOR;
            if (isComparator && argument.getLeftChildren().getData().equals(molArgument.getRootNodeData())
                    || molArgument.getRootNodeType() == Type.COMPARATOR
                        && molArgument.getLeftChildren().getData().equals(argument.getRootNodeData())
            ) {
                return true; // case <def:N > 5> == <N>
            } else if (argument.getRootNodeType() != molArgument.getRootNodeType()
                    || !(argument.getRootNodeData().equals(molArgument.getRootNodeData()))
            ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isIstance() {
        return instance;
    }

    @Override
    public Iterator<IExpression> iterator() {
        return args.iterator();
    }

    @Override
    public boolean matches(final ILsaMolecule mol) {
        if (this == mol // NOPMD: this comparison is intentional
                || mol instanceof LsaMolecule && ((LsaMolecule) mol).toHashString().equals(toHashString())) {
            return true;
        }
        return mol.matches(args, duplicateVars);
    }

    @Override
    public boolean matches(final List<? extends IExpression> mol, final boolean duplicateVariables) {
        if (argsNumber() != mol.size()) {
            return false;
        }
        final Map<HashString, ITreeNode<?>> map = duplicateVars || duplicateVariables ? new HashMap<>(argsNumber(), 1f) : SMAP;
        for (int i = 0; i < argsNumber(); i++) {
            /*
             * Call matchwith of Expression
             */
            final IExpression a = args.get(i);
            final IExpression tomatch = mol.get(i);
            if (!a.syntacticMatch(tomatch)) {
                final IExpression tempinstance = new Expression(a.updateMatchedVar(map));
                if (!tempinstance.matches(tomatch, map)) {
                    return false;
                }
                if (duplicateVars || duplicateVariables) {
                    final Type tt = tomatch.getRootNodeType();
                    final Type at = a.getRootNodeType();
                    final boolean toMatchIsAssignable = tt.equals(Type.NUM) || tt.equals(Type.CONST) || tt.equals(Type.OPERATOR);
                    if (toMatchIsAssignable && at.equals(Type.VAR)) {
                        map.put((HashString) a.getRootNodeData(), tomatch.calculate(map));
                    } else {
                        final boolean aIsAssignable = at.equals(Type.NUM) || at.equals(Type.CONST) || at.equals(Type.OPERATOR);
                        if (aIsAssignable && tt.equals(Type.VAR)) {
                            map.put((HashString) tomatch.getRootNodeData(), a.calculate(map));
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean moreGenericOf(final ILsaMolecule mol) {
        if (mol.argsNumber() != argsNumber()) {
            return false;
        }
        for (int i = 0; i < argsNumber(); i++) {
            final IExpression a = args.get(i);
            final IExpression tomatch = mol.getArg(i);
            /*
             * If the comparison between arguments make no sense (e.g. two
             * different atoms) or the passed is a variable (and consequently,
             * more generic), then return false
             */
            if (!args.get(i).mayMatch(mol.getArg(i))
                    || a.getRootNodeType() != Type.VAR && tomatch.getRootNodeType() == Type.VAR
            ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        return args.size();
    }

    @Override
    public Spliterator<IExpression> spliterator() {
        return args.spliterator();
    }

    @Override
    public HashString toHashString() {
        if (repr == null) {
            repr = buildString(args);
        }
        return repr;
    }

    @Override
    public String toString() {
        return toHashString().toString();
    }

    private static List<IExpression> buildArgsDesc(final String argsString, final String description) {
        final String[] listArgs = argsString.split(",");
        final boolean hasDescription = description != null && description.length() > 0;
        final List<IExpression> args = new ArrayList<>(listArgs.length + (hasDescription ? 1 : 0));
        for (final String listArg : listArgs) {
            args.add(new Expression(listArg));
        }
        if (hasDescription) {
            args.add(ExpressionFactory.buildComplexGroundExpression(description));
        }
        return args;
    }

    private static HashString buildString(final List<IExpression> expList) {
        final StringBuilder output = new StringBuilder(OPEN_SYMBOL);
        for (int i = 0; i < expList.size(); i++) {
            output.append(expList.get(i).toString());
            if (i < expList.size() - 1) {
                output.append(SEPARATOR);
            }
        }
        output.append(CLOSE_SYMBOL);
        return new HashString(output.toString());
    }

    private static boolean computeInstance(final List<IExpression> e) {
        for (final IExpression exp : e) {
            final Type t = exp.getRootNodeType();
            if (isVarType(t)) {
                return false;
            } else if (t == Type.LIST) {
                for (final ITreeNode<?> ln : ((ListTreeNode) exp.getRootNode()).getData()) {
                    if (isVarType(ln.getType())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean containVars(final ITreeNode<?> e, final List<HashString> l) {
        if (e.getType() == Type.VAR) {
            final HashString fs = e.toHashString();
            if (l.contains(fs)) {
                return true;
            } else {
                l.add(e.toHashString());
                return false;
            }
        } else if (e.getLeftChild() != null) {
            if (e.getRightChild() != null) {
                return containVars(e.getRightChild(), l) || containVars(e.getLeftChild(), l);
            }
            return containVars(e.getLeftChild(), l);
        }
        return false;
    }

    private static boolean isVarType(final Type t) {
        return t == Type.VAR || t == Type.COMPARATOR || t == Type.LISTCOMPARATOR || t == Type.OPERATOR;
    }

    private static boolean selfVariableUsed(final List<IExpression> expList) {
        final List<HashString> foundVars = new ArrayList<>(expList.size());
        for (final IExpression e : expList) {
            if (containVars(e.getRootNode(), foundVars)) {
                return true;
            }
        }
        return false;
    }

}
