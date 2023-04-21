/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import com.google.common.collect.Sets;
import it.unibo.alchemist.expressions.implementations.Expression;
import it.unibo.alchemist.expressions.implementations.ListTreeNode;
import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.implementations.Type;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaAction;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import org.danilopianini.lang.HashString;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public abstract class LsaAbstractAction extends AbstractAction<List<ILsaMolecule>> implements ILsaAction {
    private static final long serialVersionUID = 4158296120349274343L;
    private Map<HashString, ITreeNode<?>> matches;
    private List<ILsaNode> nodes;

    /**
     * @param node
     *            the node
     * @param m
     *            the modified molecule
     */
    public LsaAbstractAction(final ILsaNode node, final List<ILsaMolecule> m) {
        super(node);
        for (final ILsaMolecule mol : m) {
            declareDependencyTo(mol);
        }
    }

    /**
     * Used to add a new match to the matches map.
     * 
     * @param key
     *            the variable
     * @param value
     *            the associated value
     */
    protected final void addMatch(final HashString key, final double value) {
        addMatch(key, new NumTreeNode(value));
    }

    /**
     * Used to add a new match to the matches map.
     * 
     * @param key
     *            the variable
     * @param value
     *            the associated value
     */
    protected final void addMatch(final ITreeNode<?> key, final ITreeNode<?> value) {
        if (key.getType() != Type.VAR) {
            throw new IllegalArgumentException("Only variables can be used as keys when inserting matches.");
        }
        switch (value.getType()) {
        case COMPARATOR:
        case LISTCOMPARATOR:
        case OPERATOR:
        case VAR:
            throw new IllegalArgumentException("Only instanced elements can be used as values when inserting matches.");
        default:
            addMatch(key.toHashString(), value);
        }
    }

    /**
     * Used to add a new match to the matches map.
     * 
     * @param key
     *            the variable
     * @param value
     *            the associated value
     */
    protected final void addMatch(final IExpression key, final ITreeNode<?> value) {
        addMatch(key.getRootNode(), value);
    }

    /**
     * Used to add a new match to the matches map.
     * 
     * @param key
     *            the variable
     * @param value
     *            the associated value
     */
    protected final void addMatch(final IExpression key, final IExpression value) {
        addMatch(key.getRootNode(), value.getRootNode());
    }

    /**
     * Used to add a new match to the matches map.
     * 
     * @param key
     *            the variable
     * @param value
     *            the associated value
     */
    protected final void addMatch(final HashString key, final ITreeNode<?> value) {
        if (matches == null) {
            matches = new HashMap<>();
        }
        matches.put(key, value);
    }

    /**
     * Used to add a new match to the matches map.
     * 
     * @param key
     *            the variable
     * @param value
     *            the associated value
     */
    protected final void addMatch(final String key, final double value) {
        addMatch(new HashString(key), new NumTreeNode(value));
    }

    /**
     * Used to add a new match to the matches map.
     * 
     * @param key
     *            the variable
     * @param value
     *            the associated value
     */
    protected final void addMatch(final String key, final ITreeNode<?> value) {
        addMatch(new HashString(key), value);
    }

    /**
     * Given an LSA template, allocates all the variables using the current
     * matches. The allocations may or not produce an instance: it does only if
     * all the variable within the LSA have a corresponding entry in the matches
     * map.
     * 
     * @param template
     *            the LSA template
     * @return the template with all the the variables instanced
     */
    protected List<IExpression> allocateVars(final ILsaMolecule template) {
        return template.allocateVar(getMatches());
    }

    /**
     * Same of {@link #allocateVars(ILsaMolecule)}, but also builds an
     * ILsaMolecule.
     * 
     * @param template
     *            the LSA template
     * @return the template with all the the variables instanced
     */
    protected ILsaMolecule allocateVarsAndBuildLSA(final ILsaMolecule template) {
        return new LsaMolecule(template.allocateVar(getMatches()));
    }

    @Override
    public abstract LsaAbstractAction cloneAction(Node<List<ILsaMolecule>> node, Reaction<List<ILsaMolecule>> reaction);

    /**
     * This method allows subclasses to access a field of an LSA (supposed to be
     * a computable expression or a number) and have back its VALUE as Double.
     * Hides some low-level details. If a molecule template is passed, it will
     * be istanced using the current matches.
     * 
     * @param mol
     *            the molecule (or molecule template)
     * @param argNumber
     *            the position of the argument
     * @return the value as Double, or NaN if you use this method not properly
     */
    protected Double getLSAArgumentAsDouble(final ILsaMolecule mol, final int argNumber) {
        return (Double) getLSAArgumentAsObject(mol, argNumber);
    }

    /**
     * This method allows subclasses to access a field of an LSA (supposed to be
     * a computable expression or a number) and have back its VALUE as Double.
     * Hides some low-level details. If a molecule template is passed, it will
     * be instanced using the current matches.
     * 
     * @param mol
     *            the molecule (or molecule template)
     * @param argNumber
     *            the position of the argument
     * @return the value as Double, or NaN if you use this method not properly
     */
    protected Double getLSAArgumentAsDouble(final List<IExpression> mol, final int argNumber) {
        return (Double) getLSAArgumentAsObject(mol, argNumber);
    }

    /**
     * This method allows subclasses to access a field of an LSA (supposed to be
     * a computable expression or a number) and have back its VALUE as Double.
     * Hides some low-level details. If a molecule template is passed, it will
     * be instanced using the current matches.
     * 
     * @param mol
     *            the molecule (or molecule template)
     * @param argNumber
     *            the position of the argument
     * @return the value as Integer, or 0 if you use this method not properly
     */
    protected int getLSAArgumentAsInt(final ILsaMolecule mol, final int argNumber) {
        return getLSAArgumentAsDouble(mol, argNumber).intValue();
    }

    /**
     * This method allows subclasses to access a field of an LSA (supposed to be
     * a computable expression or a number) and have back its VALUE as Double.
     * Hides some low-level details. If a molecule template is passed, it will
     * be instanced using the current matches.
     * 
     * @param mol
     *            the molecule (or molecule template)
     * @param argNumber
     *            the position of the argument
     * @return the value as Integer, or 0 if you use this method not properly
     */
    protected int getLSAArgumentAsInt(final List<IExpression> mol, final int argNumber) {
        return getLSAArgumentAsDouble(mol, argNumber).intValue();
    }

    /**
     * This method allows subclasses to access a field of an LSA (supposed to be
     * a computable expression or a number) and have back its VALUE as Integer.
     * Hides some low-level details. If a molecule template is passed, it will
     * be instanced using the current matches.
     * 
     * @param mol
     *            the molecule (or molecule template)
     * @param argNumber
     *            the position of the argument
     * @return the value as Object
     */
    protected Object getLSAArgumentAsObject(final ILsaMolecule mol, final int argNumber) {
        return mol.getArg(argNumber).calculate(matches).getValue(matches);
    }

    /**
     * This method allows subclasses to access a field of an LSA (supposed to be
     * a computable expression or a number) and have back its VALUE as Object.
     * Hides some low-level details. If a molecule template is passed, it will
     * be instanced using the current matches.
     * 
     * @param mol
     *            the molecule (or molecule template)
     * @param argNumber
     *            the position of the argument
     * @return the value as Object
     */
    protected Object getLSAArgumentAsObject(final List<IExpression> mol, final int argNumber) {
        return mol.get(argNumber).calculate(matches).getValue(matches);
    }

    /**
     * This method allows subclasses to access a field of an LSA (supposed to be
     * a computable expression or a number) and have back its VALUE as String.
     * Hides some low-level details. If a molecule template is passed, it will
     * be instanced using the current matches.
     * 
     * @param mol
     *            the molecule (or molecule template)
     * @param argNumber
     *            the position of the argument
     * @return the value as String
     */
    protected String getLSAArgumentAsString(final ILsaMolecule mol, final int argNumber) {
        return getLSAArgumentAsObject(mol, argNumber).toString();
    }

    /**
     * This method allows subclasses to access a field of an LSA (supposed to be
     * a computable expression or a number) and have back its VALUE as String.
     * Hides some low-level details. If a molecule template is passed, it will
     * be instanced using the current matches.
     * 
     * @param mol
     *            the molecule (or molecule template)
     * @param argNumber
     *            the position of the argument
     * @return the value as String
     */
    protected String getLSAArgumentAsString(final List<IExpression> mol, final int argNumber) {
        return getLSAArgumentAsObject(mol, argNumber).toString();
    }

    /**
     * Accesses an LSA space and retrieves a molecule matching template. It can
     * use the template as-is or instance its variables with the current matches
     * before accessing the space.
     * 
     * @param n
     *            the node to access
     * @param template
     *            the template molecule to retrieve
     * @return a list of ILsaMolecules matching the (possibly partly instanced)
     *         template
     */
    protected List<ILsaMolecule> getLSAs(final ILsaNode n, final ILsaMolecule template) {
        return n.getConcentration(template);
    }

    /**
     * Given a variable, allows to access to its associated value.
     * 
     * @param var
     *            the variable
     * @return the value associated to the variable
     */
    protected ITreeNode<?> getMatch(final HashString var) {
        return matches.get(var);
    }

    /**
     * Given a variable, allows to access to its associated value.
     * 
     * @param s
     *            the variable
     * @return the value associated to the variable
     */
    protected ITreeNode<?> getMatch(final String s) {
        return getMatch(new HashString(s));
    }

    /**
     * Given a variable, allows to access to its associated value as double.
     * Note that this might fail if you try to access a value which is not
     * numeric through this method.
     * 
     * @param s
     *            the variable
     * @return the double value associated to the variable
     */
    protected double getMatchAsDouble(final HashString s) {
        return ((NumTreeNode) getMatch(s)).getData();
    }

    /**
     * Given a variable, allows to access to its associated value as double.
     * Note that this might fail if you try to access a value which is not
     * numeric through this method.
     * 
     * @param s
     *            the variable
     * @return the double value associated to the variable
     */
    protected double getMatchAsDouble(final String s) {
        return ((NumTreeNode) getMatch(s)).getData();
    }

    /**
     * Given a variable, allows to access to its associated value as String.
     * 
     * @param s
     *            the variable
     * @return its String representation
     */
    protected String getMatchAsString(final HashString s) {
        return getMatch(s).toString();
    }

    /**
     * Given a variable, allows to access to its associated value as String.
     * 
     * @param s
     *            the variable
     * @return its String representation
     */
    protected String getMatchAsString(final String s) {
        return getMatch(s).toString();
    }

    /**
     * @return the map containing the variable / value associations
     */
    protected final Map<HashString, ITreeNode<?>> getMatches() {
        return matches;
    }

    @Nonnull
    @Override
    public final ILsaNode getNode() {
        return (ILsaNode) super.getNode();
    }

    /**
     * @return a list of the nodes in the current execution context. This backs
     *         the internal representation: handle with care.
     */
    protected final List<ILsaNode> getNodes() {
        return nodes;
    }

    /**
     * Injects an LSA in a node.
     * 
     * @param n
     *            the destination
     * @param m
     *            the LSA to inject
     */
    protected void inject(final ILsaNode n, final ILsaMolecule m) {
        n.setConcentration(new LsaMolecule(m.allocateVar(getMatches())));
    }

    /**
     * Injects an ILsaMolecule locally.
     * 
     * @param m
     *            the molecule to inject. Must be instanced: no variables, no
     *            comparators, no operations of any kind.
     */
    protected void injectLocally(final ILsaMolecule m) {
        inject(getNode(), m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutionContext(final Map<HashString, ITreeNode<?>> m, final List<ILsaNode> n) {
        matches = m;
        nodes = n;
    }

    /**
     * Given an an LSA template, an argument number and some new data, sets the
     * argument in the given position as the passed value.
     * 
     * @param template
     *            the original LSA template
     * @param data
     *            the data to insert
     * @param argNumber
     *            the argument to substitute with the new one
     * @return the template modified, with the insertion of data where required
     *         in place of the old value
     */
    protected ILsaMolecule substitute(final ILsaMolecule template, final double data, final int argNumber) {
        return substitute(template, new NumTreeNode(data), argNumber);
    }

    /**
     * Given an an LSA template, an argument number and some new data, sets the
     * argument in the given position as the passed value.
     * 
     * @param template
     *            the original LSA template
     * @param data
     *            the data to insert
     * @param argNumber
     *            the argument to substitute with the new one
     * @return the template modified, with the insertion of data where required
     *         in place of the old value
     */
    protected ILsaMolecule substitute(final ILsaMolecule template, final ITreeNode<?> data, final int argNumber) {
        final List<IExpression> l = template.allocateVar(null);
        l.remove(argNumber);
        l.add(argNumber, new Expression(data));
        return new LsaMolecule(l);
    }

    /**
     * Sets #D.
     * 
     * @param d
     *            the value associated to #D
     */
    protected void setSyntheticD(final double d) {
        matches.put(LsaMolecule.SYN_D, new NumTreeNode(d));
    }

    /**
     * Sets #ROUTE.
     * 
     * @param d
     *            the value associated to #ROUTE
     */
    protected void setSyntheticRoute(final double d) {
        matches.put(LsaMolecule.SYN_ROUTE, new NumTreeNode(d));
    }

    /**
     * Sets #NEIGH equal to the passed node list.
     * 
     * @param list
     *            the list of nodes to use
     */
    protected void setSyntheticNeigh(final Collection<? extends Node<List<ILsaMolecule>>> list) {
        final Set<ITreeNode<?>> l = Sets.newHashSetWithExpectedSize(list.size());
        for (final Node<List<ILsaMolecule>> n : list) {
            l.add(new NumTreeNode(n.getId()));
        }
        matches.put(LsaMolecule.SYN_NEIGH, new ListTreeNode(l));
    }

    /**
     * Sets #O to the local node.
     */
    protected void setSyntheticO() {
        matches.put(LsaMolecule.SYN_O, new NumTreeNode(getNode().getId()));
    }

    @Override
    public abstract String toString();

}
