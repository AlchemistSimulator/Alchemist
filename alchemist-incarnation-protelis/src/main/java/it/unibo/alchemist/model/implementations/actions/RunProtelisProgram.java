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
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Dependency;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.protelis.AlchemistExecutionContext;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.util.ImmutableListSet;
import org.danilopianini.util.ListSet;
import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ProtelisVM;

import java.io.IOException;
import java.io.ObjectInputStream;

import static java.util.Objects.requireNonNull;

/**
 * @param <P> position type
 */
@SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "This is desired.")
public final class RunProtelisProgram<P extends Position<P>> implements Action<Object> {

    private static final long serialVersionUID = 1L;
    private boolean computationalCycleComplete;
    private final Environment<Object, P> environment;
    private final Molecule name;
    private final ProtelisNode<P> node;
    private String originalProgram = "unknown";
    private final org.protelis.vm.ProtelisProgram program;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
    private final RandomGenerator random;
    private final Reaction<Object> reaction;
    private final double retentionTime;
    private transient ProtelisVM vm;
    private transient AlchemistExecutionContext<P> executionContext;

    private RunProtelisProgram(
            final Environment<Object, P> env,
            final ProtelisNode<P> n,
            final Reaction<Object> r,
            final RandomGenerator rand,
            final org.protelis.vm.ProtelisProgram prog,
            final double retentionTime) {
        name = new SimpleMolecule(prog.getName());
        program = requireNonNull(prog);
        environment = requireNonNull(env);
        node = requireNonNull(n);
        random = requireNonNull(rand);
        reaction = requireNonNull(r);
        final AlchemistNetworkManager netmgr = new AlchemistNetworkManager(environment, node, reaction, this, retentionTime);
        node.addNetworkManger(this, netmgr);
        executionContext = new AlchemistExecutionContext<>(env, n, r, rand, netmgr);
        vm = new ProtelisVM(prog, executionContext);
        this.retentionTime = retentionTime;
    }

    /**
     * @param env
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     * @param rand
     *            the random engine
     * @param program
     *            the Protelis program
     * @throws SecurityException
     *             if you are not authorized to load required classes
     */
    public RunProtelisProgram(
            final Environment<Object, P> env,
            final ProtelisNode<P> n,
            final Reaction<Object> r,
            final RandomGenerator rand,
            final String program) {
        this(env, n, r, rand, program, Double.NaN);
    }

    /**
     * @param env
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     * @param rand
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
            final Environment<Object, P> env,
            final ProtelisNode<P> n,
            final Reaction<Object> r,
            final RandomGenerator rand,
            final String program,
            final double retentionTime) {
        this(env, n, r, rand, ProtelisLoader.parse(program), retentionTime);
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
        if (node instanceof ProtelisNode) {
            try {
                return new RunProtelisProgram<>(
                    getEnvironment(),
                    (ProtelisNode<P>) node,
                    reaction,
                    getRandomGenerator(),
                    originalProgram,
                    getRetentionTime()
                );
            } catch (SecurityException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("The node must be a " + ProtelisNode.class.getSimpleName());
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other != null && other.getClass() == getClass()) {
            return name.equals(((RunProtelisProgram<?>) other).name);
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
    public ProtelisNode getNode() {
        return node;
    }

    /**
     * @return the internal {@link RandomGenerator}
     */
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
        final AlchemistNetworkManager netmgr = new AlchemistNetworkManager(environment, node, reaction, this);
        node.addNetworkManger(this, netmgr);
        executionContext = new AlchemistExecutionContext<>(environment, node, reaction, random, netmgr);
        vm = new ProtelisVM(program, executionContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name + "@" + node.getId();
    }

    public AlchemistExecutionContext<P> getExecutionContext() {
        return executionContext;
    }
}
