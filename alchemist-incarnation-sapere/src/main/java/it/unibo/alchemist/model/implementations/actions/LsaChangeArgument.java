/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.expressions.implementations.ConstTreeNode;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.lang.HashString;

import java.util.Arrays;
import java.util.List;


/**
 */
public final class LsaChangeArgument extends SAPERELocalAgent {

    private static final long serialVersionUID = -7128058274012426458L;
    private static final HashString OLD = new HashString("OldType");
    private final Environment<List<ILsaMolecule>, ?> environment;
    @SuppressFBWarnings(
            value = "SE_BAD_FIELD",
            justification = "All provided RandomGenerator implementations are actually Serializable"
    )
    private final RandomGenerator rnd;
    private final HashString newTargetVar;
    private final String[] listT;

    /**
     * Builds a new action that test neighbors which contain in their lsaSpace
     * an lsaMolecule matching {target,Type}. The effect of this Action is to
     * add to the matches map the variable PreferredType (the most present type
     * in neighborhood). The execution has no effect on influenced molecule of
     * reaction.
     * 
     * 
     * @param environment
     *            The environment to use
     * @param node
     *            The source node
     * @param listTarget
     *            Gradients list
     * @param targetVariable
     *            Variable name
     * @param random
     *            Random engine
     */
    public LsaChangeArgument(
            final Environment<List<ILsaMolecule>, ?> environment,
            final ILsaNode node,
            final String[] listTarget,
            final String targetVariable,
            final RandomGenerator random
    ) {
        super(node);
        rnd = random;
        this.environment = environment;
        newTargetVar = new HashString(targetVariable);
        listT = Arrays.copyOf(listTarget, listTarget.length);
    }

    @Override
    public void execute() {
        final List<String> listTarg = Arrays.asList(Arrays.copyOf(listT, listT.length));
        final String oldType = getMatches().get(OLD).toString();
        if (!listTarg.remove(oldType)) {
            throw new IllegalStateException("Cannot remove " + oldType + " from " + listTarg);
        }
        if (!listTarg.isEmpty()) {
            final String newTarg = listTarg.get((int) (listTarg.size() * rnd.nextDouble()));
            addMatch(newTargetVar, new ConstTreeNode(new HashString(newTarg)));
        }
    }

    @Override
    public String toString() {
        return "Find " + newTargetVar;
    }

    /**
     * @return the current environment
     */
    protected Environment<List<ILsaMolecule>, ?> getEnvironment() {
        return environment;
    }

    /**
     * @return a random integer
     */
    protected double random() {
        return rnd.nextInt();
    }

}
