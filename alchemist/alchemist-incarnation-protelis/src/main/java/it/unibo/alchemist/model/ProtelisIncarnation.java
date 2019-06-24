/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.jetbrains.annotations.NotNull;
import org.protelis.lang.ProtelisLoader;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.CodePath;
import org.protelis.vm.ExecutionEnvironment;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.ProtelisVM;
import org.protelis.vm.impl.AbstractExecutionContext;
import org.protelis.vm.impl.SimpleExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.actions.SendToNeighbor;
import it.unibo.alchemist.model.implementations.conditions.ComputationalRoundComplete;
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.implementations.reactions.ChemicalReaction;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * @param <P> position type
 */
public final class ProtelisIncarnation<P extends Position<P>> implements Incarnation<Object, P> {

    /**
     * The name that can be used in a property to refer to the extracted value.
     */
    public static final String VALUE_TOKEN = "<value>";
    private static final Logger L = LoggerFactory.getLogger(ProtelisIncarnation.class);
    private final LoadingCache<CacheKey, SynchronizedVM> cache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<CacheKey, SynchronizedVM>() {
                @Override
                public SynchronizedVM load(@NotNull final CacheKey key) {
                    return new SynchronizedVM(key);
                }
            });

    private static List<RunProtelisProgram<?>> getIncomplete(final ProtelisNode pNode, final List<RunProtelisProgram<?>> alreadyDone) {
        return pNode.getReactions().parallelStream()
                /*
                 * Get the actions
                 */
                .flatMap(r -> r.getActions().parallelStream())
                /*
                 * Get only the ProtelisPrograms
                 */
                .filter(a -> a instanceof RunProtelisProgram)
                .map(a -> (RunProtelisProgram<?>) a)
                /*
                 * Retain only those ProtelisPrograms that have no associated ComputationalRoundComplete.
                 *
                 * Only one should be available.
                 */
                .filter(prog -> !alreadyDone.contains(prog))
                .collect(Collectors.toList());
    }

    @Override
    public Action<Object> createAction(final RandomGenerator rand, final Environment<Object, P> env,
            final Node<Object> node, final TimeDistribution<Object> time, final Reaction<Object> reaction,
            final String param) {
        Objects.requireNonNull(param);
        if (node instanceof ProtelisNode) {
            final ProtelisNode pNode = (ProtelisNode) node;
            if (param.equalsIgnoreCase("send")) {
                final List<RunProtelisProgram<?>> alreadyDone = pNode.getReactions()
                    .parallelStream()
                    .flatMap(r -> r.getActions().parallelStream())
                    .filter(a -> a instanceof SendToNeighbor)
                    .map(c -> (SendToNeighbor) c)
                    .map(SendToNeighbor::getProtelisProgram)
                    .collect(Collectors.toList());
                final List<RunProtelisProgram<?>> pList = getIncomplete(pNode, alreadyDone);
                if (pList.isEmpty()) {
                    throw new IllegalStateException("There is no program requiring a "
                            + SendToNeighbor.class.getSimpleName() + " action");
                }
                if (pList.size() > 1) {
                    throw new IllegalStateException("There are too many programs requiring a "
                            + SendToNeighbor.class.getName() + " action: " + pList);
                }
                return new SendToNeighbor(pNode, reaction, pList.get(0));
            } else {
                try {
                    return new RunProtelisProgram<>(env, pNode, reaction, rand, param);
                } catch (RuntimeException e) { // NOPMD AvoidCatchingGenericException
                    throw new IllegalArgumentException("Could not create the requested Protelis program: " + param, e);
                }
            }
        }
        throw new IllegalArgumentException("The node must be an instance of " + ProtelisNode.class.getSimpleName()
                + ", it is a " + node.getClass().getName() + " instead");
    }

    @Override
    public Object createConcentration(final String s) {
        try {
            final SynchronizedVM vm = new SynchronizedVM(new CacheKey(NoNode.INSTANCE, createMolecule(s), s));
            return vm.runCycle();
        } catch (IllegalArgumentException e) {
            /*
             * Not a valid program: inject the String itself
             */
            return s;
        }
    }

    @Override
    public Condition<Object> createCondition(final RandomGenerator rand, final Environment<Object, P> env,
            final Node<Object> node, final TimeDistribution<Object> time, final Reaction<Object> reaction,
            final String param) {
        if (node instanceof ProtelisNode) {
            final ProtelisNode pNode = (ProtelisNode) node;
            /*
             * The list of ProtelisPrograms that have already been completed with a ComputationalRoundComplete condition
             */
            @SuppressWarnings("unchecked")
            final List<RunProtelisProgram<?>> alreadyDone = pNode.getReactions()
                .parallelStream()
                .flatMap(r -> r.getConditions().parallelStream())
                .filter(c -> c instanceof ComputationalRoundComplete)
                .map(c -> (ComputationalRoundComplete) c)
                .flatMap(crc -> crc.getInboundDependencies().parallelStream())
                .filter(mol -> mol instanceof RunProtelisProgram)
                .map(mol -> (RunProtelisProgram<P>) mol)
                .collect(Collectors.toList());
            final List<RunProtelisProgram<?>> pList = getIncomplete(pNode, alreadyDone);
            if (pList.isEmpty()) {
                throw new IllegalStateException("There is no program requiring a "
                        + ComputationalRoundComplete.class.getSimpleName() + " condition");
            }
            if (pList.size() > 1) {
                throw new IllegalStateException("There are too many programs requiring a "
                        + ComputationalRoundComplete.class.getName() + " condition: " + pList);
            }
            return new ComputationalRoundComplete(pNode, pList.get(0));
        }
        throw new IllegalArgumentException("The node must be an instance of " + ProtelisNode.class.getSimpleName()
                + ", it is a " + node.getClass().getName() + " instead");
    }

    @Override
    public Molecule createMolecule(final String s) {
        return new SimpleMolecule(Objects.requireNonNull(s));
    }

    @Override
    public Node<Object> createNode(final RandomGenerator rand, final Environment<Object, P> env, final String param) {
        return new ProtelisNode(env);
    }

    @Override
    public Reaction<Object> createReaction(final RandomGenerator rand, final Environment<Object, P> env,
            final Node<Object> node, final TimeDistribution<Object> time, final String param) {
        final boolean isSend = "send".equalsIgnoreCase(param);
        final Reaction<Object> result = isSend
                ? new ChemicalReaction<>(Objects.requireNonNull(node), Objects.requireNonNull(time))
                : new Event<>(node, time);
        if (param != null) {
            result.setActions(Lists.newArrayList(createAction(rand, env, node, time, result, param)));
        }
        if (isSend) {
            result.setConditions(Lists.newArrayList(createCondition(rand, env, node, time, result, null)));
        }
        return result;
    }

    @Override
    public TimeDistribution<Object> createTimeDistribution(
            final RandomGenerator rand,
            final Environment<Object, P> env,
            final Node<Object> node,
            final String param) {
        if (param == null) {
            return new ExponentialTime<>(Double.POSITIVE_INFINITY, rand);
        }
        double frequency;
        try {
            frequency = Double.parseDouble(param);
        } catch (final NumberFormatException e) {
            frequency = 1;
        }
        return new DiracComb<>(new DoubleTime(rand.nextDouble() / frequency), frequency);
    }

    @Override
    public double getProperty(final Node<Object> node, final Molecule mol, final String prop) {
        try {
            final SynchronizedVM vm = cache.get(new CacheKey(Objects.requireNonNull(node), Objects.requireNonNull(mol), Objects.requireNonNull(prop)));
            final Object val = vm.runCycle();
            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            } else if (val instanceof String) {
                try {
                    return Double.parseDouble(val.toString());
                } catch (final NumberFormatException e) {
                    if (val.equals(prop)) {
                        return 1;
                    }
                    return 0;
                }
            } else if (val instanceof Boolean) {
                final Boolean cond = (Boolean) val;
                if (cond) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        } catch (ExecutionException e) {
            L.error("Bug in " + getClass().getName() + ": getProperty should never fail.", e);
        }
        return Double.NaN;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    private static final class CacheKey {
        private final Molecule molecule;
        private final WeakReference<Node<Object>> node;
        private final String property;
        private final int hash;
        private CacheKey(final Node<Object> node, final Molecule mol, final String prop) {
            this.node = new WeakReference<>(node);
            molecule = mol;
            property = prop;
            hash = molecule.hashCode() ^ property.hashCode() ^ (node == null ? 0 : node.hashCode());
        }
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof CacheKey
                    && ((CacheKey) obj).node.get() == node.get()
                    && ((CacheKey) obj).molecule.equals(molecule)
                    && ((CacheKey) obj).property.equals(property);
        }
        @Override
        public int hashCode() {
            return hash;
        }
    }

    /**
     * An {@link org.protelis.vm.ExecutionContext} that operates over a node, but does not
     * modify it.
     */
    public static final class DummyContext extends AbstractExecutionContext {
        private static final Semaphore MUTEX = new Semaphore(1);
        private static final int SEED = -241837578;
        private static final RandomGenerator RNG = new MersenneTwister(SEED);
        private final Node<?> node;
        private DummyContext(final Node<?> node) {
            super(new ProtectedExecutionEnvironment(node), new NetworkManager() {
                @Override
                public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
                    return Collections.emptyMap();
                }
                @Override
                public void shareState(final Map<CodePath, Object> toSend) {
                }
            });
            this.node = node;
        }

        @Override
        public Number getCurrentTime() {
            return 0;
        }
        @Override
        public DeviceUID getDeviceUID() {
            if (node instanceof ProtelisNode) {
                return (ProtelisNode) node;
            }
            throw new IllegalStateException("You tried to compute a Protelis device UID, on a non-Protelis node");
        }
        @Override
        protected AbstractExecutionContext instance() {
            return this;
        }
        @Override
        public double nextRandomDouble() {
            final double result;
            MUTEX.acquireUninterruptibly();
            result = RNG.nextDouble();
            MUTEX.release();
            return result;
        }

    }

    /**
     * An {@link ExecutionEnvironment} that can read and shadow the content of a
     * {@link Node}, but cannot modify it. This is used to prevent badly written
     * properties to interact with the simulation flow.
     */
    public static final class ProtectedExecutionEnvironment implements ExecutionEnvironment {
        private final Node<?> node;
        private final ExecutionEnvironment shadow = new SimpleExecutionEnvironment();

        /**
         * @param node the {@link Node}
         */
        public ProtectedExecutionEnvironment(final Node<?> node) {
            this.node = node;
        }

        @Override
        public void commit() {
        }
        @Override
        public Object get(final String id) {
            return shadow.get(id, node.getConcentration(new SimpleMolecule(id)));
        }
        @Override
        public Object get(final String id, final Object defaultValue) {
            return Optional.ofNullable(get(id)).orElse(defaultValue);
        }
        @Override
        public boolean has(final String id) {
            return shadow.has(id) || node.contains(new SimpleMolecule(id));
        }
        @Override
        public boolean put(final String id, final Object v) {
            return shadow.put(id, v);
        }
        @Override
        public Object remove(final String id) {
            return shadow.remove(id);
        }
        @Override
        public void setup() {
        }

        @Override
        public Set<String> keySet() {
            return Sets.union(node.getContents().keySet().stream().map(Molecule::getName).collect(Collectors.toSet()), shadow.keySet());
        }
    }

    private static final class SynchronizedVM {
        private final CacheKey key;
        private final Semaphore mutex = new Semaphore(1);
        private final Optional<ProtelisVM> vm;
        private SynchronizedVM(final CacheKey key) {
            this.key = key;
            ProtelisVM myVM = null;
            if (!StringUtils.isBlank(key.property)) {
                try {
                    final String baseProgram = "env.get(\"" + key.molecule.getName() + "\")";
                    myVM = new ProtelisVM(
                            ProtelisLoader.parse(key.property.replace(VALUE_TOKEN, baseProgram)),
                            new DummyContext(key.node.get()));
                } catch (RuntimeException ex) { // NOPMD AvoidCatchingGenericException
                    L.warn("Program ignored as invalid: \n" + key.property);
                    L.debug("Debug information", ex);
                }
            }
            vm = Optional.ofNullable(myVM);
        }
        public Object runCycle() {
            final Node<Object> node = key.node.get();
            if (node == null) {
                throw new IllegalStateException("The node should never be null");
            }
            if (vm.isPresent()) {
                final ProtelisVM myVM = vm.get();
                mutex.acquireUninterruptibly();
                myVM.runCycle();
                mutex.release();
                return myVM.getCurrentValue();
            }
            if (node instanceof NoNode) {
                return key.property;
            }
            return node.getConcentration(key.molecule);
        }
    }

    private static final class NoNode implements Node<Object> {
        public static final NoNode INSTANCE = new NoNode();
        private static final long serialVersionUID = 1L;

        private <A> A notImplemented() {
            throw new UnsupportedOperationException("Method can't be invoked in this context.");
        }
        @Override @NotNull
        public Iterator<Reaction<Object>> iterator() {
            return notImplemented();
        }
        @Override
        public int compareTo(@NotNull final Node<Object> o) {
            return notImplemented();
        }
        @Override
        public void addReaction(final Reaction<Object> r) {
            notImplemented();
        }
        @Override
        public boolean contains(final Molecule mol) {
            return notImplemented();
        }
        @Override
        public int getChemicalSpecies() {
            return notImplemented();
        }
        @Override
        public Object getConcentration(final Molecule mol) {
            return notImplemented();
        }
        @Override
        public Map<Molecule, Object> getContents() {
            return notImplemented();
        }
        @Override
        public int getId() {
            return notImplemented();
        }
        @Override
        public List<Reaction<Object>> getReactions() {
            return null;
        }
        @Override
        public void removeConcentration(final Molecule mol) {
            notImplemented();
        }
        @Override
        public void removeReaction(final Reaction<Object> r) {
            notImplemented();
        }
        @Override
        public void setConcentration(final Molecule mol, final Object c) {
            notImplemented();
        }
        @Override
        public Node<Object> cloneNode(final Time t) {
            return notImplemented();
        }
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof NoNode;
        }
        @Override
        public int hashCode() {
            return -1;
        }
    }

}
