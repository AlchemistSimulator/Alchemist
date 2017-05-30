/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * The type which describes the concentration of a molecule
 * 
 * This class offers a partial implementation of Reaction. In particular, it
 * allows to write new reaction specifying only which distribution time to adopt
 * 
 * @param <T>
 */
public abstract class AReaction<T> implements Reaction<T> {

    private static final int CENTER = 0;
    private static final AtomicInteger ID_GEN = new AtomicInteger();
    /**
     * How bigger should be the StringBuffer with respect to the previous
     * interaction
     */
    private static final byte MARGIN = 20;
    private static final int MAX = 1073741824;
    private static final int MIN = -MAX;
    /**
     * Separators for toString.
     */
    protected static final String NEXT = "next scheduled @", SEP0 = " :: ", SEP1 = " -", SEP2 = "-> ";
    private static final AtomicInteger ODD = new AtomicInteger(1);
    private static final AtomicBoolean POSITIVE = new AtomicBoolean(true);
    private static final AtomicInteger POW = new AtomicInteger(1);
    private static final long serialVersionUID = 6454665278161217867L;

    private List<? extends Action<T>> actions = new ArrayList<Action<T>>(0);
    private List<? extends Condition<T>> conditions = new ArrayList<Condition<T>>(0);
    private List<Molecule> influencing = new ArrayList<Molecule>(), influenced = new ArrayList<Molecule>();

    private final int hash;
    private Context incontext = Context.LOCAL, outcontext = Context.LOCAL;
    private int stringLength = Byte.MAX_VALUE;
    private final TimeDistribution<T> dist;
    private final Node<T> node;

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
        final ArrayList<Condition<T>> c = new ArrayList<Condition<T>>(conditions.size());
        for (final Condition<T> cond : getConditions()) {
            c.add(cond.cloneCondition(n, res));
        }
        final ArrayList<Action<T>> a = new ArrayList<Action<T>>(actions.size());
        for (final Action<T> act : getActions()) {
            a.add(act.cloneAction(n, res));
        }
        res.setActions(a);
        res.setConditions(c);
        return res;
    }

    /**
     * Builds a new reaction, starting at time t.
     * 
     * @param n
     *            the node this reaction belongs to
     * @param pd
     *            the time distribution this reaction should follow
     */
    public AReaction(final Node<T> n, final TimeDistribution<T> pd) {
        final int id = ID_GEN.getAndIncrement();
        if (id == 0) {
            hash = CENTER;
        } else {
            final boolean positive = POSITIVE.get();
            final int val = positive ? MAX : MIN;
            final int pow = POW.get();
            final int odd = ODD.get();
            hash = val / pow * odd;
            if (!positive) {
                if (odd + 2 > pow) {
                    POW.set(pow * 2);
                    ODD.set(1);
                } else {
                    ODD.set(odd + 2);
                }
            }
            POSITIVE.set(!positive);
        }
        dist = pd;
        node = n;
    }

    @Override
    public int compareTo(final Reaction<T> o) {
        return getTau().compareTo(o.getTau());
    }

    @Override
    public final boolean equals(final Object o) {
        if (o instanceof AReaction) {
            return ((AReaction<?>) o).hash == hash;
        }
        return false;
    }

    @Override
    public Context getInputContext() {
        return incontext;
    }

    @Override
    public Context getOutputContext() {
        return outcontext;
    }

    @Override
    public Time getTau() {
        return dist.getNextOccurence();
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public void initializationComplete(final Time t, final Environment<T> env) { }

    /**
     * Used by sublcasses to set their input context.
     * 
     * @param c
     *            the new input context
     */
    protected void setInputContext(final Context c) {
        incontext = c;
    }

    /**
     * Used by sublcasses to set their output context.
     * 
     * @param c
     *            the new input context
     */
    protected void setOutputContext(final Context c) {
        outcontext = c;
    }

    @Override
    public String toString() {
        final StringBuilder tot = new StringBuilder(stringLength + MARGIN);
        tot.append(getReactionName());
        tot.append(SEP0);
        tot.append(NEXT);
        tot.append(getTau());
        tot.append('\n');
        for (final Condition<T> c : getConditions()) {
            tot.append(c);
            tot.append(' ');
        }
        tot.append(SEP1);
        tot.append(getRateAsString());
        tot.append(SEP2);
        for (final Action<T> a : getActions()) {
            tot.append(a);
            tot.append(' ');
        }
        stringLength = tot.length();
        return tot.toString();
    }

    /**
     * This method is used to provide a reaction name in toString().
     * 
     * @return the name for this reaction.
     */
    protected String getReactionName() {
        return getClass().getSimpleName();
    }

    @Override
    public final void update(final Time curTime, final boolean executed, final Environment<T> env) {
        updateInternalStatus(curTime, executed, env);
        dist.update(curTime, executed, getRate(), env);
    }

    @Override
    public final TimeDistribution<T> getTimeDistribution() {
        return dist;
    }

    /**
     * Allows subclasses to add influenced molecules.
     * 
     * @param m
     *            the influenced molecule
     */
    protected void addInfluencedMolecule(final Molecule m) {
        influenced.add(m);
    }

    /**
     * This method gets called as soon as
     * {@link #update(Time, boolean, Environment)} is called. It is useful to
     * update the internal status of the reaction.
     * 
     * @param curTime
     *            the current simulation time
     * @param executed
     *            true if this reaction has just been executed, false if the
     *            update has been triggered due to a dependency
     * @param env
     *            the current environment
     */
    protected abstract void updateInternalStatus(Time curTime, boolean executed, Environment<T> env);

    /**
     * Allows subclasses to add influencing molecules.
     * 
     * @param m
     *            the molecule to add
     */
    protected void addInfluencingMolecule(final Molecule m) {
        influencing.add(m);
    }

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
    public void execute() {
        for (final Action<T> a : actions) {
            a.execute();
        }
    }

    @Override
    public List<Action<T>> getActions() {
        return Collections.unmodifiableList(actions);
    }

    @Override
    public List<Condition<T>> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    @Override
    public List<Molecule> getInfluencedMolecules() {
        return influenced == null ? null : Collections.unmodifiableList(influenced);
    }

    /**
     * @param influenced
     *            the new influenced molecules. Can be null.
     */
    @SuppressWarnings("unchecked")
    protected void setInfluencedMolecules(final List<? extends Molecule> influenced) {
        this.influenced = (List<Molecule>) influenced;
    }

    @Override
    public List<Molecule> getInfluencingMolecules() {
        return influenced == null ? null : Collections.unmodifiableList(influencing);
    }

    /**
     * @param influencing
     *            the new influencing molecules. Can be null.
     */
    @SuppressWarnings("unchecked")
    protected void setInfluencingMolecules(final List<? extends Molecule> influencing) {
        this.influencing = (List<Molecule>) influencing;
    }


    @Override
    public Node<T> getNode() {
        return node;
    }

    /**
     * @return a {@link String} representation of the rate
     */
    protected String getRateAsString() {
        return Double.toString(dist.getRate());
    }

    @Override
    public void setActions(final List<Action<T>> a) {
        actions = Objects.requireNonNull(a, "The actions list can't be null");
        Context lessStrict = Context.LOCAL;
        influenced = new ArrayList<Molecule>();
        for (final Action<T> act : actions) {
            final Context condcontext = Objects.requireNonNull(act, "Actions can't be null")
                    .getContext();
            lessStrict = lessStrict.isMoreStrict(condcontext) ? condcontext : lessStrict;
            final List<? extends Molecule> mod = act.getModifiedMolecules();
            /*
             * This check is needed because of the meaning of a null list of
             * modified molecules: it means that the reaction will influence
             * every other reaction. This must be managed directly by the
             * dependency graph, and consequently the whole reaction must have a
             * null list of modified molecules.
             */
            if (mod != null) {
                influenced.addAll(mod);
            } else {
                influenced = null;
                break;
            }
        }
        setOutputContext(lessStrict);
    }

    @Override
    public void setConditions(final List<Condition<T>> c) {
        conditions = c;
        Context lessStrict = Context.LOCAL;
        influencing = new ArrayList<Molecule>();
        for (final Condition<T> cond : conditions) {
            final Context condcontext = cond.getContext();
            lessStrict = lessStrict.isMoreStrict(condcontext) ? condcontext : lessStrict;
            final List<? extends Molecule> mod = cond.getInfluencingMolecules();
            /*
             * This check is needed because of the meaning of a null list of
             * modified molecules: it means that the reaction will influence
             * every other reaction. This must be managed directly by the
             * dependency graph, and consequently the whole reaction must have a
             * null list of modified molecules.
             */
            if (mod != null) {
                influencing.addAll(mod);
            } else {
                influencing = null;
                break;
            }
        }
        setInputContext(lessStrict);
    }

}
