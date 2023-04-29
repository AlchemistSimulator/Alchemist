/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.exceptions.BiochemistryParseException;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.actions.AddJunctionInCell;
import it.unibo.alchemist.model.actions.AddJunctionInNeighbor;
import it.unibo.alchemist.model.actions.ChangeBiomolConcentrationInCell;
import it.unibo.alchemist.model.actions.ChangeBiomolConcentrationInEnv;
import it.unibo.alchemist.model.actions.ChangeBiomolConcentrationInNeighbor;
import it.unibo.alchemist.model.actions.RemoveJunctionInCell;
import it.unibo.alchemist.model.actions.RemoveJunctionInNeighbor;
import it.unibo.alchemist.model.conditions.BiomolPresentInCell;
import it.unibo.alchemist.model.conditions.BiomolPresentInEnv;
import it.unibo.alchemist.model.conditions.BiomolPresentInNeighbor;
import it.unibo.alchemist.model.conditions.EnvPresent;
import it.unibo.alchemist.model.conditions.JunctionPresentInCell;
import it.unibo.alchemist.model.conditions.NeighborhoodPresent;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.geometry.Vector;
import it.unibo.alchemist.model.interfaces.properties.CellProperty;
import it.unibo.alchemist.model.internal.biochemistry.dsl.BiochemistrydslBaseVisitor;
import it.unibo.alchemist.model.internal.biochemistry.dsl.BiochemistrydslLexer;
import it.unibo.alchemist.model.internal.biochemistry.dsl.BiochemistrydslParser;
import it.unibo.alchemist.model.internal.biochemistry.dsl.BiochemistrydslParser.ArgListContext;
import it.unibo.alchemist.model.internal.biochemistry.dsl.BiochemistrydslParser.BiochemicalReactionRightElemContext;
import it.unibo.alchemist.model.internal.biochemistry.dsl.BiochemistrydslParser.BiomoleculeContext;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.jirf.Factory;
import org.danilopianini.jirf.FactoryBuilder;
import org.kaikikm.threadresloader.ResourceLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class implements a builder for chemical reactions.
 *
 * @param <P> {@link Position} type
 */
public class BiochemicalReactionBuilder<P extends Position<P> & Vector<P>> {

    private final BiochemistryIncarnation incarnation;
    private final Node<Double> node;
    private final Environment<Double, P> environment;
    private RandomGenerator rand;
    private TimeDistribution<Double> time;
    private String reactionString;


    /**
     * Construct a builder for biochemical reactions.
     * @param incarnation the current incarnation
     * @param currentNode the node where the reaction is placed.
     * @param environment the environment.
     */
    public BiochemicalReactionBuilder(
            final BiochemistryIncarnation incarnation,
            final Node<Double> currentNode,
            final Environment<Double, P> environment
    ) {
        this.incarnation = incarnation;
        node = currentNode;
        this.environment = environment;
    }

    /**
     * Builds the chemical reaction.
     * @return a chemical reaction based on the given program
     */
    public Reaction<Double> build()  {
        checkReaction();
        final BiochemistrydslLexer lexer = new BiochemistrydslLexer(CharStreams.fromString(reactionString));
        final BiochemistrydslParser parser = new BiochemistrydslParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new BiochemistryParseErrorListener(reactionString));
        final ParseTree tree = parser.reaction();
        final BiochemistryDSLVisitor<P> eval = new BiochemistryDSLVisitor<>(rand, incarnation, time, node, environment);
        return Objects.requireNonNull(eval.visit(tree), "Unable to visit/parse " + reactionString);
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

    private static final class BiochemistryDSLVisitor<P extends Position<? extends P>>
            extends BiochemistrydslBaseVisitor<Reaction<Double>> {

        private static final String CONDITIONS_PACKAGE = "it.unibo.alchemist.model.conditions.";
        private static final String ACTIONS_PACKAGE = "it.unibo.alchemist.model.actions.";

        private final Factory factory;
        private final @Nonnull RandomGenerator rand;
        private final @Nonnull Node<Double> node;
        private final @Nullable CellProperty<Euclidean2DPosition> cell;
        private final @Nonnull Environment<Double, P> environment;
        private final @Nonnull Reaction<Double> reaction;
        private final List<Condition<Double>> conditionList = new ArrayList<>(0);
        private final List<Action<Double>> actionList = new ArrayList<>(0);
        private final Map<Biomolecule, Double> biomolConditionsInCell = new LinkedHashMap<>();
        private final Map<Biomolecule, Double> biomolConditionsInNeighbor = new LinkedHashMap<>();
        private final List<Junction> junctionList = new ArrayList<>();
        private boolean neighborActionPresent;
        private boolean envConditionPresent;
        private boolean envActionPresent;

        private BiochemistryDSLVisitor(
            @Nonnull final RandomGenerator rand,
            @Nonnull final BiochemistryIncarnation incarnation,
            @Nonnull final TimeDistribution<Double> timeDistribution,
            @Nonnull final Node<Double> currentNode,
            @Nonnull final Environment<Double, P> environment
        ) {
            this.rand = rand;
            this.node = currentNode;
            this.cell = node.asPropertyOrNull(CellProperty.class);
            this.environment = environment;
            reaction = new BiochemicalReaction(node, timeDistribution, this.environment, rand);
            factory = new FactoryBuilder()
                    .withAutoBoxing()
                    .withBooleanIntConversions()
                    .withNarrowingConversions()
                    .withArrayBooleanIntConversions()
                    .withArrayNarrowingConversions()
                    .build();
            factory.registerSingleton(Incarnation.class, incarnation);
            factory.registerSingleton(Environment.class, environment);
            factory.registerSingleton(TimeDistribution.class, timeDistribution);
            factory.registerSingleton(Node.class, node);
            factory.registerSingleton(Reaction.class, reaction);
            factory.registerSingleton(RandomGenerator.class, rand);
            factory.registerImplicit(String.class, Double.class, incarnation::createConcentration);
            factory.registerImplicit(String.class, Molecule.class, incarnation::createMolecule);
            factory.registerImplicit(String.class, Boolean.class, Boolean::parseBoolean);
        }

        @SuppressWarnings("unchecked")
        private <O> O createObject(
                final BiochemistrydslParser.JavaConstructorContext context,
                final String packageName
        ) {
            String className = context.javaClass().getText();
            if (!className.contains(".")) {
                className = packageName + className; // NOPMD UseStringBufferForStringAppends
            }
            try {
                final Class<O> clazz = (Class<O>) ResourceLoader.classForName(className);
                final ArgListContext lctx = context.argList();
                final List<Object> params = new ArrayList<>();
                if (lctx != null) { // if null there are no parameters, so params must be an empty List (as it is, actually)
                    lctx.arg().forEach(arg ->
                            params.add((arg.decimal() != null)
                                    ? Double.parseDouble(arg.decimal().getText())
                                    : arg.LITERAL().getText())
                    );
                }
                return factory.build(clazz, params).getCreatedObjectOrThrowException();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("cannot instance " + className + ", class not found", e);
            }
        }

        @Override 
        public Reaction<Double> visitBiochemicalReaction(final BiochemistrydslParser.BiochemicalReactionContext context) {
            visit(context.biochemicalReactionLeft());
            visit(context.biochemicalReactionRight());
            if (context.customConditions() != null) {
                visit(context.customConditions());
            }
            if (context.customReactionType() != null) {
                visit(context.customReactionType());
            }
            /*
             * if the reaction has at least one neighbor action but no neighbor condition 
             * add the neighborhoodPresent condition.
             * This is necessary because if the node which contain this reaction don't have
             * a neighborhood and the reaction is valid (all conditions are valid) the neighbor action
             * is undefined, and can lead to unwanted behavior.
             */
            if (neighborActionPresent && biomolConditionsInNeighbor.isEmpty()) { 
                conditionList.add(new NeighborhoodPresent<>(environment, node));
            }
            if (envActionPresent && !envConditionPresent) {
                conditionList.add(new EnvPresent(environment, node));
            }
            reaction.setConditions(conditionList);
            reaction.setActions(actionList);
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionLeftInCellContext(
                final BiochemistrydslParser.BiochemicalReactionLeftInCellContextContext context
        ) {
            for (final BiomoleculeContext b : context.biomolecule()) {
                final Biomolecule biomol = createBiomolecule(b);
                final double concentration = createConcentration(b);
                insertInMap(biomolConditionsInCell, biomol, concentration);
                conditionList.add(new BiomolPresentInCell(node, biomol, concentration));
                actionList.add(new ChangeBiomolConcentrationInCell(node, biomol, -concentration));
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionLeftInEnvContext(
                final BiochemistrydslParser.BiochemicalReactionLeftInEnvContextContext ctx
        ) {
            for (final BiomoleculeContext b : ctx.biomolecule()) {
                final Biomolecule biomol = createBiomolecule(b);
                final double concentration = createConcentration(b);
                conditionList.add(new BiomolPresentInEnv<>(environment, node, biomol, concentration));
                actionList.add(new ChangeBiomolConcentrationInEnv(node, biomol, environment, rand));
                envConditionPresent = true;
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionLeftInNeighborContext(
                final BiochemistrydslParser.BiochemicalReactionLeftInNeighborContextContext ctx
        ) {
            for (final BiomoleculeContext b : ctx.biomolecule()) {
                final Biomolecule biomol = createBiomolecule(b);
                final double concentration = createConcentration(b);
                insertInMap(biomolConditionsInNeighbor, biomol, concentration);
                conditionList.add(new BiomolPresentInNeighbor(environment, node, biomol, concentration));
                actionList.add(new ChangeBiomolConcentrationInNeighbor(rand, environment, node, biomol, -concentration));
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionRightInCellContext(
                final BiochemistrydslParser.BiochemicalReactionRightInCellContextContext ctx
        ) {
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
        public Reaction<Double> visitBiochemicalReactionRightInEnvContext(
                final BiochemistrydslParser.BiochemicalReactionRightInEnvContextContext context
        ) {
            for (final BiochemicalReactionRightElemContext re : context.biochemicalReactionRightElem()) {
                if (re.biomolecule() != null) {
                    final Biomolecule biomol = createBiomolecule(re.biomolecule());
                    final double concentration = createConcentration(re.biomolecule());
                    actionList.add(new ChangeBiomolConcentrationInEnv(environment, node, biomol, concentration, rand));
                } else if (re.javaConstructor() != null) {
                    actionList.add(createObject(re.javaConstructor(), ACTIONS_PACKAGE));
                }
                envActionPresent = true;
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitBiochemicalReactionRightInNeighborContext(
                final BiochemistrydslParser.BiochemicalReactionRightInNeighborContextContext context
        ) {
            for (final BiochemicalReactionRightElemContext re : context.biochemicalReactionRightElem()) {
                if (re.biomolecule() != null) {
                    final Biomolecule biomol = createBiomolecule(re.biomolecule());
                    final double concentration = createConcentration(re.biomolecule());
                    actionList.add(new ChangeBiomolConcentrationInNeighbor(rand, environment, node, biomol, concentration));
                } else if (re.javaConstructor() != null) {
                    actionList.add(createObject(re.javaConstructor(), ACTIONS_PACKAGE));
                }
            }
            neighborActionPresent = true;
            return reaction;
        }

        @Override
        public Reaction<Double> visitCreateJunction(final BiochemistrydslParser.CreateJunctionContext context) {
            visit(context.createJunctionLeft());
            visit(context.createJunctionRight());
            if (context.customConditions() != null) {
                visit(context.customConditions());
            }
            if (context.customReactionType() != null) {
                visit(context.customReactionType());
            }
            reaction.setConditions(conditionList);
            reaction.setActions(actionList);
            return reaction;
        }

        @Override
        public Reaction<Double> visitCreateJunctionJunction(
                final BiochemistrydslParser.CreateJunctionJunctionContext context
        ) {
            final Junction j = createJunction(context.junction());
            j.getMoleculesInCurrentNode().forEach((k, v) -> {
                if (!biomolConditionsInCell.containsKey(k) || biomolConditionsInCell.get(k) < v) {
                    throw new BiochemistryParseException(
                            "The creation of the junction " + j + " requires " + v + " " + k
                            + " in the current node, specify a greater or equal value in conditions."
                    );
                }
            });
            j.getMoleculesInNeighborNode().forEach((k, v) -> {
                if (!biomolConditionsInNeighbor.containsKey(k) || biomolConditionsInNeighbor.get(k) < v) {
                    throw new BiochemistryParseException(
                            "The creation of the junction " + j + " requires " + v + " " + k
                            + " in the neighbor node, specify a greater or equal value in conditions."
                    );
                }
            });
            if (cell != null) {
                actionList.add(new AddJunctionInCell(environment, node, j, rand));
                actionList.add(new AddJunctionInNeighbor<>(environment, node, reverseJunction(j), rand));
            } else {
                throw new UnsupportedOperationException(
                        "Junctions are supported ONLY in nodes with " + CellProperty.class.getSimpleName()
                );
            }
            return reaction;
        }

        @Override 
        public Reaction<Double> visitCustomCondition(final BiochemistrydslParser.CustomConditionContext context) {
            conditionList.add(createObject(context.javaConstructor(), CONDITIONS_PACKAGE));
            return reaction;
        }

        @Override
        public Reaction<Double> visitJunctionReaction(final BiochemistrydslParser.JunctionReactionContext context) {
            visit(context.junctionReactionLeft());
            visit(context.junctionReactionRight());
            if (context.customConditions() != null) {
                visit(context.customConditions());
            }
            if (context.customReactionType() != null) {
                visit(context.customReactionType());
            }
            junctionList.forEach(j -> {
                if (cell != null) {
                    actionList.add(new RemoveJunctionInCell(environment, node, j, rand));
                    actionList.add(new RemoveJunctionInNeighbor(environment, node, reverseJunction(j), rand));
                } else {
                    throw new UnsupportedOperationException(
                            "Junctions are supported ONLY in node with " + CellProperty.class.getSimpleName()
                    );
                }
            });
            reaction.setConditions(conditionList);
            reaction.setActions(actionList);
            return reaction;
        }

        @Override
        public Reaction<Double> visitJunctionReactionJunction(
                final BiochemistrydslParser.JunctionReactionJunctionContext context
        ) {
            final Junction j = createJunction(context.junction());
            if (!junctionList.remove(j)) {
                /*
                 * the junction is not present in the list,
                 * witch means that this junction is undefined
                 * (e.g. [junction A-B] --> [junction C-D]
                 */
                throw new BiochemistryParseException("The junction " + j + " is not present in conditions.\n"
                        + "If you want to create the junction " + j + " do it on a separate reaction.");
            }
            return reaction;
        }

        @Override
        public Reaction<Double> visitJunctionReactionJunctionCondition(
                final BiochemistrydslParser.JunctionReactionJunctionConditionContext context
        ) {
            if (cell != null) {
                final Junction j = createJunction(context.junction());
                junctionList.add(j);
                conditionList.add(new JunctionPresentInCell(environment, node, j));
                return reaction;
            } else {
                throw new UnsupportedOperationException(
                        "Junctions are supported ONLY in nodes with " + CellProperty.class.getSimpleName()
                );
            }
        }

        @Override
        public Reaction<Double> visitTerminal(final TerminalNode node) {
            return reaction;
        }

        private static Biomolecule createBiomolecule(final BiomoleculeContext ctx) {
            return new Biomolecule(ctx.name.getText());
        }

        // create the concentration of a biomolecule from a bio-molecular context
        private static double createConcentration(final BiomoleculeContext ctx) {
            return (ctx.concentration() == null) ? 1.0 : Double.parseDouble(ctx.concentration().POSDOUBLE().getText());
        }

        private static Junction createJunction(final BiochemistrydslParser.JunctionContext context) {
            final Map<Biomolecule, Double> currentNodeMolecules = new LinkedHashMap<>();
            final Map<Biomolecule, Double> neighborNodeMolecules = new LinkedHashMap<>();
            for (final BiomoleculeContext b : context.junctionLeft().biomolecule()) {
                insertInMap(currentNodeMolecules, createBiomolecule(b), createConcentration(b));
            }
            for (final BiomoleculeContext b : context.junctionRight().biomolecule()) {
                insertInMap(neighborNodeMolecules, createBiomolecule(b), createConcentration(b));
            }
            return new Junction(
                    context.junctionLeft().getText() + "-" + context.junctionRight().getText(),
                    currentNodeMolecules,
                    neighborNodeMolecules
            );
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
                final String message,
                final RecognitionException e) {
            throw new BiochemistryParseException(
                    "Error in reaction: " + reaction + "at character " + charPositionInLine + "\n" + message
            );
        }
    }
}
