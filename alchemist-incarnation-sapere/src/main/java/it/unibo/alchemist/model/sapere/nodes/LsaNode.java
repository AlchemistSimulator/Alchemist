/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.nodes;

import it.unibo.alchemist.model.sapere.dsl.impl.Expression;
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.nodes.GenericNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class realizes a node with LSA concentration.
 */
public final class LsaNode extends GenericNode<List<ILsaMolecule>> implements ILsaNode {
    private static final long serialVersionUID = -2167025208984968645L;
    private final List<ILsaMolecule> instances = new ArrayList<>();
    private static final ILsaMolecule ZEROMOL = new LsaMolecule("0");

    /**
     * @param environment
     *            The environment (used for safe node id computation)
     */
    public LsaNode(final Environment<List<ILsaMolecule>, ?> environment) {
        super(environment);
    }

    @Override
    public boolean contains(@Nonnull final Molecule molecule) {
        if (molecule instanceof ILsaMolecule) {
            final ILsaMolecule toMatch = (ILsaMolecule) molecule;
            return instances.stream().anyMatch(mol -> mol.matches(toMatch));
        }
        return false;
    }

    @Override
    @Nullable
    protected List<ILsaMolecule> createT() { // NOPMD: this must return null, not an empty collection.
        return null; // NOPMD: this must return null, not an empty collection.
    }

    @Override
    public int getMoleculeCount() {
        return instances.size();
    }

    @Override
    public List<ILsaMolecule> getConcentration(@Nonnull final Molecule m) {
        if (!(m instanceof ILsaMolecule)) {
            throw new IllegalArgumentException(m + " is not a compatible molecule type");
        }
        final ILsaMolecule mol = (ILsaMolecule) m;
        final ArrayList<ILsaMolecule> listMol = new ArrayList<>();
        for (final ILsaMolecule instance : instances) {
            if (mol.matches(instance)) {
                listMol.add(instance);
            }
        }
        return listMol;
    }

    @Override
    @Nonnull
    public Map<Molecule, List<ILsaMolecule>> getContents() {
        final Map<Molecule, List<ILsaMolecule>> res = new HashMap<>(instances.size(), 1.0f);
        for (final ILsaMolecule m : instances) {
            final List<ILsaMolecule> l;
            if (res.containsKey(m)) {
                /*
                 * Safe by construction.
                 */
                l = res.get(m);
            } else {
                l = new ArrayList<>(1);
                l.add(ZEROMOL);
                res.put(m, l);
            }
            final Double v = (Double) l.get(0).getArg(0).getRootNodeData() + 1;
            final IExpression e = new Expression(new NumTreeNode(v));
            l.set(0, new LsaMolecule(Collections.singletonList(e)));
        }
        return res;
    }

    @Override
    public List<ILsaMolecule> getLsaSpace() {
        return Collections.unmodifiableList(instances);
    }

    @Override
    public boolean removeConcentration(final ILsaMolecule matchedInstance) {
        for (int i = 0; i < instances.size(); i++) {
            if (matchedInstance.matches(instances.get(i))) {
                instances.remove(i);
                return true;
            }
        }
        throw new IllegalStateException("Tried to remove missing " + matchedInstance + " from " + this);
    }

    @Override
    public void setConcentration(final ILsaMolecule inst) {
        if (inst.isIstance()) {
            instances.add(inst);
        } else {
            throw new IllegalStateException("Tried to insert uninstanced " + inst + " into " + this);
        }
    }

    @Override
    public void setConcentration(@Nonnull final Molecule molecule, final List<ILsaMolecule> c) {
        if (molecule instanceof ILsaMolecule) {
            final ILsaMolecule il = (ILsaMolecule) molecule;
            setConcentration(il);
        } else {
            throw new IllegalArgumentException(molecule + " is not a compatible molecule type");
        }
    }

    @Override
    @Nonnull
    public String toString() {
        return getId() + " contains: " + instances.toString();
    }

}
