/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.sapere.dsl.impl.Type;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.actions.LsaAllNeighborsAction;
import it.unibo.alchemist.model.sapere.actions.LsaRandomNeighborAction;
import it.unibo.alchemist.model.sapere.actions.LsaStandardAction;
import it.unibo.alchemist.model.sapere.conditions.LsaNeighborhoodCondition;
import it.unibo.alchemist.model.sapere.conditions.LsaStandardCondition;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import it.unibo.alchemist.model.sapere.nodes.LsaNode;
import it.unibo.alchemist.model.sapere.reactions.SAPEREReaction;
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime;
import it.unibo.alchemist.model.times.DoubleTime;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nullable;
import java.io.Serial;
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

    @Serial
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
        final @Nullable Object parameter
    ) {
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
        final @Nullable Object parameter
    ) {
        if (parameter == null || parameter.toString().isEmpty()) {
            return defaultTD(randomGenerator);
        }
        final String[] actualArgs = parameter.toString().split(",");
        return switch (actualArgs.length) {
            case 0 -> defaultTD(randomGenerator);
            case 1 -> new SAPEREExponentialTime(actualArgs[0], randomGenerator);
            case 2 ->
                new SAPEREExponentialTime(actualArgs[0], new DoubleTime(Double.parseDouble(actualArgs[1])), randomGenerator);
            default -> throw new IllegalArgumentException(parameter + " could not be used");
        };
    }

    @Override
    public Reaction<List<ILsaMolecule>> createReaction(
            final RandomGenerator randomGenerator,
            final Environment<List<ILsaMolecule>, P> environment,
            final Node<List<ILsaMolecule>> node,
            final TimeDistribution<List<ILsaMolecule>> timeDistribution,
            final @Nullable Object parameter) {
        final SAPEREReaction result = new SAPEREReaction(environment, (LsaNode) node, randomGenerator, timeDistribution);
        if (parameter != null && !parameter.toString().isEmpty()) {
            final Matcher rMatcher = MATCH_REACTION.matcher(parameter.toString());
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
                illegalSpec("must match regex " + REACTION_REGEX, parameter.toString());
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
        final Actionable<List<ILsaMolecule>> reaction,
        final @Nullable Object additionalParameters
    ) {
        Objects.requireNonNull(additionalParameters, "The condition can't be null. Reaction:" + reaction);
        if (additionalParameters.toString().startsWith("+")) {
            return new LsaNeighborhoodCondition(
                (LsaNode) node,
                createMolecule(additionalParameters.toString().substring(1)),
                environment
            );
        }
        return new LsaStandardCondition(createMolecule(additionalParameters.toString()), (LsaNode) node);
    }

    @Override
    public Action<List<ILsaMolecule>> createAction(
        final RandomGenerator randomGenerator,
        final Environment<List<ILsaMolecule>, P> environment,
        final Node<List<ILsaMolecule>> node,
        final TimeDistribution<List<ILsaMolecule>> time,
        final Actionable<List<ILsaMolecule>> actionable,
        final @Nullable Object additionalParameters
    ) {
        Objects.requireNonNull(additionalParameters, "The action parameter can't be null. Actionable:" + actionable);
        final var parameters = additionalParameters.toString();
        if (parameters.startsWith("+")) {
            return new LsaRandomNeighborAction(
                (LsaNode) node,
                createMolecule(parameters.substring(1)),
                environment,
                randomGenerator
            );
        }
        if (parameters.startsWith("*")) {
            return new LsaAllNeighborsAction(
                (LsaNode) node,
                createMolecule(parameters.substring(1)),
                environment
            );
        }
        return new LsaStandardAction(createMolecule(parameters), (LsaNode) node, randomGenerator);
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
