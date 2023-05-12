/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.reactions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Dependency;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.TimeDistribution;
import org.danilopianini.util.ArrayListSet;
import org.danilopianini.util.Hashes;
import org.danilopianini.util.ImmutableListSet;
import org.danilopianini.util.LinkedListSet;
import org.danilopianini.util.ListSet;
import org.danilopianini.util.ListSets;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The type which describes the concentration of a molecule.
 * This class offers a partial implementation of Reaction. In particular, it
 * allows to write new reaction specifying only which distribution time to adopt
 * 
 * @param <T> concentration type
 */
public abstract class AbstractReaction<T> implements Reaction<T> {

    /**
     * How bigger should be the StringBuffer with respect to the previous
     * interaction.
     */
    private static final byte MARGIN = 20;
    private static final ListSet<Dependency> EVERYTHING = ImmutableListSet.of(Dependency.EVERYTHING);
    private static final long serialVersionUID = 1L;
    private final int hash;
    private List<? extends Action<T>> actions = new ArrayList<>(0);
    private List<? extends Condition<T>> conditions = new ArrayList<>(0);
    private Context incontext = Context.LOCAL, outcontext = Context.LOCAL;
    private ListSet<Dependency> outbound = new LinkedListSet<>();
    private ListSet<Dependency> inbound = new LinkedListSet<>();
    private int stringLength = Byte.MAX_VALUE;
    private final TimeDistribution<T> timeDistribution;
    private final Node<T> node;

    /**
     * Builds a new reaction, starting at time t.
     * 
     * @param node
     *            the node this reaction belongs to
     * @param timeDistribution
     *            the time distribution this reaction should follow
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public AbstractReaction(final Node<T> node, final TimeDistribution<T> timeDistribution) {
        hash = Hashes.hash32(node.hashCode(), node.getMoleculeCount(), node.getReactions().size());
        this.timeDistribution = timeDistribution;
        this.node = node;
    }

    /**
     * Allows subclasses to add influenced molecules.
     *
     * @param m
     *            the influenced molecule
     */
    protected final void addOutboundDependency(final Dependency m) {
        outbound.add(m);
    }

    /**
     * Allows subclasses to add influencing molecules.
     *
     * @param m
     *            the molecule to add
     */
    protected final void addInboundDependency(final Dependency m) {
        inbound.add(m);
    }

    /**
     * The default implementation verifies if all the conditions are valid.
     *
     * @return true if the reaction can execute right now.
     */
    @Override
    public boolean canExecute() {
        if (conditions == null) {
            return true;
        }
        int i = 0;
        while (i < conditions.size() && conditions.get(i).isValid()) {
            i++;
        }
        return i == conditions.size();
    }

    @Override
    public final int compareTo(final Actionable<T> o) {
        return getTau().compareTo(o.getTau());
    }

    @Override
    public final boolean equals(final Object o) {
        return this == o;
    }

    /**
     * The default execution iterates all the actions in order and executes them. Override to change the behaviour.
     */
    @Override
    public void execute() {
        for (final Action<T> a : actions) {
            a.execute();
        }
    }

    /**
     * Override only if you need to implement extremely tricky behaviours. Must be overridden along with
     * {@link #setActions(List)}.
     *
     * @return the list of {@link Action}s.
     */
    @Nonnull
    @Override
    public List<Action<T>> getActions() {
        return Collections.unmodifiableList(actions);
    }

    /**
     * Override only if you need to implement extremely tricky behaviours. Must be overridden along with
     * {@link #setConditions(List)}.
     *
     * @return the list of {@link Condition}s.
     */
    @Nonnull
    @Override
    public List<Condition<T>> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    @Nonnull
    @Override
    public final ListSet<Dependency> getOutboundDependencies() {
        return optionallyImmodifiableView(outbound);
    }

    @Nonnull
    @Override
    public final ListSet<Dependency> getInboundDependencies() {
        return optionallyImmodifiableView(inbound);
    }

    @Nonnull
    @Override
    public final Context getInputContext() {
        return incontext;
    }

    @Nonnull
    @Override
    public final Context getOutputContext() {
        return outcontext;
    }

    /**
     * @return a {@link String} representation of the rate
     */
    protected String getRateAsString() {
        return Double.toString(timeDistribution.getRate());
    }

    /**
     * This method is used to provide a reaction name in toString().
     *
     * @return the name for this reaction.
     */
    protected String getReactionName() {
        return getClass().getSimpleName();
    }

    @Nonnull
    @Override
    public final Time getTau() {
        return timeDistribution.getNextOccurence();
    }

    @Nonnull
    @Override
    public final TimeDistribution<T> getTimeDistribution() {
        return timeDistribution;
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public void initializationComplete(@Nonnull final Time atTime, @Nonnull final Environment<T, ?> environment) { }

    /**
     * This method provides facility to clone reactions. Given a constructor in
     * form of a {@link Supplier}, it populates the actions and conditions with
     * cloned version of the ones registered in this reaction.
     *
     * @param builder
     *            the supplier
     *
     * @param <R>
     *            The reaction type
     * @return the populated cloned reaction
     */
    protected <R extends Reaction<T>> R makeClone(final Supplier<R> builder) {
        final R res = builder.get();
        final Node<T> n = res.getNode();
        final ArrayList<Condition<T>> c = new ArrayList<>(conditions.size());
        for (final Condition<T> cond : getConditions()) {
            c.add(cond.cloneCondition(n, res));
        }
        final ArrayList<Action<T>> a = new ArrayList<>(actions.size());
        for (final Action<T> act : getActions()) {
            a.add(act.cloneAction(n, res));
        }
        res.setActions(a);
        res.setConditions(c);
        return res;
    }

    private static ListSet<Dependency> computeDependencies(final Stream<? extends Dependency> stream) {
        final Iterator<? extends Dependency> fromStream = stream.iterator();
        boolean everyMolecule = false;
        final ListSet<Dependency> result = new ArrayListSet<>();
        while (fromStream.hasNext()) {
            final Dependency dependency = fromStream.next();
            if (dependency.equals(Dependency.EVERYTHING)) {
                return EVERYTHING;
            }
            if (dependency.equals(Dependency.EVERY_MOLECULE) && !everyMolecule) {
                result.removeIf(it -> it instanceof Molecule);
                everyMolecule = true;
                result.add(Dependency.EVERY_MOLECULE);
            } else if (!(everyMolecule && dependency instanceof Molecule)) {
                result.add(dependency);
            }
        }
        return result;
    }

    /**
     * This should get overridden only if very tricky behaviours are implemented, such that the default Alchemist
     * action addition model is no longer usable. Must be overridden along with {@link #getActions()}.
     *
     * @param actions the actions to set
     */
    @Override
    public void setActions(@Nonnull final List<? extends Action<T>> actions) {
        this.actions = Objects.requireNonNull(actions, "The actions list can't be null");
        setOutputContext(actions.stream().map(Action::getContext).reduce(Context.LOCAL, Context::getWider));
        outbound = computeDependencies(actions.stream().map(Action::getOutboundDependencies).flatMap(List::stream));
    }

    /**
     * This should get overridden only if very tricky behaviours are implemented, such that the default Alchemist
     * condition addition model is no longer usable. Must be overridden along with {@link #getConditions()}.
     *
     * @param conditions the actions to set
     */
    @Override
    public void setConditions(@Nonnull final List<? extends Condition<T>> conditions) {
        this.conditions = Objects.requireNonNull(conditions, "The conditions list can't be null");
        setInputContext(conditions.stream().map(Condition::getContext).reduce(Context.LOCAL, Context::getWider));
        inbound = computeDependencies(conditions.stream().map(Condition::getInboundDependencies).flatMap(List::stream));
    }

    /**
     * Used by subclasses to set their input context.
     * 
     * @param c
     *            the new input context
     */
    protected final void setInputContext(final Context c) {
        incontext = c;
    }

    /**
     * Used by subclasses to set their output context.
     * 
     * @param c
     *            the new input context
     */
    protected final void setOutputContext(final Context c) {
        outcontext = c;
    }

    /**
     * @return the default implementation returns a String in the form
     * className@timeScheduled[Conditions]-rate-&gt;[Actions]
     */
    @Override
    public String toString() {
        final StringBuilder tot = new StringBuilder(stringLength + MARGIN)
            .append(getReactionName())
            .append('@')
            .append(getTau())
            .append(':')
            .append(getConditions())
            .append('-')
            .append(getRateAsString())
            .append("->")
            .append(getActions());
        stringLength = tot.length();
        return tot.toString();
    }

    @Override
    public final void update(
        @Nonnull final Time currentTime,
        final boolean hasBeenExecuted,
        @Nonnull final Environment<T, ?> environment
    ) {
        updateInternalStatus(currentTime, hasBeenExecuted, environment);
        timeDistribution.update(currentTime, hasBeenExecuted, getRate(), environment);
    }

    /**
     * This method gets called as soon as
     * {@link #update(Time, boolean, Environment)} is called. It is useful to
     * update the internal status of the reaction.
     * 
     * @param currentTime
     *            the current simulation time
     * @param hasBeenExecuted
     *            true if this reaction has just been executed, false if the
     *            update has been triggered due to a dependency
     * @param environment
     *            the current environment
     */
    protected abstract void updateInternalStatus(
            Time currentTime,
            boolean hasBeenExecuted,
            Environment<T, ?> environment
    );

    @Nonnull
    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "This is intentional")
    public final Node<T> getNode() {
        return node;
    }


    private static <E> ListSet<E> optionallyImmodifiableView(final ListSet<E> in) {
        return in == null ? null : ListSets.unmodifiableListSet(in);
    }
}
