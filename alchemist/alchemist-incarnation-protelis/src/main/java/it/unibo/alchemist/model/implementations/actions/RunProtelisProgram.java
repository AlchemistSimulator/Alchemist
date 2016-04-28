/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.lang.LangUtils;
import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ExecutionContext;
import org.protelis.vm.ProtelisVM;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.protelis.AlchemistExecutionContext;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;

/**
 */
@SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "This is desired.")
public class RunProtelisProgram extends SimpleMolecule implements Action<Object> {

    private static final long serialVersionUID = 2207914086772704332L;
    private final Environment<Object> environment;
    private final ProtelisNode node;
    private final Reaction<Object> reaction;
    private final org.protelis.vm.ProtelisProgram program;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
    private final RandomGenerator random;
    private transient ProtelisVM vm;
    private boolean computationalCycleComplete;

    /**
     * @param env
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     * @param rand
     *            the random engine
     * @param prog
     *            the Protelis program
     * @throws SecurityException
     *             if you are not authorized to load required classes
     * @throws ClassNotFoundException
     *             if required classes can not be found
     */
    public RunProtelisProgram(
            final Environment<Object> env,
            final ProtelisNode n,
            final Reaction<Object> r,
            final RandomGenerator rand,
            final String prog) throws SecurityException, ClassNotFoundException {
        this(env, n, r, rand, ProtelisLoader.parse(prog));
    }

    private RunProtelisProgram(
            final Environment<Object> env,
            final ProtelisNode n,
            final Reaction<Object> r,
            final RandomGenerator rand,
            final org.protelis.vm.ProtelisProgram prog) {
        super(prog.getName());
        LangUtils.requireNonNull(env, r, n, prog, rand);
        program = prog;
        environment = env;
        node = n;
        random = rand;
        reaction = r;
        final AlchemistNetworkManager netmgr = new AlchemistNetworkManager(environment, node, this);
        node.addNetworkManger(this, netmgr);
        final ExecutionContext ctx = new AlchemistExecutionContext(env, n, r, rand, netmgr);
        vm = new ProtelisVM(prog, ctx);
    }

    @Override
    public RunProtelisProgram cloneOnNewNode(final Node<Object> n, final Reaction<Object> r) {
        if (n instanceof ProtelisNode) {
            return new RunProtelisProgram(environment, (ProtelisNode) n, r, random, program);
        }
        throw new IllegalStateException("Can not load a Protelis program on a " + n.getClass()
                + ". A " + ProtelisNode.class + " is required.");
    }

    @Override
    public void execute() {
        vm.runCycle();
        node.setConcentration(this, vm.getCurrentValue());
        computationalCycleComplete = true;
    }

    /**
     * @return the environment
     */
    protected final Environment<Object> getEnvironment() {
        return environment;
    }

    /**
     * @return the node
     */
    protected final ProtelisNode getNode() {
        return node;
    }

    @Override
    public List<? extends Molecule> getModifiedMolecules() {
        /*
         * A Protelis program may modify any molecule (global variable)
         */
        return null;
    }

    @Override
    public Context getContext() {
        /*
         * A Protelis program never writes in other nodes
         */
        return Context.LOCAL;
    }

    /**
     * @return true if the Program has finished its last computation, and is ready to send a new message (used for dependency management)
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
        final AlchemistNetworkManager netmgr = new AlchemistNetworkManager(environment, node, this);
        node.addNetworkManger(this, netmgr);
        vm = new ProtelisVM(program, new AlchemistExecutionContext(environment, node, reaction, random, netmgr));
    }

}
