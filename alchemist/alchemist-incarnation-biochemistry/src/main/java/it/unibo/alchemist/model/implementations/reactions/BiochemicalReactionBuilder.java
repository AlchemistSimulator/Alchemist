/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.jirf.Factory;
import org.danilopianini.jirf.FactoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.kaikikm.threadresloader.ResourceLoader;

import it.unibo.alchemist.biochemistrydsl.BiochemistrydslBaseVisitor;
import it.unibo.alchemist.biochemistrydsl.BiochemistrydslLexer;
import it.unibo.alchemist.biochemistrydsl.BiochemistrydslParser;
import it.unibo.alchemist.biochemistrydsl.BiochemistrydslParser.ArgListContext;
import it.unibo.alchemist.biochemistrydsl.BiochemistrydslParser.BiochemicalReactionRightElemContext;
import it.unibo.alchemist.biochemistrydsl.BiochemistrydslParser.BiomoleculeContext;
import it.unibo.alchemist.exceptions.BiochemistryParseException;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.actions.AddJunctionInCell;
import it.unibo.alchemist.model.implementations.actions.AddJunctionInNeighbor;
import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInCell;
import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInEnv;
import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInNeighbor;
import it.unibo.alchemist.model.implementations.actions.RemoveJunctionInCell;
import it.unibo.alchemist.model.implementations.actions.RemoveJunctionInNeighbor;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInCell;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInEnv;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInNeighbor;
import it.unibo.alchemist.model.implementations.conditions.EnvPresent;
import it.unibo.alchemist.model.implementations.conditions.JunctionPresentInCell;
import it.unibo.alchemist.model.implementations.conditions.NeighborhoodPresent;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * This class implements a builder for chemical reactions.
 *
 * @param <P>
 */
public class BiochemicalReactionBuilder<P extends Position<P>> {

    private final BiochemistryIncarnation<P> incarnation;
    private final Node<Double> node;
    private final Environment<Double, P> env;
    private RandomGenerator rand;
    private TimeDistribution<Double> time;
    private String reactionString;


    /**
     * Construct a builder for biochemical reactions.
     * @param inc the current incarnation
     * @param currentNode the node where the reaction is placed.
     * @param environment the environment.
     */
    public BiochemicalReactionBuilder(final BiochemistryIncarnation<P> inc, final Node<Double> currentNode, final Environment<Double, P> environment) {
        incarnation = inc;
        node = currentNode;
        env = environment;
    }

    /**
     * Builds the chemical reaction.
     * @return a builded chemical reaction based on the given program
     */
    public Reaction<Double> build()  {
        checkReaction();
        final BiochemistrydslLexer lexer = new BiochemistrydslLexer(new ANTLRInputStream(reactionString));
        final BiochemistrydslParser parser = new BiochemistrydslParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new BiochemistryParseErrorListener(reactionString));
        final ParseTree tree = parser.reaction();
        final BiochemistryDSLVisitor<P> eval = new BiochemistryDSLVisitor<>(rand, incarnation, time, node, env);
        return eval.visit(tree);
    }

    private void checkReaction() {
        if (rand == null) {
            throw new IllegalArgumentException("Random generator cannot be null");
        }
        if (time == null) {
            throw new IllegalArgumentException("Time distribution cannot be null");
        }
        if (reactionString == null) {
            throw new IllegalArgumentException("Reaction string cannot be null");
        }
    }

    /**
     * Set the reaction to the passed program string.
     * @param program the string version of this reaction
     * @return .
     */
    public BiochemicalReactionBuilder<P> program(final String program) {
        reactionString = program;
        return this;
    }

    /**
     * set the random generator to the passed object.
     * @param rg the random generator.
     * @return .
     */
    public BiochemicalReactionBuilder<P> randomGenerator(final RandomGenerator rg) {
        rand = rg;
        return this;
    }

    /**
     * Set the time distribution to the passed object.
     * @param td the time distribution
     * @return .
     */
    public BiochemicalReactionBuilder<P> timeDistribution(final TimeDistribution<Double> td) {
        time = td;
        return this;
    }

    private static final class BiochemistryDSLVisitor<P extends Position<? extends P>> extends BiochemistrydslBaseVisitor<Reaction<Double>> {
        private static final String CONDITIONS_PACKAGE = "it.unibo.alchemist.model.implementations.conditions.";
        private static final String ACTIONS_PACKAGE = "it.unibo.alchemist.model.implementations.actions.";

        private final Factory factory;
        private final @Nonnull RandomGenerator rand;
        private final @Nonnull BiochemistryIncarnation<?> currentInc;
        private final @Nonnull TimeDistribution<Double> time;
        private final @Nonnull Node<Double> node;
        private final @Nonnull Environment<Double, P> env;
        private final @Nonnull Reaction<Double> reaction;
        private final List<Condition<Double>> conditionList = new ArrayList<>(0);
        private final List<Action<Double>> actionList = new ArrayList<>(0);
        private final Map<Biomolecule, Double> biomolConditionsInCell = new LinkedHashMap<>();
        private final Map<Biomolecule, Double> biomolConditionsInNeighbor = new LinkedHashMap<>();
        private final List<Junction> junctionList = new ArrayList<>();
        private boolean neighborActionPresent;
        private boolean envConditionPresent;
        private boolean envActionPresent;

        private BiochemistryDSLVisitor(@NotNull final RandomGenerator rand,
                                       @NotNull final BiochemistryIncarnation<?> incarnation,
                                       @NotNull final TimeDistribution<Double> timeDistribution,
                                       @NotNull final Node<Double> currentNode,
                                       @NotNull final Environment<Double, P> environment) {
            this.rand = rand;
            currentInc = incarnation;
            time = timeDistribution;
            this.node = currentNode;
            env = environment;
            reaction = new BiochemicalReaction(node, time, env);
            factory = new FactoryBuilder()
                    .withAutoBoxing()
                    .withBooleanIntConversions()
                    .withNarrowingConversions()
                    .withArrayBooleanIntConversions()
                    .withArrayNarrowingConversions()
                    .build();
            factory.registerSingleton(Incarnation.class, currentInc);
            factory.registerSingleton(Environment.class, environment);
            factory.registerSingleton(TimeDistribution.class, time);
            factory.registerSingleton(Node.class, node);
            factory.registerSingleton(Reaction.class, reaction);
            factory.registerSingleton(RandomGenerator.class, rand);
            factory.registerImplicit(String.class, Double.class, currentInc::createConcentration);
            factory.registerImplicit(String.class, Molecule.class, currentInc::createMolecule);
            factory.registerImplicit(String.class, Boolean.class, Boolean::parseBoolean);
        }

        @SuppressWarnings("unchecked")
        private <O> O createObject(final BiochemistrydslParser.JavaConstructorContext ctx, final String packageName) {
            String className = ctx.javaClass().getText();
            if (!className.contains(".")) {
                className = packageName + className;
            }
            try {
                final Class<O> clazz = (Class<O>) ResourceLoader.classForName(className);
                final ArgListContext lctx = ctx.argList();
                final List<Object> params = new ArrayList<>();
                if (lctx != null) { // if null there are no parameters, so params must be an empty List (as it is, actually)
                    lctx.arg().forEach(arg -> params.add((arg.decimal() != null) ? Double.parseDouble(arg.decimal().getText()) : arg.LITERAL().getText()));
                }
                return factory.build(clazz, params);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("cannot instance " + className + ", class not found");
            }
        }

        @Override 
        public Reaction<Double> visitBiochemicalReaction(final BiochemistrydslParser.BiochemicalReactionContext ctx) { 
            visit(ctx.biochemicalReactionLeft());
            visit(ctx.biochemicalReactionRight());
            if (ctx.customConditions() != null) {
                visit(ctx.customConditions());
            }
            if (ctx.customReactionType() != null) {
                visit(ctx.customReactionType());
            }
            /*
             * if the reaction has at least one neighbor action but no neighbor condition 
             * add the neighborhoodPresent condition.
             * This is necessary because if the node which contain this reaction don't have
             * a neighborhood and the reaction is valid (all conditions are valid) the neighbor action
             * is undefined, and can lead to unwanted behavior.
             */
            if (neighborActionPresent && biomolConditionsInNeighbor.isEmpty()) { 
                conditionList.add(new NeighborhoodPresent<>(env, node));
            }
            if (envActionPresent && !envConditionPresent) {
                conditionList.add(new EnvPresent(env, node));
            }
            reaction.setConditions(conditionList);
            reaction.setActions(actionList);
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionLeftInCellContext(final BiochemistrydslParser.BiochemicalReactionLeftInCellContextContext ctx) {
            for (final BiomoleculeContext b : ctx.biomolecule()) {
                final Biomolecule biomol = createBiomolecule(b);
                final double concentration = createConcentration(b);
                insertInMap(biomolConditionsInCell, biomol, concentration);
                conditionList.add(new BiomolPresentInCell(node, biomol, concentration));
                actionList.add(new ChangeBiomolConcentrationInCell(node, biomol, -concentration));
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionLeftInEnvContext(final BiochemistrydslParser.BiochemicalReactionLeftInEnvContextContext ctx) {
            for (final BiomoleculeContext b : ctx.biomolecule()) {
                final Biomolecule biomol = createBiomolecule(b);
                final double concentration = createConcentration(b);
                conditionList.add(new BiomolPresentInEnv<>(env, node, biomol, concentration));
                actionList.add(new ChangeBiomolConcentrationInEnv(node, biomol, env, rand));
                envConditionPresent = true;
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionLeftInNeighborContext(final BiochemistrydslParser.BiochemicalReactionLeftInNeighborContextContext ctx) {
            for (final BiomoleculeContext b : ctx.biomolecule()) {
                final Biomolecule biomol = createBiomolecule(b);
                final double concentration = createConcentration(b);
                insertInMap(biomolConditionsInNeighbor, biomol, concentration);
                conditionList.add(new BiomolPresentInNeighbor(env, node, biomol, concentration));
                actionList.add(new ChangeBiomolConcentrationInNeighbor(env, node, biomol, rand, -concentration));
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionRightInCellContext(final BiochemistrydslParser.BiochemicalReactionRightInCellContextContext ctx) {
            for (final BiochemicalReactionRightElemContext re : ctx.biochemicalReactionRightElem()) {
                if (re.biomolecule() != null) {
                    final Biomolecule biomol = createBiomolecule(re.biomolecule());
                    final double concentration = createConcentration(re.biomolecule());
                    actionList.add(new ChangeBiomolConcentrationInCell(node, biomol, concentration));
                } else if (re.javaConstructor() != null) {
                    actionList.add(createObject(re.javaConstructor(), ACTIONS_PACKAGE));
                }
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionRightInEnvContext(final BiochemistrydslParser.BiochemicalReactionRightInEnvContextContext ctx) {
            for (final BiochemicalReactionRightElemContext re : ctx.biochemicalReactionRightElem()) {
                if (re.biomolecule() != null) {
                    final Biomolecule biomol = createBiomolecule(re.biomolecule());
                    final double concentration = createConcentration(re.biomolecule());
                    actionList.add(new ChangeBiomolConcentrationInEnv(env, node, biomol, concentration, rand));
                } else if (re.javaConstructor() != null) {
                    actionList.add(createObject(re.javaConstructor(), ACTIONS_PACKAGE));
                }
                envActionPresent = true;
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionRightInNeighborContext(final BiochemistrydslParser.BiochemicalReactionRightInNeighborContextContext ctx) {
            for (final BiochemicalReactionRightElemContext re : ctx.biochemicalReactionRightElem()) {
                if (re.biomolecule() != null) {
                    final Biomolecule biomol = createBiomolecule(re.biomolecule());
                    final double concentration = createConcentration(re.biomolecule());
                    actionList.add(new ChangeBiomolConcentrationInNeighbor(env, node, biomol, rand, concentration));
                } else if (re.javaConstructor() != null) {
                    actionList.add(createObject(re.javaConstructor(), ACTIONS_PACKAGE));
                }
            }
            neighborActionPresent = true;
            return reaction;
        }

        @Override
        public Reaction<Double> visitCreateJunction(final BiochemistrydslParser.CreateJunctionContext ctx) { 
            visit(ctx.createJunctionLeft());
            visit(ctx.createJunctionRight());
            if (ctx.customConditions() != null) {
                visit(ctx.customConditions());
            }
            if (ctx.customReactionType() != null) {
                visit(ctx.customReactionType());
            }
            reaction.setConditions(conditionList);
            reaction.setActions(actionList);
            return reaction;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Reaction<Double> visitCreateJunctionJunction(final BiochemistrydslParser.CreateJunctionJunctionContext ctx) {
            final Junction j = createJunction(ctx.junction());
            j.getMoleculesInCurrentNode().forEach((k, v) -> {
                if (!biomolConditionsInCell.containsKey(k) || biomolConditionsInCell.get(k) < v) {
                    throw new BiochemistryParseException("The creation of the junction " + j + " requires " + v + " " + k + " in the current node, specify a greater or equal value in conditions.");
                }
            });
            j.getMoleculesInNeighborNode().forEach((k, v) -> {
                if (!biomolConditionsInNeighbor.containsKey(k) || biomolConditionsInNeighbor.get(k) < v) {
                    throw new BiochemistryParseException("The creation of the junction " + j + " requires " + v + " " + k + " in the neighbor node, specify a greater or equal value in conditions.");
                }
            });
            if (node instanceof CellNode) {
                actionList.add(new AddJunctionInCell(env, node, j, rand));
                actionList.add(new AddJunctionInNeighbor<>(env, (CellNode<P>) node, reverseJunction(j), rand));
            } else {
                throw new UnsupportedOperationException("Junctions are supported ONLY in CellNodes, not in " + node.getClass().getName());
            }
            return reaction;
        }

        @Override 
        public Reaction<Double> visitCustomCondition(final BiochemistrydslParser.CustomConditionContext ctx) {
            conditionList.add(createObject(ctx.javaConstructor(), CONDITIONS_PACKAGE));
            return reaction;
        }

        @Override
        public Reaction<Double> visitJunctionReaction(final BiochemistrydslParser.JunctionReactionContext ctx) { 
            visit(ctx.junctionReactionLeft());
            visit(ctx.junctionReactionRight());
            if (ctx.customConditions() != null) {
                visit(ctx.customConditions());
            }
            if (ctx.customReactionType() != null) {
                visit(ctx.customReactionType());
            }
            junctionList.forEach((j -> {
                if (node instanceof CellNode) {
                    actionList.add(new RemoveJunctionInCell(env, node, j, rand));
                    actionList.add(new RemoveJunctionInNeighbor(env, node, reverseJunction(j), rand));
                } else {
                    throw new UnsupportedOperationException("Junctions are supported ONLY in CellNodes, not in " + node.getClass().getName());
                }
            }));
            reaction.setConditions(conditionList);
            reaction.setActions(actionList);
            return reaction;
        }

        @Override
        public Reaction<Double> visitJunctionReactionJunction(final BiochemistrydslParser.JunctionReactionJunctionContext ctx) {
            final Junction j = createJunction(ctx.junction());
            if (!junctionList.remove(j)) { // the junction is not present in the list, witch means that this junction is undefined (e.g. [junction A-B] --> [junction C-D]
                throw new BiochemistryParseException("The junction " + j + " is not present in conditions.\n"
                        + "If you want to create the junction " + j + " do it on a separate reaction.");
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitJunctionReactionJunctionCondition(final BiochemistrydslParser.JunctionReactionJunctionConditionContext ctx) {
            if (node instanceof CellNode) {
                final Junction j = createJunction(ctx.junction());
                junctionList.add(j);
                conditionList.add(new JunctionPresentInCell(env, node, j));
                return reaction;
            } else {
                throw new UnsupportedOperationException("Junctions are supported ONLY in CellNodes, not in " + node.getClass().getName());
            }
        }

        private static Biomolecule createBiomolecule(final BiomoleculeContext ctx) {
            return new Biomolecule(ctx.name.getText());
        }

        // create the concentration of a biomolecule from a bio-molecular context
        private static double createConcentration(final BiomoleculeContext ctx) {
            return (ctx.concentration() == null) ? 1.0 : Double.parseDouble(ctx.concentration().POSDOUBLE().getText());
        }

        private static Junction createJunction(final BiochemistrydslParser.JunctionContext ctx) {
            final Map<Biomolecule, Double> currentNodeMolecules = new LinkedHashMap<>();
            final Map<Biomolecule, Double> neighborNodeMolecules = new LinkedHashMap<>();
            for (final BiomoleculeContext b : ctx.junctionLeft().biomolecule()) {
                insertInMap(currentNodeMolecules, createBiomolecule(b), createConcentration(b));
            }
            for (final BiomoleculeContext b : ctx.junctionRight().biomolecule()) {
                insertInMap(neighborNodeMolecules, createBiomolecule(b), createConcentration(b));
            }
            return new Junction(ctx.junctionLeft().getText() + "-" + ctx.junctionRight().getText(), currentNodeMolecules, neighborNodeMolecules);
        }

        private static void insertInMap(final Map<Biomolecule, Double> map, final Biomolecule mol, final double conc) {
            if (map.containsKey(mol)) {
                final double oldConc = map.get(mol);
                map.put(mol, oldConc + conc);
            } else {
                map.put(mol, conc);
            }
        }

        private static Junction reverseJunction(final Junction j) {
            final String[] split = j.getName().split("-");
            final String revName = split[1] + "-" + split[0];
            return new Junction(revName, j.getMoleculesInNeighborNode(), j.getMoleculesInCurrentNode());
        }
    }

    private static final class BiochemistryParseErrorListener implements ANTLRErrorListener {

        private final String reaction;

        private BiochemistryParseErrorListener(final String reactionString) {
            reaction = reactionString;
        }

        @Override
        public void reportAmbiguity(final Parser recognizer, 
                final DFA dfa, 
                final int startIndex,
                final int stopIndex,
                final boolean exact,
                final BitSet ambigAlts, 
                final ATNConfigSet configs) {
            throw new BiochemistryParseException("report ambiguity in " + reaction);
        }
        @Override
        public void reportAttemptingFullContext(final Parser recognizer,
                final DFA dfa,
                final int startIndex,
                final int stopIndex,
                final BitSet conflictingAlts,
                final ATNConfigSet configs) {
            throw new BiochemistryParseException("report attempting full context in " + reaction);
        }
        @Override
        public void reportContextSensitivity(final Parser recognizer,
                final DFA dfa, 
                final int startIndex,
                final int stopIndex,
                final int prediction,
                final ATNConfigSet configs) {
            throw new BiochemistryParseException("report context sensitivity in " + reaction);
        }

        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer,
                final Object offendingSymbol, 
                final int line,
                final int charPositionInLine,
                final String msg,
                final RecognitionException e) {
            throw new BiochemistryParseException("Error in reaction: " + reaction + "at character " + charPositionInLine + "\n" + msg);
        }
    }
}
