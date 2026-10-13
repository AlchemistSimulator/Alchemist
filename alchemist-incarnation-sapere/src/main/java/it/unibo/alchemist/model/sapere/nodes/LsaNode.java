/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
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
import it.unibo.alchemist.model.observables.ObservableMutableList;
import it.unibo.alchemist.model.observables.ObservableMutableMap;
import it.unibo.alchemist.model.observation.MutableObservable;
import it.unibo.alchemist.model.observation.Observable;
import it.unibo.alchemist.model.observation.ObservableList;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.impl.Expression;
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static it.unibo.alchemist.model.observables.util.MutableObservables.observe;

/**
 * This class realizes a node with LSA concentration.
 */
public final class LsaNode extends GenericNode<List<ILsaMolecule>> implements ILsaNode {

    private static final String INCOMPATIBLE_MOLECULE = " is not a compatible molecule type";

    private final Map<String, List<ILsaMolecule>> instancesByName = new HashMap<>();
    private final Map<String, MutableObservable<List<ILsaMolecule>>> observablesByName = new HashMap<>();
    private final MutableObservable<Integer> moleculeCount = observe(0);
    private final ObservableMutableList<ILsaMolecule> allInstances = new ObservableMutableList<>();

    /**
     * @param environment
     *            The environment (used for safe node id computation)
     */
    public LsaNode(final Environment<List<ILsaMolecule>, ?> environment) {
        super(environment);
    }

    @Override
    @NotNull
    public Observable<Boolean> observeContains(@NotNull final Molecule molecule) {
        if (molecule instanceof final ILsaMolecule toMatch) {
            return observeMoleculeName(toMatch.getArg(0).toString()).map(molecules ->
                molecules.stream().anyMatch(mol -> mol.matches(toMatch))
            );
        }
        return observe(false);
    }

    @Override
    @Nullable
    protected List<ILsaMolecule> createT() { // NOPMD: this must return null, not an empty collection.
        return null; // NOPMD: this must return null, not an empty collection.
    }

    @Override
    @NotNull
    public Observable<Integer> getObserveMoleculeCount() {
        return moleculeCount;
    }

    @Override
    @NotNull
    public Observable<Option<List<ILsaMolecule>>> observeConcentration(@NotNull final Molecule molecule) {
        if (!(molecule instanceof final ILsaMolecule mol)) {
            throw new IllegalArgumentException(molecule + INCOMPATIBLE_MOLECULE);
        }
        return observeMoleculeName(mol.getArg(0).toString()).map(current -> {
            final ArrayList<ILsaMolecule> res = new ArrayList<>();
            for (final ILsaMolecule instance : current) {
                if (mol.matches(instance)) {
                    res.add(instance);
                }
            }
            return Option.fromNullable(res);
        });
    }

    @Override
    @NotNull
    public Observable<List<ILsaMolecule>> observeMoleculeName(@NotNull final String name) {
        synchronized (instancesByName) {
            return observablesByName.computeIfAbsent(name, k ->
                observe(
                    new ArrayList<>(instancesByName.getOrDefault(name, Collections.emptyList()))
                )
            );
        }
    }

    @Override
    public List<ILsaMolecule> getLsaSpace() {
        return allInstances.getCurrent();
    }

    @Override
    public ObservableList<ILsaMolecule> observeLsaSpace() {
        return allInstances;
    }

    @Override
    public boolean removeOneMatchedLsa(final ILsaMolecule matchedInstance) {
        synchronized (instancesByName) {
            final String name = matchedInstance.getArg(0).toString();
            final List<ILsaMolecule> list = instancesByName.get(name);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    if (matchedInstance.matches(list.get(i))) {
                        // The argument may be a template: remove the stored instance it matched.
                        final ILsaMolecule removed = list.remove(i);
                        moleculeCount.update(c -> c - 1);
                        allInstances.remove(removed);
                        updateCount(removed, list);
                        final var obs = observablesByName.get(name);
                        if (obs != null) {
                            obs.update(it -> new ArrayList<>(list));
                        }
                        return true;
                    }
                }
            }
        }
        throw new IllegalStateException("Tried to remove missing " + matchedInstance + " from " + this);
    }

    /**
     * Removes every copy of [moleculeToRemove] from this node.
     *
     * @param moleculeToRemove the LSA to remove
     * @throws java.util.NoSuchElementException if the node contains no copy of it
     */
    @Override
    public void removeConcentration(@Nonnull final Molecule moleculeToRemove) {
        if (!(moleculeToRemove instanceof final ILsaMolecule lsa)) {
            throw new IllegalArgumentException(moleculeToRemove + INCOMPATIBLE_MOLECULE);
        }
        synchronized (instancesByName) {
            if (!getLsaSpace().contains(lsa)) {
                throw new NoSuchElementException(lsa + " was not present in node " + getId());
            }
            while (getLsaSpace().contains(lsa)) {
                removeOneMatchedLsa(lsa);
            }
        }
    }

    @Override
    public void addLsa(final ILsaMolecule inst) {
        if (inst.isIstance()) {
            synchronized (instancesByName) {
                final String name = inst.getArg(0).toString();
                final List<ILsaMolecule> list = instancesByName.computeIfAbsent(name, k -> new ArrayList<>());
                list.add(inst);
                moleculeCount.update(c -> c + 1);
                allInstances.add(inst);
                updateCount(inst, list);
                final var obs = observablesByName.get(name);
                if (obs != null) {
                    obs.update(it -> new ArrayList<>(list));
                }
            }
        } else {
            throw new IllegalStateException("Tried to insert uninstanced " + inst + " into " + this);
        }
    }

    @Override
    public void setConcentration(@Nonnull final Molecule molecule, final List<ILsaMolecule> c) {
        if (molecule instanceof final ILsaMolecule il) {
            addLsa(il);
        } else {
            throw new IllegalArgumentException(molecule + INCOMPATIBLE_MOLECULE);
        }
    }

    @Override
    @Nonnull
    public String toString() {
        return getId() + " contains: " + allInstances.getCurrent();
    }

    /*
     * The contents map each distinct instance to a single LSA holding how many copies of it the node contains.
     * They are updated along with every mutation, so that their observers are notified, by counting the copies in
     * the bucket of the instance: the count of a mutated instance is always the actual one.
     */
    private void updateCount(final ILsaMolecule instance, final List<ILsaMolecule> bucket) {
        final long count = bucket.stream().filter(instance::equals).count();
        final ObservableMutableMap<Molecule, List<ILsaMolecule>> contents = getObservableContents();
        if (count > 0) {
            final IExpression countExpression = new Expression(new NumTreeNode((double) count));
            contents.put(instance, List.of(new LsaMolecule(Collections.singletonList(countExpression))));
        } else {
            contents.remove(instance);
        }
    }
}
