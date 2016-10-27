/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.molecules;

import it.unibo.alchemist.expressions.implementations.Expression;
import it.unibo.alchemist.expressions.implementations.ExpressionFactory;
import it.unibo.alchemist.expressions.implementations.ListTreeNode;
import it.unibo.alchemist.expressions.implementations.Type;
import it.unibo.alchemist.expressions.implementations.VarTreeNode;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.Molecule;
import org.danilopianini.lang.util.FasterString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
    private static final Map<FasterString, ITreeNode<?>> SMAP = Collections.unmodifiableMap(new HashMap<FasterString, ITreeNode<?>>(0, 1f));
    /**
     * Synthetic property representing the distance.
     */
    public static final FasterString SYN_D = new FasterString("#D");
    /**
     * Synthetic property representing an LSA ID.
     */
    public static final FasterString SYN_MOL_ID = new FasterString("#ID");
    /**
     * Synthetic property representing the current neighborhood.
     */
    public static final FasterString SYN_NEIGH = new FasterString("#NEIGH");
    /**
     * Synthetic property representing the local node id.
     */
    public static final FasterString SYN_NODE_ID = new FasterString("#NODE");
    /**
     * Synthetic property representing the orientation.
     */
    public static final FasterString SYN_O = new FasterString("#O");
    /**
     * Synthetic property representing a random value.
     */
    public static final FasterString SYN_RAND = new FasterString("#RANDOM");
    /**
     * Synthetic property representing the distance. If the environment does not
     * support route computation, it falls back to SYN_D.
     */
    public static final FasterString SYN_ROUTE = new FasterString("#ROUTE");
    /**
     * Synthetic property representing the current selected neighbor ("+"
     * conditions on the left).
     */
    public static final FasterString SYN_SELECTED = new FasterString("#SELECTEDNEIGH");
    /**
     * Synthetic property representing the current simulation time.
     */
    public static final FasterString SYN_T = new FasterString("#T");

    private final List<IExpression> args;
    private final boolean duplicateVars, instance;

    private static FasterString buildString(final List<IExpression> expList) {
        StringBuilder output = new StringBuilder(OPEN_SYMBOL);
        for (int i = 0; i < expList.size(); i++) {
            output = output.append(expList.get(i).toString());
            if (i < expList.size() - 1) {
                output.append(SEPARATOR);
            }
        }
        output.append(CLOSE_SYMBOL);
        return new FasterString(output.toString());
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

    private static boolean containVars(final ITreeNode<?> e, final List<FasterString> l) {
        if (e.getType() == Type.VAR) {
            final FasterString fs = e.toFasterString();
            if (l.contains(fs)) {
                return true;
            } else {
                l.add(e.toFasterString());
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
        final List<FasterString> foundVars = new ArrayList<>(expList.size());
        boolean count = false;
        for (final IExpression e : expList) {
            count = containVars(e.getRootNode(), foundVars);
            if (count) {
                return count;
            }
        }
        return false;
    }

    /**
     * Empty molecule, no arguments.
     */
    public LsaMolecule() {
        this(new ArrayList<IExpression>(0));
    }

    /**
     * Builds a new LsaMolecule by interpreting a list of IExpressions.
     * Dramatically faster than parsing, slower than copy.
     * 
     * @param listArgs
     *            the list of IExpressions
     */
    public LsaMolecule(final List<IExpression> listArgs) {
        this(listArgs, buildString(listArgs));
    }

    private LsaMolecule(final List<IExpression> listArgs, final FasterString hash) {
        this(listArgs, hash, selfVariableUsed(listArgs), computeInstance(listArgs));
    }

    private LsaMolecule(final List<IExpression> listArgs, final FasterString hash, final boolean dup, final boolean isInstance) {
        super(hash);
        args = listArgs;
        duplicateVars = dup;
        instance = isInstance;
    }

    /**
     * Very fast constructor, produces a copy of an LsaMolecule. Use whenever
     * possible.
     * 
     * @param m
     *            the LsaMolecule to copy
     */
    public LsaMolecule(final LsaMolecule m) {
        this(m.args, m.toFasterString(), m.duplicateVars, m.instance);
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
        args = buildArgsDesc(argsString, description);
        duplicateVars = selfVariableUsed(args);
        instance = computeInstance(args);
    }

    private static List<IExpression> buildArgsDesc(final String argsString, final String description) {
        final String[] listArgs = argsString.split(",");
        final boolean hasDescription = description != null && description.length() > 0;
        final List<IExpression> args = new ArrayList<IExpression>(listArgs.length + (hasDescription ? 1 : 0));
        for (int i = 0; i < listArgs.length; i++) {
            args.add(new Expression(listArgs[i]));
        }
        if (hasDescription) {
            args.add(ExpressionFactory.buildComplexGroundExpression(description));
        }
        return args;
    }

    @Override
    public List<IExpression> allocateVar(final Map<FasterString, ITreeNode<?>> matches) {
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
    public boolean dependsOn(final Molecule m) {
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
    public int hashCode() {
        return super.hashCode() ^ -1;
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
                nl.add(new Expression(new VarTreeNode(new FasterString("VAR" + i++))));
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
    public boolean isIdenticalTo(final ILsaMolecule mol) {
        for (int i = 0; i < argsNumber(); i++) {
            if (args.get(i).getRootNodeType() == Type.COMPARATOR && args.get(i).getLeftChildren().getData().equals(mol.getArg(i).getRootNodeData()) || mol.getArg(i).getRootNodeType() == Type.COMPARATOR && mol.getArg(i).getLeftChildren().getData().equals(args.get(i).getRootNodeData())) {
                return true; // case <def:N > 5> == <N>
            } else if (args.get(i).getRootNodeType() != mol.getArg(i).getRootNodeType() || !(args.get(i).getRootNodeData().equals(mol.getArg(i).getRootNodeData()))) {
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
        if (mol.getId() == getId()) {
            return true;
        }
        return mol.matches(args, duplicateVars);
    }

    @Override
    public boolean matches(final List<? extends IExpression> mol, final boolean duplicateVariables) {
        if (argsNumber() != mol.size()) {
            return false;
        }
        final Map<FasterString, ITreeNode<?>> map = duplicateVars || duplicateVariables ? new HashMap<FasterString, ITreeNode<?>>(argsNumber(), 1f) : SMAP;
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
                        map.put((FasterString) a.getRootNodeData(), tomatch.calculate(map));
                    } else {
                        final boolean aIsAssignable = at.equals(Type.NUM) || at.equals(Type.CONST) || at.equals(Type.OPERATOR);
                        if (aIsAssignable && tt.equals(Type.VAR)) {
                            map.put((FasterString) tomatch.getRootNodeData(), a.calculate(map));
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
            if (!args.get(i).mayMatch(mol.getArg(i)) || (a.getRootNodeType() != Type.VAR && tomatch.getRootNodeType() == Type.VAR)) {
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
    public String toString() {
        return toFasterString().toString();
    }

    @Override
    public void forEach(final Consumer<? super IExpression> action) {
        args.forEach(action);
    }

    @Override
    public Spliterator<IExpression> spliterator() {
        return args.spliterator();
    }

}
