/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Dependency;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.capabilities.ProtelisCapability;
import it.unibo.alchemist.protelis.AlchemistExecutionContext;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.util.ImmutableListSet;
import org.danilopianini.util.ListSet;
import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ProtelisProgram;
import org.protelis.vm.ProtelisVM;

import java.io.IOException;
import java.io.ObjectInputStream;

import static it.unibo.alchemist.model.math.RealDistributionUtil.makeRealDistribution;
import static java.util.Objects.requireNonNull;

/**
 * @param <P> position type
 */
public final class RunProtelisProgram<P extends Position<P>> implements Action<Object> {

    private static final long serialVersionUID = 1L;
    private static final String
        EI_EXPOSE_REP = "EI_EXPOSE_REP",
        INTENTIONAL = "This is intentional";
    private boolean computationalCycleComplete;
    private final Environment<Object, P> environment;
    private final Molecule name;
    private final Node<Object> node;
    private String originalProgram = "unknown";
    private final ProtelisProgram program;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
    private final RandomGenerator random;
    private final Reaction<Object> reaction;
    private final double retentionTime;
    private final AlchemistNetworkManager networkManager;
    private transient ProtelisVM vm;
    private transient AlchemistExecutionContext<P> executionContext;

    private RunProtelisProgram(
            final Environment<Object, P> environment,
            final Node<Object> node,
            final Reaction<Object> reaction,
            final RandomGenerator randomGenerator,
            final ProtelisProgram program,
            final double retentionTime,
            final RealDistribution packetLossDistance
    ) {
        final var otherCopies = node.getReactions().stream()
            .flatMap(it -> it.getActions().stream())
            .filter(it -> it instanceof RunProtelisProgram)
            .map(it -> ((RunProtelisProgram) it).program.getName())
            .filter(programName -> programName.equals(program.getName()))
            .count();
        name = new SimpleMolecule(program.getName() + (otherCopies == 0 ? "" : "$copy" + otherCopies));
        this.program = requireNonNull(program);
        this.environment = requireNonNull(environment);
        this.node = requireNonNull(node);
        random = requireNonNull(randomGenerator);
        this.reaction = requireNonNull(reaction);
        networkManager = new AlchemistNetworkManager(reaction, this, retentionTime, packetLossDistance);
        this.node.asCapability(ProtelisCapability.class).addNetworkManger(this, networkManager);
        executionContext = new AlchemistExecutionContext<>(environment, node, reaction, randomGenerator, networkManager);
        vm = new ProtelisVM(program, executionContext);
        this.retentionTime = retentionTime;
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param randomGenerator
     *            the random engine
     * @param program
     *            the Protelis program
     * @throws SecurityException
     *             if you are not authorized to load required classes
     */
    public RunProtelisProgram(
            final Environment<Object, P> environment,
            final Node<Object> node,
            final Reaction<Object> reaction,
            final RandomGenerator randomGenerator,
            final String program
    ) {
        this(environment, node, reaction, randomGenerator, program, Double.NaN);
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param randomGenerator
     *            the random engine
     * @param program
     *            the Protelis program
     * @param retentionTime
     *            how long the messages will be stored. Pass {@link Double#NaN}
     *            to mean that they should get eliminated upon node awake.
     * @throws SecurityException
     *             if you are not authorized to load required classes
     */
    public RunProtelisProgram(
            final Environment<Object, P> environment,
            final Node<Object> node,
            final Reaction<Object> reaction,
            final RandomGenerator randomGenerator,
            final String program,
            final double retentionTime
    ) {
        this(environment, node, reaction, randomGenerator, program, retentionTime, null);
        originalProgram = program;
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param randomGenerator
     *            the random engine
     * @param program
     *            the Protelis program
     * @param packetLossDistributionName
     *            the package loss probability, scaling with distance.
     *            This is the name of the {@link RealDistribution} to be used as follows:
     *            its PDF will be computed with {@link RealDistribution#density(double)},
     *            and will be fed the distance between the current node and the neighbor;
     *            the generated probability will in turn be used to determine the probability of the package to be
     *            successfully delivered.
     * @param packetLossDistributionParameters
     *            parameters that will be passed when building the packet loss distribution
     * @throws SecurityException
     *             if you are not authorized to load required classes
     */
    public RunProtelisProgram(
            final Environment<Object, P> environment,
            final Node<Object> node,
            final Reaction<Object> reaction,
            final RandomGenerator randomGenerator,
            final String program,
            final String packetLossDistributionName,
            final double... packetLossDistributionParameters
    ) {
        this(
            environment,
            node,
            reaction,
            randomGenerator,
            program,
            Double.NaN,
            makeRealDistribution(randomGenerator, packetLossDistributionName, packetLossDistributionParameters)
        );
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param randomGenerator
     *            the random engine
     * @param program
     *            the Protelis program
     * @param retentionTime
     *            how long the messages will be stored. Pass {@link Double#NaN}
     *            to mean that they should get eliminated upon node awake.
     * @param packetLossDistributionName
     *            the package loss probability, scaling with distance.
     *            This is the name of the {@link RealDistribution} to be used as follows:
     *            its PDF will be computed with {@link RealDistribution#density(double)},
     *            and will be fed the distance between the current node and the neighbor;
     *            the generated probability will in turn be used to determine the probability of the package to be
     *            successfully delivered.
     * @param packetLossDistributionParameters
     *            parameters that will be passed when building the packet loss distribution
     * @throws SecurityException
     *             if you are not authorized to load required classes
     */
    public RunProtelisProgram(
            final Environment<Object, P> environment,
            final Node<Object> node,
            final Reaction<Object> reaction,
            final RandomGenerator randomGenerator,
            final String program,
            final double retentionTime,
            final String packetLossDistributionName,
            final double... packetLossDistributionParameters
    ) {
        this(
            environment,
            node,
            reaction,
            randomGenerator,
            program,
            retentionTime,
            makeRealDistribution(randomGenerator, packetLossDistributionName, packetLossDistributionParameters)
        );
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param randomGenerator
     *            the random engine
     * @param program
     *            the Protelis program
     * @param retentionTime
     *            how long the messages will be stored. Pass {@link Double#NaN}
     *            to mean that they should get eliminated upon node awake.
     * @param packetLossDistributionName
     *            the package loss probability, scaling with distance.
     *            This is the name of the {@link RealDistribution} to be used as follows:
     *            its PDF will be computed with {@link RealDistribution#density(double)},
     *            and will be fed the distance between the current node and the neighbor;
     *            the generated probability will in turn be used to determine the probability of the package to be
     *            successfully delivered.
     * @param packetLossDistributionParameters
     *            parameters that will be passed when building the packet loss distribution
     * @throws SecurityException
     *             if you are not authorized to load required classes
     */
    public RunProtelisProgram(
            final Environment<Object, P> environment,
            final Node<Object> node,
            final Reaction<Object> reaction,
            final RandomGenerator randomGenerator,
            final ProtelisProgram program,
            final double retentionTime,
            final String packetLossDistributionName,
            final double... packetLossDistributionParameters
    ) {
        this(
            environment,
            node,
            reaction,
            randomGenerator,
            program,
            retentionTime,
            makeRealDistribution(randomGenerator, packetLossDistributionName, packetLossDistributionParameters)
        );
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param randomGenerator
     *            the random engine
     * @param program
     *            the Protelis program
     * @param retentionTime
     *            how long the messages will be stored. Pass {@link Double#NaN}
     *            to mean that they should get eliminated upon node awake.
     * @param packetLossDistance
     *            the package loss probability, scaling with distance.
     *            This {@link RealDistribution} will be used as follows:
     *            its PDF will be computed with {@link RealDistribution#density(double)},
     *            and will be fed the distance between the current node and the neighbor;
     *            the generated probability will in turn be used to determine the probability of the package to be
     *            successfully delivered. Can be null, in which case packets always arrive to neighbors.
     * @throws SecurityException
     *             if you are not authorized to load required classes
     */
    public RunProtelisProgram(
            final Environment<Object, P> environment,
            final Node<Object> node,
            final Reaction<Object> reaction,
            final RandomGenerator randomGenerator,
            final String program,
            final double retentionTime,
            final RealDistribution packetLossDistance
    ) {
        this(
            environment,
            node,
            reaction,
            randomGenerator,
            ProtelisLoader.parse(program),
            retentionTime,
            packetLossDistance
        );
        originalProgram = program;
    }

    /**
     * @return the molecule associated with the execution of this program
     */
    public Molecule asMolecule() {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RunProtelisProgram<P> cloneAction(final Node<Object> node, final Reaction<Object> reaction) {
        if (node.asCapabilityOrNull(ProtelisCapability.class) != null) {
            try {
                return new RunProtelisProgram<P>(
                    getEnvironment(),
                    node,
                    reaction,
                    getRandomGenerator(),
                    originalProgram,
                    getRetentionTime()
                );
            } catch (SecurityException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("The node must have an instance of " + ProtelisCapability.class.getSimpleName());
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other != null && other.getClass() == getClass()) {
            final var otherProgram = (RunProtelisProgram<?>) other;
            return name.equals(otherProgram.name);
        }
        return false;
    }

    @Override
    public void execute() {
        vm.runCycle();
        node.setConcentration(name, vm.getCurrentValue());
        computationalCycleComplete = true;
    }

    @Override
    public Context getContext() {
        /*
         * A Protelis program never writes in other nodes
         */
        return Context.LOCAL;
    }

    /**
     * @return the environment
     */
    @SuppressFBWarnings(value = EI_EXPOSE_REP, justification = INTENTIONAL)
    public Environment<Object, P> getEnvironment() {
        return environment;
    }

    @Override
    public ListSet<? extends Dependency> getOutboundDependencies() {
        return ImmutableListSet.of(Dependency.EVERY_MOLECULE);
    }

    /**
     * @return the node
     */
    @SuppressFBWarnings(value = EI_EXPOSE_REP, justification = INTENTIONAL)
    public Node<Object> getNode() {
        return node;
    }

    /**
     * @return the internal {@link RandomGenerator}
     */
    @SuppressFBWarnings(value = EI_EXPOSE_REP, justification = INTENTIONAL)
    public RandomGenerator getRandomGenerator() {
        return random;
    }

    /**
     * @return the retention time
     */
    public double getRetentionTime() {
        return retentionTime;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * @return true if the Program has finished its last computation,
     * and is ready to send a new message (used for dependency management)
     */
    public boolean isComputationalCycleComplete() {
        return computationalCycleComplete;
    }

    /**
     * Resets the computation status (used for dependency management).
     */
    public void prepareForComputationalCycle() {
        this.computationalCycleComplete = false;
    }

    private void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
        executionContext = new AlchemistExecutionContext<>(environment, node, reaction, random, networkManager);
        vm = new ProtelisVM(program, executionContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name + "@" + node.getId();
    }

    /**
     * Provides an access to the underlying {@link org.protelis.vm.ExecutionContext}.
     *
     * @return the current {@link AlchemistExecutionContext}
     */
    @SuppressFBWarnings(value = EI_EXPOSE_REP, justification = INTENTIONAL)
    public AlchemistExecutionContext<P> getExecutionContext() {
        return executionContext;
    }
}
