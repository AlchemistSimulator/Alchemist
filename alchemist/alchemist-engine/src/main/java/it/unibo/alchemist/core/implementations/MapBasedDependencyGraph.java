/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/

/**
 * 
 */
package it.unibo.alchemist.core.implementations;

import it.unibo.alchemist.core.interfaces.DependencyGraph;
import it.unibo.alchemist.core.interfaces.DependencyHandler;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *          This class offers an implementation of a dependency graph, namely a
 *          data structure which can address in an efficient way the problem of
 *          finding those reactions affected by the execution of another
 *          reaction. This class relies heavily on the ReactionHandler
 *          interface.
 * 
 * @param <T>
 */
public final class MapBasedDependencyGraph<T> implements DependencyGraph<T> {

    private static final long serialVersionUID = 4118923665670988775L;
    private final Environment<T, ?> env;
    private final Map<Reaction<T>, DependencyHandler<T>> hndlrs;

    /**
     * This constructor builds a new, empty dependency graph. It relies on a map
     * of neighborhoods in order to retrieve them efficiently and on a map of
     * reactions in order to get handlers given a reaction.
     * 
     * @param environment
     *            the environment
     * @param handlers
     *            A map storing, for each reaction, its handler.
     */
    public MapBasedDependencyGraph(final Environment<T, ?> environment, final Map<Reaction<T>, DependencyHandler<T>> handlers) {
        this.hndlrs = handlers;
        this.env = environment;
    }

    @Override
    public void createDependencies(final DependencyHandler<T> rh) {
        createDependencies(rh, rh.getReaction());
    }

    private void addLocalReactions(final Set<Reaction<T>> list, final Reaction<T> r) {
        for (final Reaction<T> or : r.getNode()) {
            /*
             * If the reaction is not the current and it is already in this
             * graph
             */
            if (or != r && hndlrs.containsKey(or)) { // NOPMD by danysk on 8/20/13 2:37 PM
                list.add(or);
            }
        }
    }

    private void addNeighborhoodReactions(final Set<Reaction<T>> list, final Reaction<T> r) {
        for (final Node<T> n : env.getNeighborhood(r.getNode())) {
            for (final Reaction<T> or : n) {
                /*
                 * If the reaction is already in this graph
                 */
                if (hndlrs.containsKey(or)) {
                    list.add(or);
                }
            }
        }
    }

    private void addExtendedNeighborhoodReactions(final Set<Reaction<T>> list, final Reaction<T> r) {
        for (final Node<T> n : env.getNeighborhood(r.getNode())) {
            for (final Node<T> neigh : env.getNeighborhood(n)) {
                for (final Reaction<T> or : neigh) {
                    /*
                     * If the reaction is not the current and it is already in this
                     * graph
                     */
                    if (or != r && hndlrs.containsKey(or)) { // NOPMD by danysk on 8/20/13 2:37 PM
                        list.add(or);
                    }
                }
            }
        }
    }

    private Set<Reaction<T>> initCandidates(final Reaction<T> newReaction, final Context c) {
        if (c.equals(Context.GLOBAL)) {
            return hndlrs.keySet();
        }
        final Set<Reaction<T>> list = new LinkedHashSet<>();
        addNeighborhoodReactions(list, newReaction);
        if (c.equals(Context.LOCAL)) {
            addLocalReactions(list, newReaction);
        } else {
            addExtendedNeighborhoodReactions(list, newReaction);
        }
        return list;
    }

    private void createDependencies(final DependencyHandler<T> newHandler, final Reaction<T> newReaction) {
        /*
         * Will contain the reactions potentially influencing the new one
         */
        final Iterable<Reaction<T>> inputCandidates = initCandidates(newReaction, newReaction.getInputContext());
        /*
         * Will contain the reactions possibly influenced by the new one
         */
        final Iterable<Reaction<T>> outputCandidates = initCandidates(newReaction, newReaction.getOutputContext());
        /*
         * keySet() is not guaranteed to preserve the ordering. This can lead to
         * bad behaviors, since may change the order by which the reactions are
         * updated, and consequently ruin the predictability
         */
        for (final Reaction<T> r : inputCandidates) {
            if (mayInfluence(r, newReaction) && influences(r, newReaction.getInfluencingMolecules())) {
                final DependencyHandler<T> dep = hndlrs.get(r);
                dep.addOutDependency(newHandler);
                newHandler.addInDependency(dep);
            }
        }
        for (final Reaction<T> r : outputCandidates) {
            if (mayInfluence(newReaction, r) && influences(newReaction, r.getInfluencingMolecules())) {
                final DependencyHandler<T> dep = hndlrs.get(r);
                newHandler.addOutDependency(dep);
                dep.addInDependency(newHandler);
            }
        }
    }

    @Override
    public void removeDependencies(final DependencyHandler<T> rh) {
        final List<DependencyHandler<T>> indeps = rh.isInfluenced();
        while (!indeps.isEmpty()) {
            indeps.get(0).removeOutDependency(rh);
            indeps.remove(0);
        }
        final List<DependencyHandler<T>> outdeps = rh.influences();
        for (final DependencyHandler<T> toChange : outdeps) {
            toChange.removeInDependency(rh);
        }
    }

    private static boolean influences(final Reaction<?> source, final List<? extends Molecule> target) {
        final List<? extends Molecule> sl = source.getInfluencedMolecules();
        if (sl == null || target == null) {
            return true;
        }
        for (final Molecule m : sl) {
            for (final Molecule mol : target) {
                if (m == null || mol == null || m.dependsOn(mol)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean mayInfluence(final Reaction<T> source, final Reaction<T> target) {
        final Context in = target.getInputContext();
        final Context out = source.getOutputContext();
        if (// Same node
        target.getNode().equals(source.getNode())
        // If reaction writes something globally
                || out.equals(Context.GLOBAL)
                // If reaction reads something globally
                || in.equals(Context.GLOBAL)) {
            return true;
        }

        return influenceNeighborCheck(env, source, target, in, out);
    }

    /**
     * This method checks if there may be a dependency considering the
     * neighborhoods.
     */
    private static <T> boolean influenceNeighborCheck(final Environment<T, ?> env, final Reaction<T> source, final Reaction<T> target, final Context in, final Context out) {
        final Neighborhood<T> sn = env.getNeighborhood(source.getNode());
        final boolean scn = in.equals(Context.NEIGHBORHOOD);
        // If source reads from neighborhood and target is within
        if (scn && sn.contains(target.getNode())) {
            return true;
        }
        // If target writes in neighborhood and source is within
        final Neighborhood<T> tn = env.getNeighborhood(target.getNode());
        final boolean tcn = out.equals(Context.NEIGHBORHOOD);
        if (tcn && tn.contains(source.getNode())) {
            return true;
        }
        // If source writes on the neighborhood, target reads on its
        // neighborhood and there is at least one common node
        return scn && tcn && commonNeighbor(env, sn, target.getNode());
    }

    private static <T> boolean commonNeighbor(final Environment<T, ?> env, final Neighborhood<T> sl, final Node<T> t) {
        for (final Node<T> n : sl) {
            if (env.getNeighborhood(n).getNeighbors().contains(t)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addNeighbor(final Node<T> n1, final Node<T> n2) {
        DependencyHandler<T> rh1, rh2;
        for (final Reaction<T> r1 : n1.getReactions()) {
            rh1 = hndlrs.get(r1);
            for (int j = 0; j < n2.getReactions().size(); j++) {
                final Reaction<T> r2 = n2.getReactions().get(j);
                if (mayInfluence(r2, r1) && influences(r2, r1.getInfluencingMolecules())) {
                    rh2 = hndlrs.get(r2);
                    if (!rh1.isInfluenced().contains(rh2)) {
                        rh1.addInDependency(rh2);
                    }
                    if (!rh2.influences().contains(rh1)) {
                        rh2.addOutDependency(rh1);
                    }
                }
                if (mayInfluence(r1, r2) && influences(r1, r2.getInfluencingMolecules())) {
                    rh2 = hndlrs.get(r2);
                    if (!rh2.isInfluenced().contains(rh1)) {
                        rh2.addInDependency(rh1);
                    }
                    if (!rh1.influences().contains(rh2)) {
                        rh1.addOutDependency(rh2);
                    }
                }
            }
        }
    }

    @Override
    public void removeNeighbor(final Node<T> n1, final Node<T> n2) {
        DependencyHandler<T> rh1, rh2;
        for (final Reaction<T> r1 : n1.getReactions()) {
            rh1 = hndlrs.get(r1);
            for (final Reaction<T> r2 : n2.getReactions()) {
                rh2 = hndlrs.get(r2);
                if (!mayInfluence(r2, r1)) {
                    rh1.removeInDependency(rh2);
                    rh2.removeOutDependency(rh1);
                    /*
                     * Dependencies addition or removal should not influence the
                     * next scheduled execution time.
                     */
                }
                if (!mayInfluence(r1, r2)) {
                    rh2.removeInDependency(rh1);
                    rh1.removeOutDependency(rh2);
                    /*
                     * Dependencies addition or removal should not influence the
                     * next scheduled execution time.
                     */
                }
            }
        }
    }
}
