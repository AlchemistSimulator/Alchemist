/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * 
 */
package it.unibo.alchemist.model.implementations.nodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.danilopianini.util.concurrent.FastReadWriteLock;

import it.unibo.alchemist.expressions.implementations.Expression;
import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Molecule;

/**
 * This class realizes a node with LSA concentration.
 */
public class LsaNode extends AbstractNode<List<ILsaMolecule>> implements ILsaNode {
    private static final long serialVersionUID = -2167025208984968645L;
    private final List<ILsaMolecule> instances = new ArrayList<>();
    private transient FastReadWriteLock lock = new FastReadWriteLock();
    private static final ILsaMolecule ZEROMOL = new LsaMolecule("0");

    /**
     * @param env
     *            The environment (used for safe node id computation)
     */
    public LsaNode(final Environment<List<ILsaMolecule>, ?> env) {
        super(env);
    }

    @Override
    public boolean contains(final Molecule m) {
        if (m instanceof ILsaMolecule) {
            final ILsaMolecule toMatch = (ILsaMolecule) m;
            lock.read();
            final boolean result = instances.parallelStream().anyMatch(mol -> mol.matches(toMatch));
            lock.release();
            return result;
        }
        return false;
    }

    @Override
    protected List<ILsaMolecule> createT() {
        return null;
    }

    @Override
    public int getChemicalSpecies() {
        return instances.size();
    }

    @Override
    public List<ILsaMolecule> getConcentration(final Molecule m) {
        if (!(m instanceof ILsaMolecule)) {
            throw new IllegalArgumentException(m + " is not a compatible molecule type");
        }
        final ILsaMolecule mol = (ILsaMolecule) m;
        final ArrayList<ILsaMolecule> listMol = new ArrayList<>();
        lock.write();
        for (int i = 0; i < instances.size(); i++) {
            if (mol.matches(instances.get(i))) {
                listMol.add(instances.get(i));
            }
        }
        lock.release();
        return listMol;
    }

    @Override
    public Map<Molecule, List<ILsaMolecule>> getContents() {
        final Map<Molecule, List<ILsaMolecule>> res = new HashMap<>(instances.size(), 1.0f);
        lock.read();
        for (final ILsaMolecule m : instances) {
            final List<ILsaMolecule> l;
            if (res.containsKey(m)) {
                /*
                 * Safe by construction.
                 */
                l = (List<ILsaMolecule>) res.get(m);
            } else {
                l = new ArrayList<>(1);
                l.add(ZEROMOL);
                res.put(m, l);
            }
            final Double v = (Double) l.get(0).getArg(0).getRootNodeData() + 1;
            final IExpression e = new Expression(new NumTreeNode(v));
            l.set(0, new LsaMolecule(Arrays.asList(new IExpression[] { e })));
        }
        lock.release();
        return res;
    }

    @Override
    public List<ILsaMolecule> getLsaSpace() {
        return Collections.unmodifiableList(instances);
    }

    @Override
    public boolean removeConcentration(final ILsaMolecule matchedInstance) {
        lock.write();
        for (int i = 0; i < instances.size(); i++) {
            if (matchedInstance.matches(instances.get(i))) {
                instances.remove(i);
                lock.release();
                return true;
            }
        }
        lock.release();
        throw new IllegalStateException("Tried to remove missing " + matchedInstance + " from " + this.toString());
    }

    @Override
    public void setConcentration(final ILsaMolecule inst) {
        if (inst.isIstance()) {
            lock.write();
            instances.add(inst);
            lock.release();
        } else {
            throw new IllegalStateException("Tried to insert uninstanced " + inst + " into " + this);
        }
    }

    @Override
    public void setConcentration(final Molecule mol, final List<ILsaMolecule> c) {
        if (mol instanceof ILsaMolecule) {
            final ILsaMolecule il = (ILsaMolecule) mol;
            setConcentration(il);
        } else {
            throw new IllegalArgumentException(mol + " is not a compatible molecule type");
        }
    }

    @Override
    public String toString() {
        return getId() + " contains: " + instances.toString();
    }

    private void readObject(final ObjectInputStream o) throws ClassNotFoundException, IOException {
        o.defaultReadObject();
        lock = new FastReadWriteLock();
    }

}
