/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.expressions.implementations.Type;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.model.implementations.actions.LsaAllNeighborsAction;
import it.unibo.alchemist.model.implementations.actions.LsaRandomNeighborAction;
import it.unibo.alchemist.model.implementations.actions.LsaStandardAction;
import it.unibo.alchemist.model.implementations.conditions.LsaNeighborhoodCondition;
import it.unibo.alchemist.model.implementations.conditions.LsaStandardCondition;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.implementations.reactions.SAPEREReaction;
import it.unibo.alchemist.model.implementations.timedistributions.SAPEREExponentialTime;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @param <P> position type
 */
public final class SAPEREIncarnation<P extends Position<? extends P>>
        implements Incarnation<List<ILsaMolecule>, P>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CONDITION_GROUP = "condition";
    private static final String CONDITIONS_GROUP = "conditions";
    private static final String ACTION_GROUP = "action";
    private static final String ACTIONS_GROUP = "actions";
    private static final Pattern MATCH_REACTION;
    private static final Pattern MATCH_CONDITION;
    private static final Pattern MATCH_ACTION;
    private static final Pattern CONDITIONS_SEQUENCE;
    private static final Pattern ACTIONS_SEQUENCE;
    private static final String REACTION_REGEX;
    private int saperePropertyNumber = -1;
    private Molecule molCache;
    private String propCache;

    static {
        final String matchStart = "(?:\\s*(?<";
        final String condition = matchStart + CONDITION_GROUP + ">\\+";
        final String action = matchStart + ACTION_GROUP + ">[+*]";
        final String matchEnd = "?\\{[^\\{\\}]+?\\}))";
        MATCH_CONDITION = Pattern.compile(condition + matchEnd);
        MATCH_ACTION = Pattern.compile(action + matchEnd);
        final String sequence = matchEnd + "*\\s*";
        final String condSeq = condition + sequence;
        final String actSeq = action + sequence;
        CONDITIONS_SEQUENCE = Pattern.compile(condSeq);
        ACTIONS_SEQUENCE = Pattern.compile(actSeq);
        REACTION_REGEX = "(?<" + CONDITIONS_GROUP + ">" + condSeq + ")-->(?<" + ACTIONS_GROUP + ">" + actSeq + ")";
        MATCH_REACTION = Pattern.compile(REACTION_REGEX);
    }

    @SuppressFBWarnings(
            value = "ES_COMPARING_PARAMETER_STRING_WITH_EQ",
            justification = "Pointer comparison is intentional"
    )
    @Override
    public double getProperty(final Node<List<ILsaMolecule>> node, final Molecule molecule, final String property) {
        if (molecule instanceof ILsaMolecule && node instanceof ILsaNode && node.contains(molecule)) {
            boolean cacheUpdated = false;
            if (!molecule.equals(molCache) || property != propCache) { // NOPMD: reference comparison is intentional
                molCache = molecule;
                propCache = property;
                cacheUpdated = true;
            }
            return sapereProperty((ILsaNode) node, (ILsaMolecule) molecule, property, cacheUpdated);
        }
        return Double.NaN;
    }

    private double sapereProperty(
            final ILsaNode node,
            final ILsaMolecule molecule,
            final String prop,
            final boolean cacheUpdated
    ) {
        if (cacheUpdated) {
            saperePropertyNumber = -1;
            for (int i = 0; i < molecule.argsNumber() && saperePropertyNumber == -1; i++) {
                final IExpression arg = molecule.getArg(i);
                switch (arg.getRootNodeType()) {
                case COMPARATOR:
                    if (arg.getLeftChildren().toString().equals(prop)) {
                        saperePropertyNumber = i;
                    }
                    break;
                case VAR:
                    if (arg.getRootNode().toString().equals(prop)) {
                        saperePropertyNumber = i;
                    }
                    break;
                default: break;
                }
            }
        }
        if (saperePropertyNumber >= 0) {
            final List<ILsaMolecule> concentration = node.getConcentration(molecule);
            /*
             * Potential concurrency issue: a size check is mandatory
             */
            if (!concentration.isEmpty()) {
                final IExpression arg = concentration.get(0).getArg(saperePropertyNumber);
                if (arg.getRootNodeType().equals(Type.NUM)) {
                    return (double) arg.getRootNodeData();
                }
            }
        }
        return Double.NaN;
    }

    @Override
    public ILsaMolecule createMolecule(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        final String param = s.trim().startsWith("{") && s.endsWith("}") ? s.substring(1, s.length() - 1) : s;
        return new LsaMolecule(param);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public ILsaNode createNode(
            final RandomGenerator randomGenerator,
            final Environment<List<ILsaMolecule>, P> environment,
            final String parameter) {
        return new LsaNode(environment);
    }

    private static TimeDistribution<List<ILsaMolecule>> defaultTD(final RandomGenerator rand) {
        return new SAPEREExponentialTime("Infinity", rand);
    }

    @Override
    public TimeDistribution<List<ILsaMolecule>> createTimeDistribution(
            final RandomGenerator randomGenerator,
            final Environment<List<ILsaMolecule>, P> environment,
            final Node<List<ILsaMolecule>> node,
            final String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            return defaultTD(randomGenerator);
        }
        final String[] actualArgs = parameter.split(",");
        switch (actualArgs.length) {
        case 0:
            return defaultTD(randomGenerator);
        case 1:
            return new SAPEREExponentialTime(actualArgs[0], randomGenerator);
        case 2:
            return new SAPEREExponentialTime(actualArgs[0], new DoubleTime(Double.parseDouble(actualArgs[1])), randomGenerator);
        default:
            throw new IllegalArgumentException(parameter + " could not be used");
        }
    }

    @Override
    public Reaction<List<ILsaMolecule>> createReaction(
            final RandomGenerator randomGenerator,
            final Environment<List<ILsaMolecule>, P> environment,
            final Node<List<ILsaMolecule>> node,
            final TimeDistribution<List<ILsaMolecule>> timeDistribution,
            final String parameter) {
        final SAPEREReaction result = new SAPEREReaction(environment, (LsaNode) node, randomGenerator, timeDistribution);
        if (parameter != null && !parameter.isEmpty()) {
            final Matcher rMatcher = MATCH_REACTION.matcher(parameter);
            if (rMatcher.matches()) {
                final List<Condition<List<ILsaMolecule>>> conditions = new LinkedList<>();
                final String conditionsSpec = rMatcher.group(CONDITIONS_GROUP);
                if (CONDITIONS_SEQUENCE.matcher(conditionsSpec).matches()) {
                    final Matcher condMatcher = MATCH_CONDITION.matcher(conditionsSpec);
                    while (condMatcher.find()) {
                        final String condition = condMatcher.group(CONDITION_GROUP);
                        conditions.add(createCondition(randomGenerator, environment, node, timeDistribution, result, condition));
                    }
                } else {
                    illegalSpec(
                            "not a sequence of valid conditions"
                                    + "(curly bracket enclosed LSAs, with optional '+' prefix)",
                            conditionsSpec
                    );
                }
                final List<Action<List<ILsaMolecule>>> actions = new LinkedList<>();
                final String actionsSpec = rMatcher.group(ACTIONS_GROUP);
                if (ACTIONS_SEQUENCE.matcher(actionsSpec).matches()) {
                    final Matcher actMatcher = MATCH_ACTION.matcher(actionsSpec);
                    while (actMatcher.find()) {
                        final String action = actMatcher.group(ACTION_GROUP);
                        actions.add(createAction(randomGenerator, environment, node, timeDistribution, result, action));
                    }
                } else {
                    illegalSpec("not a sequence of valid conditions"
                                    + "(curly bracket enclosed LSAs, with optional '+' prefix)",
                            conditionsSpec
                    );
                }
                result.setConditions(conditions);
                result.setActions(actions);
            } else {
                illegalSpec("must match regex " + REACTION_REGEX, parameter);
            }
        }
        return result;
    }

    private static void illegalSpec(final String reason, final String origin) {
        throw new IllegalArgumentException("This is not a valid SAPERE reaction: " + reason
                + ". Problematic specification part: " + origin);
    }

    @Override
    public Condition<List<ILsaMolecule>> createCondition(
            final RandomGenerator randomGenerator,
            final Environment<List<ILsaMolecule>, P> environment,
            final Node<List<ILsaMolecule>> node,
            final TimeDistribution<List<ILsaMolecule>> time,
            final Reaction<List<ILsaMolecule>> reaction,
            final String additionalParameters
    ) {
        Objects.requireNonNull(additionalParameters, "The condition can't be null. Reaction:" + reaction);
        if (additionalParameters.startsWith("+")) {
            return new LsaNeighborhoodCondition((LsaNode) node, createMolecule(additionalParameters.substring(1)), environment);
        }
        return new LsaStandardCondition(createMolecule(additionalParameters), (LsaNode) node);
    }

    @Override
    public Action<List<ILsaMolecule>> createAction(
            final RandomGenerator randomGenerator,
            final Environment<List<ILsaMolecule>, P> environment,
            final Node<List<ILsaMolecule>> node,
            final TimeDistribution<List<ILsaMolecule>> time,
            final Reaction<List<ILsaMolecule>> reaction,
            final String additionalParameters
    ) {
        if (additionalParameters.startsWith("+")) {
            return new LsaRandomNeighborAction(
                    (LsaNode) node,
                    createMolecule(additionalParameters.substring(1)),
                    environment,
                    randomGenerator
            );
        }
        if (additionalParameters.startsWith("*")) {
            return new LsaAllNeighborsAction(
                    (LsaNode) node,
                    createMolecule(additionalParameters.substring(1)),
                    environment
            );
        }
        return new LsaStandardAction(createMolecule(additionalParameters), (LsaNode) node, randomGenerator);
    }

    @Override
    public List<ILsaMolecule> createConcentration(final String s) {
        return createConcentration();
    }

    @Override
    public List<ILsaMolecule> createConcentration() {
        return Collections.emptyList();
    }
}
