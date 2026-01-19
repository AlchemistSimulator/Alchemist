/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.nodes;

import arrow.core.Option;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.nodes.GenericNode;
import it.unibo.alchemist.model.observation.MutableObservable;
import it.unibo.alchemist.model.observation.Observable;
import it.unibo.alchemist.model.observation.ObservableList;
import it.unibo.alchemist.model.observation.ObservableMutableList;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.impl.Expression;
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class realizes a node with LSA concentration.
 */
public final class LsaNode extends GenericNode<List<ILsaMolecule>> implements ILsaNode {

    @Serial
    private static final long serialVersionUID = -2167025208984968645L;
    private static final ILsaMolecule ZEROMOL = new LsaMolecule("0");

    private final ObservableMutableList<ILsaMolecule> instances = new ObservableMutableList<>();

    /**
     * @param environment
     *            The environment (used for safe node id computation)
     */
    public LsaNode(final Environment<List<ILsaMolecule>, ?> environment) {
        super(environment);
        this.instances.onChange(this, this::updateContents);
    }

    @Override
    @NotNull
    public Observable<Boolean> observeContains(@NotNull final Molecule molecule) {
        if (molecule instanceof final ILsaMolecule toMatch) {
            synchronized (instances) {
                return instances.map(molecules -> molecules.stream().anyMatch(mol -> mol.matches(toMatch)));
            }
        }
        return MutableObservable.Companion.observe(false);
    }

    @Override
    @Nullable
    protected List<ILsaMolecule> createT() { // NOPMD: this must return null, not an empty collection.
        return null; // NOPMD: this must return null, not an empty collection.
    }

    @Override
    @NotNull
    public Observable<Integer> getObserveMoleculeCount() {
        synchronized (instances) {
            return instances.getObservableSize();
        }
    }

    @Override
    @NotNull
    public Observable<Option<List<ILsaMolecule>>> observeConcentration(@NotNull final Molecule molecule) {
        if (!(molecule instanceof final ILsaMolecule mol)) {
            throw new IllegalArgumentException(molecule + " is not a compatible molecule type");
        }
        synchronized (instances) {
            return instances.map(current -> {
                final ArrayList<ILsaMolecule> res = new ArrayList<>();
                for (final ILsaMolecule instance : current) {
                    if (mol.matches(instance)) {
                        res.add(instance);
                    }
                }
                return Option.fromNullable(res);
            });
        }
    }

    @Override
    public List<ILsaMolecule> getLsaSpace() {
        return Collections.unmodifiableList(observeLsaSpace().toList());
    }

    @Override
    public ObservableList<ILsaMolecule> observeLsaSpace() {
        return instances;
    }

    @Override
    public boolean removeConcentration(final ILsaMolecule matchedInstance) {
        synchronized (instances) {
            for (int i = 0; i < instances.getObservableSize().getCurrent(); i++) {
                if (matchedInstance.matches(instances.get(i))) {
                    instances.removeAt(i);
                    return true;
                }
            }
        }
        throw new IllegalStateException("Tried to remove missing " + matchedInstance + " from " + this);
    }

    @Override
    public void setConcentration(final ILsaMolecule inst) {
        if (inst.isIstance()) {
            synchronized (instances) {
                instances.add(inst);
            }
        } else {
            throw new IllegalStateException("Tried to insert uninstanced " + inst + " into " + this);
        }
    }

    @Override
    public void setConcentration(@Nonnull final Molecule molecule, final List<ILsaMolecule> c) {
        if (molecule instanceof final ILsaMolecule il) {
            setConcentration(il);
        } else {
            throw new IllegalArgumentException(molecule + " is not a compatible molecule type");
        }
    }

    @Override
    @Nonnull
    public String toString() {
        return getId() + " contains: " + instances.getCurrent();
    }

    private Unit updateContents(final List<? extends ILsaMolecule> currentInstances) {
        final Map<Molecule, List<ILsaMolecule>> res = new HashMap<>(currentInstances.size(), 1.0f);
        for (final ILsaMolecule m : currentInstances) {
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
        getObservableContents().clearAndPutAll(res);
        return null;
    }
}
