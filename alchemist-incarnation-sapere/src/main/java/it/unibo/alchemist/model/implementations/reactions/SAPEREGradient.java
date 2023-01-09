/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.reactions;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import it.unibo.alchemist.expressions.implementations.Expression;
import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.implementations.Type;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Dependency;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import org.danilopianini.lang.HashString;
import org.danilopianini.util.ImmutableListSet;
import org.danilopianini.util.ListSet;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fast and stable gradient implementation, inspired on
 * the NBR construct used in Proto.
 *
 * @param <P> Position type
 */
public final class SAPEREGradient<P extends Position<P>> extends AbstractReaction<List<ILsaMolecule>> {

    private static final List<ILsaMolecule> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>(0));
    private static final long serialVersionUID = 8362443887879500016L;
    private static final IExpression ZERO_NODE = new Expression(new NumTreeNode(0d));

    private final int argPosition;
    private boolean canRun = true;
    private List<? extends ILsaMolecule> contextCache;
    private final Environment<List<ILsaMolecule>, P> environment;
    private final List<Action<List<ILsaMolecule>>> fakeacts = new ArrayList<>(1);
    private final List<Condition<List<ILsaMolecule>>> fakeconds = new ArrayList<>(2);
    private TIntObjectMap<List<? extends ILsaMolecule>> gradCache = new TIntObjectHashMap<>();
    private final MapEnvironment<List<ILsaMolecule>, ?, ?> mapenvironment;
    private P mypos;

    private TIntObjectMap<P> positionCache = new TIntObjectHashMap<>();
    private final TIntDoubleMap routecache = new TIntDoubleHashMap(
            Constants.DEFAULT_CAPACITY,
            Constants.DEFAULT_LOAD_FACTOR,
            -1,
            Double.NaN
    );
    private final ILsaMolecule source, gradient, gradientExpr, context;
    private List<? extends ILsaMolecule> sourceCache;
    private final double threshold;

    /**
     * Builds a new SAPERE Gradient.
     * 
     * @param environment
     *            the current environment
     * @param node
     *            the node where this reaction is scheduled
     * @param sourceTemplate
     *            a template ILsaMolecule representing the source
     * @param gradientTemplate
     *            a template ILsaMolecule representing the gradient. ALL the
     *            variables MUST be the same of sourceTemplate: no un-instanced
     *            variables are admitted when inserting tuples into nodes
     * @param valuePosition
     *            the point at which the computation of the new values should be
     *            inserted. All the data after this position will be considered
     *            "additional information" propagated by the source. All the
     *            values before this one, instead, will be used to distinct
     *            different gradients
     * @param expression
     *            the expression to use in order to calculate the new gradient
     *            value. #T and #D are admitted, plus every variable present in
     *            the gradient before valuePosition, and every variable matched
     *            by the contextTemplate
     * @param contextTemplate
     *            a template ILsaMolecule. It can be used to match some contents
     *            of the local node in order to have local information to use
     *            in the gradient value computation
     * @param gradThreshold
     *            if the value of the gradient grows above this threshold, the
     *            gradient evaporates
     * @param timeDistribution
     *            Markovian Rate
     */
    public SAPEREGradient(
            final Environment<List<ILsaMolecule>, P> environment,
            final ILsaNode node,
            final ILsaMolecule sourceTemplate,
            final ILsaMolecule gradientTemplate,
            final int valuePosition,
            final String expression,
            final ILsaMolecule contextTemplate,
            final double gradThreshold,
            final TimeDistribution<List<ILsaMolecule>> timeDistribution
    ) {
        super(node, timeDistribution);
        setInputContext(Context.NEIGHBORHOOD);
        setOutputContext(Context.LOCAL);
        gradient = Objects.requireNonNull(gradientTemplate);
        source = Objects.requireNonNull(sourceTemplate);
        context = contextTemplate;
        this.environment = environment;
        if (valuePosition < 0) {
            throw new IllegalArgumentException("The position in the gradient LSA must be a positive integer");
        }
        argPosition = valuePosition;
        final IExpression exp = new Expression(expression);
        threshold = gradThreshold;
        final List<IExpression> grexp = gradient.allocateVar(null);
        grexp.set(argPosition, exp);
        gradientExpr = new LsaMolecule(grexp);
        /*
         * Dependency management: this reaction depends on the value of source in this node, the value of gradient in
         * the neighbors, and the value of the context locally. Moreover, the value may change if the neighborhood
         * changes, or if the node moves.
         */
        addOutboundDependency(gradient);
        addInboundDependency(source);
        addInboundDependency(Dependency.MOVEMENT);
        fakeconds.add(new SGFakeConditionAction(source));
        addInboundDependency(gradient);
        fakeacts.add(new SGFakeConditionAction(gradient));
        if (context != null) {
            addInboundDependency(context);
            fakeconds.add(new SGFakeConditionAction(context));
        }
        final boolean usesRoutes = this.environment instanceof MapEnvironment
                && (gradientTemplate.toString().contains(LsaMolecule.SYN_ROUTE)
                || expression.contains(LsaMolecule.SYN_ROUTE));
        mapenvironment = usesRoutes ? (MapEnvironment<List<ILsaMolecule>, ?, ?>) this.environment : null;
    }

    /**
     * Builds a new SAPERE Gradient. This constructor is slower, and is provided
     * for compatibility with the YAML-based Alchemist loader. It should be
     * avoided when possible, by relying on the other constructor instead.
     * 
     * @param environment
     *            the current environment
     * @param node
     *            the node where this reaction is scheduled
     * @param sourceTemplate
     *            a template ILsaMolecule representing the source
     * @param gradientTemplate
     *            a template ILsaMolecule representing the gradient. ALL the
     *            variables MUST be the same of sourceTemplate: no un-instanced
     *            variables are admitted when inserting tuples into nodes
     * @param valuePosition
     *            the point at which the computation of the new values should be
     *            inserted. All the data after this position will be considered
     *            "additional information" propagated by the source. All the
     *            values before this one, instead, will be used to distinct
     *            different gradients
     * @param expression
     *            the expression to use in order to calculate the new gradient
     *            value. #T and #D are admitted, plus every variable present in
     *            the gradient before valuePosition, and every variable matched
     *            by the contextTemplate
     * @param contextTemplate
     *            a template ILsaMolecule. It can be used to match some contents
     *            of the local node in order to have local information to use
     *            in the gradient value computation
     * @param gradThreshold
     *            if the value of the gradient grows above this threshold, the
     *            gradient evaporates
     * @param timeDistribution
     *            Markovian Rate
     */
    public SAPEREGradient(final Environment<List<ILsaMolecule>, P> environment,
            final ILsaNode node,
            final TimeDistribution<List<ILsaMolecule>> timeDistribution,
            final String sourceTemplate,
            final String gradientTemplate,
            final int valuePosition,
            final String expression,
            final String contextTemplate,
            final double gradThreshold) {
        this(
            environment,
            node,
            new LsaMolecule(sourceTemplate),
            new LsaMolecule(gradientTemplate),
            valuePosition,
            expression,
            new LsaMolecule(contextTemplate),
            gradThreshold,
            timeDistribution
        );
    }

    @Override
    public boolean canExecute() {
        return canRun;
    }

    /*
     * Clean up existing gradients. The new values will be computed upon
     * neighbors'
     */
    private List<ILsaMolecule> cleanUpExistingAndRecomputeFromSource(final Map<HashString, ITreeNode<?>> matches) {
        for (final ILsaMolecule g : getNode().getConcentration(gradient)) {
            getLsaNode().removeConcentration(g);
        }
        final List<ILsaMolecule> createdFromSource = new ArrayList<>(sourceCache.size());
        if (!sourceCache.isEmpty()) {
            matches.put(LsaMolecule.SYN_O, new NumTreeNode(getNode().getId()));
            for (final ILsaMolecule s : sourceCache) {
                for (int i = 0; i < source.size(); i++) {
                    final ITreeNode<?> uninstancedArg = source.getArg(i).getRootNode();
                    if (uninstancedArg.getType().equals(Type.VAR)) {
                        matches.put(uninstancedArg.toHashString(), s.getArg(i).getRootNode());
                    }
                }
                final List<IExpression> gl = gradient.allocateVar(matches);
                final ILsaMolecule m = new LsaMolecule(gl);
                createdFromSource.add(m);
                getLsaNode().setConcentration(m);
            }
        }
        return createdFromSource;
    }

    @Nonnull
    @Override
    public Reaction<List<ILsaMolecule>> cloneOnNewNode(
        @Nonnull final Node<List<ILsaMolecule>> node,
        @Nonnull final Time currentTime
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute() {
        if (sourceCache == null) {
            /*
             * First run
             */
            updateInternalStatus(Time.ZERO, true, environment);
        }
        canRun = false;
        final Map<HashString, ITreeNode<?>> matches = new HashMap<>();
        matches.put(LsaMolecule.SYN_T, new NumTreeNode(getTau().toDouble()));
        // PMD suppression: there is a side effect
        final List<ILsaMolecule> createdFromSource = cleanUpExistingAndRecomputeFromSource(matches); //NOPMD
        /*
         * Context computation: if there are contexts matched, use the first.
         * Otherwise, assign zero to every variable not yet instanced (to allow
         * computation to proceed).
         */
        if (!contextCache.isEmpty()) {
            final ILsaMolecule contextInstance = contextCache.get(0);
            for (int i = 0; i < context.argsNumber(); i++) {
                final ITreeNode<?> uninstancedArg = context.getArg(i).getRootNode();
                if (uninstancedArg.getType().equals(Type.VAR)) {
                    final HashString varName = uninstancedArg.toHashString();
                    final ITreeNode<?> matched = matches.get(varName);
                    final ITreeNode<?> localVal = contextInstance.getArg(i).getRootNode();
                    if (matched == null || matched.equals(localVal)) {
                        matches.put(varName, localVal);
                    } else {
                        throw new IllegalStateException("You are doing something nasty.");
                    }
                }
            }
        } else if (context != null) {
            for (int i = 0; i < context.argsNumber(); i++) {
                final ITreeNode<?> uninstancedArg = context.getArg(i).getRootNode();
                if (uninstancedArg.getType().equals(Type.VAR)) {
                    final HashString varName = uninstancedArg.toHashString();
                    final ITreeNode<?> matched = matches.get(varName);
                    if (matched == null) {
                        matches.put(varName, ZERO_NODE.getRootNode());
                    }
                }
            }
        }
        /*
         * All the gradients in the neighborhood which conflict with those
         * generated by a source should not be considered
         */
        final TIntObjectMap<List<? extends ILsaMolecule>> filteredGradCache;
        if (createdFromSource.isEmpty()) {
            filteredGradCache = gradCache;
        } else {
            filteredGradCache = new TIntObjectHashMap<>(gradCache.size());
            gradCache.forEachEntry(new Cleaner(createdFromSource, filteredGradCache));
        }
        /*
         * Gradients in neighborhood must be discovered
         */
        final List<ILsaMolecule> gradientsFound = new ArrayList<>();
        final GradientSearch gradSearch = new GradientSearch(gradientsFound, matches);
        filteredGradCache.forEachEntry(gradSearch);
        gradientsFound.addAll(createdFromSource);
        gradientsFound.forEach(grad -> getLsaNode().setConcentration(grad));
    }

    @Nonnull
    @Override
    public List<Action<List<ILsaMolecule>>> getActions() {
        return fakeacts;
    }

    @Nonnull
    @Override
    public List<Condition<List<ILsaMolecule>>> getConditions() {
        return fakeconds;
    }

    /**
     * @return the current node as {@link ILsaNode}
     */
    public ILsaNode getLsaNode() {
        return (ILsaNode) getNode();
    }

    @Override
    public double getRate() {
        return canRun ? getTimeDistribution().getRate() : 0;
    }

    @Override
    protected void updateInternalStatus(
            final Time currentTime,
            final boolean hasBeenExecuted,
            final Environment<List<ILsaMolecule>, ?> environment
    ) {
        /*
         * It makes sense to reschedule the reaction if:
         * 
         * the source has changed
         * 
         * the contextual information has changed
         * 
         * the neighbors have moved
         * 
         * the gradients in the neighborhood have changed
         * 
         * my position is changed
         */
        final List<? extends ILsaMolecule> sourceCacheTemp = getNode().getConcentration(source);
        final List<? extends ILsaMolecule> contextCacheTemp = context == null
                ? EMPTY_LIST
                : getNode().getConcentration(context);
        final TIntObjectMap<P> positionCacheTemp = new TIntObjectHashMap<>(positionCache.size());
        final TIntObjectMap<List<? extends ILsaMolecule>> gradCacheTemp = new TIntObjectHashMap<>(gradCache.size());
        final P curPos = this.environment.getPosition(getNode());
        final boolean positionChanged = !curPos.equals(mypos);
        boolean neighPositionChanged = false;
        for (final Node<List<ILsaMolecule>> n : this.environment.getNeighborhood(getNode())) {
            final P p = this.environment.getPosition(n);
            final int nid = n.getId();
            positionCacheTemp.put(nid, p);
            gradCacheTemp.put(n.getId(), n.getConcentration(gradient));
            final boolean pConstant = p.equals(positionCache.get(nid));
            if (!pConstant) {
                neighPositionChanged = true;
            }
            if (mapenvironment != null && (!pConstant || positionChanged)) {
                routecache.put(nid, mapenvironment.computeRoute(n, getNode()).length());
            }
        }
        if (!sourceCacheTemp.equals(sourceCache)
                || !contextCacheTemp.equals(contextCache)
                || neighPositionChanged
                || !gradCacheTemp.equals(gradCache)
                || positionChanged
        ) {
            sourceCache = sourceCacheTemp;
            contextCache = contextCacheTemp;
            positionCache = positionCacheTemp;
            gradCache = gradCacheTemp;
            mypos = curPos;
            canRun = true;
        }
    }

    private class Cleaner implements TIntObjectProcedure<List<? extends ILsaMolecule>> {
        private final List<ILsaMolecule> createdFromSource;
        private final TIntObjectMap<List<? extends ILsaMolecule>> filteredGradCache;

        Cleaner(final List<ILsaMolecule> cfs, final TIntObjectMap<List<? extends ILsaMolecule>> fgc) {
            createdFromSource = cfs;
            filteredGradCache = fgc;
        }

        @Override
        public boolean execute(final int a, final List<? extends ILsaMolecule> ol) {
            final List<ILsaMolecule> nl = new ArrayList<>(ol.size());
            for (final ILsaMolecule matchedGrad : ol) {
                boolean hasMatched = false;
                for (final ILsaMolecule sm : createdFromSource) {
                    int i = 0;
                    /*
                     * Check the exogenous gradients for all the arguments
                     * before the distance: if they match with at least one of
                     * the sources, they should not be considered
                     */
                    while (i < argPosition && matchedGrad.getArg(i).matches(sm.getArg(i), null)) {
                        i++;
                    }
                    if (i == argPosition) {
                        hasMatched = true;
                        break;
                    }
                }
                if (!hasMatched) {
                    nl.add(matchedGrad);
                }
            }
            filteredGradCache.put(a, nl);
            return true;
        }
    }

    private class GradientSearch implements TIntObjectProcedure<List<? extends ILsaMolecule>> {
        private final List<ILsaMolecule> gradientsFound;
        private final Map<HashString, ITreeNode<?>> matches;

        GradientSearch(final List<ILsaMolecule> gf, final Map<HashString, ITreeNode<?>> m) {
            gradientsFound = gf;
            matches = m;
        }

        @Override
        public boolean execute(final int a, final List<? extends ILsaMolecule> mgnList) {
            if (!mgnList.isEmpty()) {
                final P aPos = positionCache.get(a);
                final double distNode = aPos.distanceTo(mypos);
                matches.put(LsaMolecule.SYN_O, new NumTreeNode(a));
                matches.put(LsaMolecule.SYN_D, new NumTreeNode(distNode));
                if (mapenvironment != null) {
                    matches.put(LsaMolecule.SYN_ROUTE, new NumTreeNode(routecache.get(a)));
                }
                final Map<HashString, ITreeNode<?>> localMatches = mgnList.size() > 1 ? new HashMap<>(matches) : matches;
                for (final ILsaMolecule mgn : mgnList) {
                    /*
                     * Instance all the variables but synthetics.
                     */
                    for (int i = 0; i < gradient.size(); i++) {
                        final ITreeNode<?> uninstancedArg = gradient.getArg(i).getRootNode();
                        if (uninstancedArg.getType().equals(Type.VAR)) {
                            final HashString varName = uninstancedArg.toHashString();
                            if (!varName.toString().startsWith("#")) {
                                final ITreeNode<?> localVal = mgn.getArg(i).getRootNode();
                                localMatches.put(varName, localVal);
                            }
                        }
                    }
                    /*
                     * Compute new value
                     */
                    final List<IExpression> valuesFound = gradientExpr.allocateVar(localMatches);
                    if (gradientsFound.isEmpty()) {
                        if ((Double) valuesFound.get(argPosition).getRootNodeData() <= threshold) {
                            gradientsFound.add(new LsaMolecule(valuesFound));
                        }
                    } else {
                        boolean compatibleFound = false;
                        for (int j = 0; j < gradientsFound.size(); j++) {
                            final ILsaMolecule gradToCompare = gradientsFound.get(j);
                            int i = 0;
                            for (; i < argPosition; i++) {
                                if (!gradToCompare.getArg(i).matches(valuesFound.get(i), null)) {
                                    /*
                                     * Gradients are not compatible
                                     */
                                    break;
                                }
                            }
                            if (i == argPosition) {
                                /*
                                 * These two gradients are comparable
                                 */
                                compatibleFound = true;
                                final double newVal = (Double) valuesFound.get(argPosition).getRootNodeData();
                                final double oldVal = (Double) gradToCompare.getArg(argPosition).getRootNodeData();
                                if (newVal < oldVal) {
                                    gradientsFound.set(j, new LsaMolecule(valuesFound));
                                }
                            }
                        }
                        if (!compatibleFound && (Double) valuesFound.get(argPosition).getRootNodeData() < threshold) {
                            gradientsFound.add(new LsaMolecule(valuesFound));
                        }
                    }
                }
            }
            return true;
        }
    }

    private static class SGFakeConditionAction implements Action<List<ILsaMolecule>>, Condition<List<ILsaMolecule>> {
        private static final long serialVersionUID = 1L;
        private static final ListSet<Dependency> DEPENDENCY = ImmutableListSet.of(Dependency.EVERYTHING);
        private final Molecule mol;

        SGFakeConditionAction(final Molecule m) {
            super();
            mol = m;
        }

        @Override
        public Action<List<ILsaMolecule>> cloneAction(
                final Node<List<ILsaMolecule>> node,
                final Reaction<List<ILsaMolecule>> reaction
        ) {
            return null;
        }

        @Override
        public Condition<List<ILsaMolecule>> cloneCondition(
                final Node<List<ILsaMolecule>> node,
                final Reaction<List<ILsaMolecule>> reaction
        ) {
            return null;
        }

        @Override
        public void execute() {
        }

        @Override
        public Context getContext() {
            return null;
        }

        @Override
        public ListSet<? extends Dependency> getInboundDependencies() {
            return DEPENDENCY;
        }

        @Nonnull
        @Override
        public ListSet<? extends Dependency> getOutboundDependencies() {
            return DEPENDENCY;
        }

        @Override
        public Node<List<ILsaMolecule>> getNode() {
            return null;
        }

        @Override
        public double getPropensityContribution() {
            return 0;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public String toString() {
            return mol.toString();
        }

    }

}
