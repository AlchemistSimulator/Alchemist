/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.protelis;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeProperty;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.protelis.actions.RunProtelisProgram;
import it.unibo.alchemist.model.protelis.actions.SendToNeighbor;
import it.unibo.alchemist.model.protelis.conditions.ComputationalRoundComplete;
import it.unibo.alchemist.model.molecules.SimpleMolecule;
import it.unibo.alchemist.model.nodes.GenericNode;
import it.unibo.alchemist.protelis.properties.ProtelisDevice;
import it.unibo.alchemist.model.reactions.ChemicalReaction;
import it.unibo.alchemist.model.reactions.Event;
import it.unibo.alchemist.model.timedistributions.DiracComb;
import it.unibo.alchemist.model.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.times.DoubleTime;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serial;
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

/**
 * @param <P> position type
 */
public final class ProtelisIncarnation<P extends Position<P>> implements Incarnation<Object, P> {

    private static final Logger L = LoggerFactory.getLogger(ProtelisIncarnation.class);

    /**
     * The name that can be used in a property to refer to the extracted value.
     */
    public static final String VALUE_TOKEN = "<value>";
    /**
     * Statically-referenceable instance. This incarnation *can* work as a singleton, and doing so may save some
     * memory. However, it is not strictly a singleton (multiple instances do not do harm).
     */
    public static final ProtelisIncarnation<?> INSTANCE = new ProtelisIncarnation<>();

    private final LoadingCache<CacheKey, SynchronizedVM> cache = CacheBuilder
        .newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build(new CacheLoader<>() {
            @NotNull
            @Override
            public SynchronizedVM load(@Nonnull final CacheKey key) {
                return new SynchronizedVM(key);
            }
        });

    @Nonnull
    private static List<RunProtelisProgram<?>> getIncomplete(
            final Node<?> protelisNode,
            final List<RunProtelisProgram<?>> alreadyDone
    ) {
        return protelisNode.getReactions().stream()
                /*
                 * Get the actions
                 */
                .flatMap(r -> r.getActions().stream())
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

    @SuppressWarnings("unchecked")
    private void checkIsProtelisNode(final Node<Object> node, final String exceptionMessage) {
        if (node == null || node.asPropertyOrNull(ProtelisDevice.class) == null) {
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    @Override
    public Action<Object> createAction(
        final RandomGenerator randomGenerator,
        final Environment<Object, P> environment,
        final Node<Object> node,
        final TimeDistribution<Object> time,
        final Actionable<Object> actionable,
        final @Nullable Object additionalParameters
    ) {
        final String parameters = additionalParameters == null ? null : additionalParameters.toString();
        if (actionable instanceof Reaction) {
            Objects.requireNonNull(additionalParameters);
            final ProtelisDevice<P> device = node.asPropertyOrNull(ProtelisDevice.class);
            if (device == null) {
                throw new IllegalArgumentException("The node must be a " + ProtelisDevice.class.getSimpleName());
            }
            if ("send".equalsIgnoreCase(parameters)) {
                final List<RunProtelisProgram<?>> alreadyDone = node.getReactions()
                    .stream()
                    .flatMap(r -> r.getActions().stream())
                    .filter(a -> a instanceof SendToNeighbor)
                    .map(c -> ((SendToNeighbor) c).getProtelisProgram())
                    .collect(Collectors.toList());
                final List<RunProtelisProgram<?>> pList = getIncomplete(node, alreadyDone);
                if (pList.isEmpty()) {
                    throw new IllegalStateException(
                        "There is no program requiring a " + SendToNeighbor.class.getSimpleName() + " action"
                    );
                }
                if (pList.size() > 1) {
                    throw new IllegalStateException(
                        "There are too many programs requiring a " + SendToNeighbor.class.getName()
                            + " action: " + pList
                    );
                }
                return new SendToNeighbor(node, (Reaction<Object>) actionable, pList.get(0));
            } else {
                try {
                    return new RunProtelisProgram<>(
                        randomGenerator,
                        environment,
                        device,
                        (Reaction<Object>) actionable,
                        parameters
                    );
                } catch (RuntimeException exception) { // NOPMD AvoidCatchingGenericException
                    throw new IllegalArgumentException(
                        "Could not create the requested Protelis program: " + additionalParameters,
                        exception
                    );
                }
            }
        }
        throw new IllegalArgumentException(
            "The provided actionable must be an instance of " + Reaction.class.getSimpleName()
        );
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
    public Object createConcentration() {
        return null;
    }

    @Override
    public Condition<Object> createCondition(
        final RandomGenerator randomGenerator,
        final Environment<Object, P> environment,
        final Node<Object> node,
        final TimeDistribution<Object> time,
        final Actionable<Object> actionable,
        final @Nullable Object additionalParameters
    ) {
        if (actionable instanceof Reaction) {
            checkIsProtelisNode(node, "The node must have a " + ProtelisDevice.class.getSimpleName());
            /*
             * The list of ProtelisPrograms that have already been completed with a ComputationalRoundComplete condition
             */
            final List<RunProtelisProgram<?>> alreadyDone = node.getReactions()
                .stream()
                .flatMap(r -> r.getConditions().stream())
                .filter(c -> c instanceof ComputationalRoundComplete)
                .map(c -> ((ComputationalRoundComplete) c).getProgram())
                .collect(Collectors.toList());
            final List<RunProtelisProgram<?>> pList = getIncomplete(node, alreadyDone);
            if (pList.isEmpty()) {
                throw new IllegalStateException(
                    "There is no program requiring a " + ComputationalRoundComplete.class.getSimpleName() + " condition"
                );
            }
            if (pList.size() > 1) {
                throw new IllegalStateException(
                    "There are too many programs requiring a " + ComputationalRoundComplete.class.getSimpleName()
                        + " condition: " + pList
                );
            }
            return new ComputationalRoundComplete(node, pList.get(0));
        }
        throw new IllegalArgumentException(
            "The provided actionable should be an instance of " + Reaction.class.getSimpleName()
        );
    }

    @Override
    public Molecule createMolecule(final String s) {
        return new SimpleMolecule(Objects.requireNonNull(s));
    }

    @Override
    public Node<Object> createNode(
        final RandomGenerator randomGenerator,
        final Environment<Object, P> environment,
        final @Nullable Object parameter
    ) {
        final Node<Object> node = new GenericNode<>(this, environment);
        node.addProperty(new ProtelisDevice<>(environment, node));
        return node;
    }

    @Override
    public Reaction<Object> createReaction(
        final RandomGenerator randomGenerator,
        final Environment<Object, P> environment,
        final Node<Object> node,
        final TimeDistribution<Object> timeDistribution,
        final @Nullable Object parameter
    ) {
        final String parameterString = parameter == null ? null : parameter.toString();
        final boolean isSend = "send".equalsIgnoreCase(parameterString);
        final Reaction<Object> result = isSend
            ? new ChemicalReaction<>(Objects.requireNonNull(node), Objects.requireNonNull(timeDistribution))
            : new Event<>(node, timeDistribution);
        if (parameter != null) {
            result.setActions(
                Lists.newArrayList(
                    createAction(randomGenerator, environment, node, timeDistribution, result, parameter)
                )
            );
        }
        if (isSend) {
            result.setConditions(
                Lists.newArrayList(createCondition(
                    randomGenerator,
                    environment,
                    node,
                    timeDistribution,
                    result,
                    null
                    )
                )
            );
        }
        return result;
    }

    @Override
    public TimeDistribution<Object> createTimeDistribution(
        final RandomGenerator randomGenerator,
        final Environment<Object, P> environment,
        final Node<Object> node,
        final @Nullable Object parameter
    ) {
        if (parameter == null) {
            return new ExponentialTime<>(Double.POSITIVE_INFINITY, randomGenerator);
        }
        try {
            final double frequency = parameter instanceof Number ? ((Number) parameter).doubleValue()
                : Double.parseDouble(parameter.toString());
            return new DiracComb<>(new DoubleTime(randomGenerator.nextDouble() / frequency), frequency);
        } catch (final NumberFormatException e) {
            L.error("Unable to convert {} to a double", parameter);
            throw e;
        }
    }

    @Override
    public double getProperty(final Node<Object> node, final Molecule molecule, final String property) {
        try {
            final SynchronizedVM vm = cache.get(
                new CacheKey(
                    Objects.requireNonNull(node),
                    Objects.requireNonNull(molecule),
                    property == null ? "" : property
                )
            );
            final Object val = vm.runCycle();
            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            } else if (val instanceof String) {
                try {
                    return Double.parseDouble(val.toString());
                } catch (final NumberFormatException e) {
                    if (val.equals(property)) {
                        return 1;
                    }
                    return 0;
                }
            } else if (val instanceof final Boolean cond) {
                if (cond) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        } catch (ExecutionException | RuntimeException e) { // NOPMD: we never want getProperty to fail
            L.error(
                "Intercepted interpreter exception when computing: \n"
                    + property + "\n"
                    + e.getMessage()
            );
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
                    && ((CacheKey) obj).node.get() == node.get() // NOPMD: this comparison is intentional
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
    private static final class DummyContext extends AbstractExecutionContext<DummyContext> {
        private static final Semaphore MUTEX = new Semaphore(1);
        private static final int SEED = -241_837_578;
        private static final RandomGenerator RNG = new MersenneTwister(SEED);
        private static final DeviceUID NO_NODE_ID = new DeviceUID() {
            @Override
            public String toString() {
                return "Wapper over a non-ProtelisNode for an invalid DeviceUID, meant to host local-only computation.";
            }
        };
        private final Node<Object> node;

        private DummyContext(final Node<Object> node) {
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
        @SuppressFBWarnings("EI_EXPOSE_REP")
        public DeviceUID getDeviceUID() {
            final ProtelisDevice<?> protelisProperty = node.asPropertyOrNull(ProtelisDevice.class);
            return protelisProperty != null ? protelisProperty : NO_NODE_ID;
        }

        @Override
        protected DummyContext instance() {
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
     * Node, but cannot modify it. This is used to prevent badly written
     * properties to interact with the simulation flow.
     */
    public static final class ProtectedExecutionEnvironment implements ExecutionEnvironment {
        private final Node<?> node;
        private final ExecutionEnvironment shadow = new SimpleExecutionEnvironment();

        /**
         * @param node the {@link Node}
         */
        @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
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
            return Sets.union(
                    node.getContents().keySet().stream()
                            .map(Molecule::getName)
                            .collect(Collectors.toSet()), shadow.keySet()
            );
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
                try {
                    myVM.runCycle();
                    return myVM.getCurrentValue();
                } finally {
                    mutex.release();
                }
            }
            if (node instanceof NoNode) {
                return key.property;
            }
            return node.getConcentration(key.molecule);
        }
    }

    private static final class NoNode implements Node<Object> {
        public static final NoNode INSTANCE = new NoNode();
        @Serial
        private static final long serialVersionUID = 1L;

        private <A> A notImplemented() {
            throw new UnsupportedOperationException("Method can't be invoked in this context.");
        }

        @Override
        @Nonnull
        public Iterator<Reaction<Object>> iterator() {
            return notImplemented();
        }

        @Override
        public int compareTo(@Nonnull final Node<Object> o) {
            return notImplemented();
        }

        @Override
        public void addReaction(@NotNull final Reaction<Object> r) {
            notImplemented();
        }

        @Override
        public boolean contains(@NotNull final Molecule mol) {
            return notImplemented();
        }

        @Override
        public int getMoleculeCount() {
            return notImplemented();
        }

        @Override
        public Object getConcentration(@NotNull final Molecule mol) {
            return notImplemented();
        }

        @NotNull
        @Override
        public Map<Molecule, Object> getContents() {
            return notImplemented();
        }

        @Override
        public int getId() {
            return notImplemented();
        }

        @NotNull
        @Override
        public List<Reaction<Object>> getReactions() {
            return Collections.emptyList();
        }

        @Override
        public void removeConcentration(@NotNull final Molecule mol) {
            notImplemented();
        }

        @Override
        public void removeReaction(@NotNull final Reaction<Object> r) {
            notImplemented();
        }

        @Override
        public void setConcentration(@NotNull final Molecule mol, final Object c) {
            notImplemented();
        }

        @NotNull
        @Override
        public Node<Object> cloneNode(@NotNull final Time t) {
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


        @Override
        public void addProperty(@NotNull final NodeProperty<Object> nodeProperty) {
            notImplemented();
        }

        @NotNull
        @Override
        public List<NodeProperty<Object>> getProperties() {
            return Collections.emptyList();
        }

    }

}
