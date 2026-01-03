/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Dependency;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.observation.Observable;
import it.unibo.alchemist.model.observation.ObservableMutableSet;
import it.unibo.alchemist.model.observation.ObservableSet;
import org.danilopianini.util.LinkedListSet;
import org.danilopianini.util.ListSet;
import org.danilopianini.util.ListSets;

import javax.annotation.Nonnull;
import java.io.Serial;
import java.util.Objects;

/**
 * @param <T> concentration type
 */
public abstract class AbstractCondition<T> implements Condition<T> {

    @Serial
    private static final long serialVersionUID = -1610947908159507754L;
    private final ListSet<Dependency> influencing = new LinkedListSet<>();
    private final Node<T> node;

    private final ObservableMutableSet<Observable<?>> dependencies = new ObservableMutableSet<>();

    /**
     * An observable which emits this condition's validity.
     */
    private Observable<Boolean> validity;

    /**
     * An observable which emits this condition's propensity contribution.
     */
    private Observable<Double> propensity;

    /**
     * @param node the node this Condition belongs to
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public AbstractCondition(@Nonnull final Node<T> node) {
        this.node = Objects.requireNonNull(node);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * How to override: if you intend your condition to be potentially changed by
     * any change in the context, return null.
     */
    @Override
    public final ListSet<? extends Dependency> getInboundDependencies() {
        return ListSets.unmodifiableListSet(influencing);
    }

    @Override
    public final ObservableSet<? extends Observable<?>> observeInboundDependencies() {
        return dependencies;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Override if your {@link Condition} can return a more specific type of node.
     * The typical way is to cast the call to super.getNode().
     */
    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "It is intentional")
    public Node<T> getNode() {
        return node;
    }

    /**
     * @param m the molecule to add
     */
    protected final void declareDependencyOn(final Dependency m) {
        influencing.add(m);
    }

    /**
     * Add the given {@link Observable} to the set of observables of this Condition.
     *
     * @param dep the observable to observe
     * @return the input dependency itself
     */
    protected Observable<?> addObservableDependency(final Observable<?> dep) {
        dependencies.add(dep);
        return dep;
    }

    /**
     * If you plan to extend this method, please consider leaving this
     * implementation as is and use a reactive/observable style by
     * overriding {@link #observeValidity()}.
     *
     * @return whether this condition is valid.
     */
    @Override
    public boolean isValid() {
        return observeValidity().getCurrent();
    }

    @Override
    public final Observable<Boolean> observeValidity() {
        return validity;
    }

    /**
     * If you plan to extend this method, please consider leaving this
     * implementation as is and use a reactive/observable style by
     * overriding {@link #observePropensityContribution()}.
     *
     * @return this condition's propensity contribution.
     */
    @Override
    public double getPropensityContribution() {
        return observePropensityContribution().getCurrent();
    }

    @Override
    public final Observable<Double> observePropensityContribution() {
        return propensity;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * How to override: create a new action of your concrete subtype.
     */
    @Override
    public Condition<T> cloneCondition(final Node<T> newNode, final Reaction<T> newReaction) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " has no support for cloning.");
    }

    protected final void setValidity(final Observable<Boolean> newValidity) {
        this.validity = newValidity;
    }

    protected final void setPropensity(final Observable<Double> newPropensity) {
        this.propensity = newPropensity;
    }

    /**
     * @return the simple class name
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
