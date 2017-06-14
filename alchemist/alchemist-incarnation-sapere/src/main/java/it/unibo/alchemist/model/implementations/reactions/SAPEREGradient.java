/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.reactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.danilopianini.lang.ExactHashObjectMap;
import org.danilopianini.lang.util.FasterString;
import org.danilopianini.util.ListSet;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
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

/**
 * This class provides a fast and stable gradient implementation, inspired on
 * the NBR construct used in Proto.
 * 
 */
public class SAPEREGradient extends AReaction<List<ILsaMolecule>> {

    private static final List<ILsaMolecule> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<ILsaMolecule>(0));
    private static final long serialVersionUID = 8362443887879500016L;
    private static final IExpression ZERO_NODE = new Expression(new NumTreeNode(0d));

    private final int argPosition;
    private final Environment<List<ILsaMolecule>> environment;
    private final MapEnvironment<List<ILsaMolecule>> mapenvironment;
    private final List<Action<List<ILsaMolecule>>> fakeacts = new ArrayList<>(1);
    private final List<Condition<List<ILsaMolecule>>> fakeconds = new ArrayList<>(2);
    private final TIntDoubleMap routecache = new TIntDoubleHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1, Double.NaN);
    private final ILsaNode node;
    private final ILsaMolecule source, gradient, gradientExpr, context;
    private final double threshold;

    private boolean canRun = true;
    private List<? extends ILsaMolecule> contextCache;
    private TIntObjectMap<List<? extends ILsaMolecule>> gradCache = new ExactHashObjectMap<>();
    private Position mypos;
    private TIntObjectMap<Position> positionCache = new ExactHashObjectMap<>();
    private List<? extends ILsaMolecule> sourceCache;

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
        private final Map<FasterString, ITreeNode<?>> matches;

        GradientSearch(final List<ILsaMolecule> gf, final Map<FasterString, ITreeNode<?>> m) {
            gradientsFound = gf;
            matches = m;
        }

        @Override
        public boolean execute(final int a, final List<? extends ILsaMolecule> mgnList) {
            if (!mgnList.isEmpty()) {
                final Position aPos = positionCache.get(a);
                final double distNode = aPos.getDistanceTo(mypos);
                matches.put(LsaMolecule.SYN_O, new NumTreeNode(a));
                matches.put(LsaMolecule.SYN_D, new NumTreeNode(distNode));
                if (mapenvironment != null) {
                    matches.put(LsaMolecule.SYN_ROUTE, new NumTreeNode(routecache.get(a)));
                }
                final Map<FasterString, ITreeNode<?>> localMatches = mgnList.size() > 1 ? new HashMap<>(matches) : matches;
                for (final ILsaMolecule mgn : mgnList) {
                    /*
                     * Instance all the variables but synthetics.
                     */
                    for (int i = 0; i < gradient.size(); i++) {
                        final ITreeNode<?> uninstancedArg = gradient.getArg(i).getRootNode();
                        if (uninstancedArg.getType().equals(Type.VAR)) {
                            final FasterString varName = uninstancedArg.toFasterString();
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
                        if (((Double) valuesFound.get(argPosition).getRootNodeData()) <= threshold) {
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
                        if (!compatibleFound && ((Double) valuesFound.get(argPosition).getRootNodeData() < threshold)) {
                            gradientsFound.add(new LsaMolecule(valuesFound));
                        }
                    }
                }
            }
            return true;
        }
    }

    private static class SGFakeConditionAction implements Action<List<ILsaMolecule>>, Condition<List<ILsaMolecule>> {
        private static final long serialVersionUID = 2202769961348637251L;
        private final Molecule mol;

        SGFakeConditionAction(final Molecule m) {
            super();
            mol = m;
        }

        @Override
        public void execute() {
        }

        @Override
        public Context getContext() {
            return null;
        }

        @Override
        public ListSet<? extends Molecule> getInfluencingMolecules() {
            return null;
        }

        @Override
        public ListSet<? extends Molecule> getModifiedMolecules() {
            return null;
        }

        @Override
        public Node<List<ILsaMolecule>> getNode() {
            return null;
        }

        @Override
        public double getPropensityConditioning() {
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

        @Override
        public Condition<List<ILsaMolecule>> cloneCondition(final Node<List<ILsaMolecule>> n, final Reaction<List<ILsaMolecule>> r) {
            return null;
        }

        @Override
        public Action<List<ILsaMolecule>> cloneAction(final Node<List<ILsaMolecule>> n, final Reaction<List<ILsaMolecule>> r) {
            return null;
        }

    }

    /**
     * Builds a new SAPERE Gradient. This constructor is slower, and is provided
     * for compatibility with the YAML-based Alchemist loader. It should be
     * avoided when possible, by relying on the other constructor instead.
     * 
     * @param env
     *            the current environment
     * @param n
     *            the node where this reaction is scheduled
     * @param sourceTemplate
     *            a template ILsaMolecule representing the source
     * @param gradientTemplate
     *            a template ILsaMolecule representing the gradient. ALL the
     *            variables MUST be the same of sourceTemplate: no uninstanced
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
     *            of the local node in order to have local informations to use
     *            in the gradient value computation
     * @param gradThreshold
     *            if the value of the gradient grows above this threshold, the
     *            gradient evaporates
     * @param td
     *            Markovian Rate
     */
    public SAPEREGradient(final Environment<List<ILsaMolecule>> env,
            final ILsaNode n,
            final TimeDistribution<List<ILsaMolecule>> td,
            final String sourceTemplate,
            final String gradientTemplate,
            final int valuePosition,
            final String expression,
            final String contextTemplate,
            final double gradThreshold) {
        this(env,
                n,
                new LsaMolecule(sourceTemplate),
                new LsaMolecule(gradientTemplate),
                valuePosition,
                expression,
                new LsaMolecule(contextTemplate),
                gradThreshold,
                td);
    }

    /**
     * Builds a new SAPERE Gradient.
     * 
     * @param env
     *            the current environment
     * @param n
     *            the node where this reaction is scheduled
     * @param sourceTemplate
     *            a template ILsaMolecule representing the source
     * @param gradientTemplate
     *            a template ILsaMolecule representing the gradient. ALL the
     *            variables MUST be the same of sourceTemplate: no uninstanced
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
     *            of the local node in order to have local informations to use
     *            in the gradient value computation
     * @param gradThreshold
     *            if the value of the gradient grows above this threshold, the
     *            gradient evaporates
     * @param td
     *            Markovian Rate
     */
    public SAPEREGradient(final Environment<List<ILsaMolecule>> env, final ILsaNode n, final ILsaMolecule sourceTemplate, final ILsaMolecule gradientTemplate, final int valuePosition, final String expression, final ILsaMolecule contextTemplate, final double gradThreshold, final TimeDistribution<List<ILsaMolecule>> td) {
        super(n, td);
        gradient = Objects.requireNonNull(gradientTemplate);
        source = Objects.requireNonNull(sourceTemplate);
        context = contextTemplate;
        environment = env;
        node = n;
        if (valuePosition < 0) {
            throw new IllegalArgumentException("The position in the gradient LSA must be a positive integer");
        }
        argPosition = valuePosition;
        final IExpression exp = new Expression(expression);
        threshold = gradThreshold;
        final List<IExpression> grexp = gradient.allocateVar(null);
        grexp.set(argPosition, exp);
        gradientExpr = new LsaMolecule(grexp);
        addInfluencedMolecule(gradient);
        addInfluencingMolecule(source);
        fakeconds.add(new SGFakeConditionAction(source));
        addInfluencingMolecule(gradient);
        fakeacts.add(new SGFakeConditionAction(gradient));
        if (context != null) {
            addInfluencingMolecule(context);
            fakeconds.add(new SGFakeConditionAction(context));
        }
        final boolean usesRoutes = environment instanceof MapEnvironment && (gradientTemplate.toString().contains(LsaMolecule.SYN_ROUTE) || expression.contains(LsaMolecule.SYN_ROUTE));
        mapenvironment = usesRoutes ? (MapEnvironment<List<ILsaMolecule>>) environment : null;
    }

    @Override
    public boolean canExecute() {
        return canRun;
    }

    /**
     * Clean up existing gradients. The new values will be computed upon
     * neighbors'
     */
    private List<ILsaMolecule> cleanUpExistingAndRecomputeFromSource(final Map<FasterString, ITreeNode<?>> matches) {
        for (final ILsaMolecule g : node.getConcentration(gradient)) {
            node.removeConcentration(g);
        }
        final List<ILsaMolecule> createdFromSource = new ArrayList<>(sourceCache.size());
        if (!sourceCache.isEmpty()) {
            matches.put(LsaMolecule.SYN_O, new NumTreeNode(getNode().getId()));
            for (final ILsaMolecule s : sourceCache) {
                for (int i = 0; i < source.size(); i++) {
                    final ITreeNode<?> uninstancedArg = source.getArg(i).getRootNode();
                    if (uninstancedArg.getType().equals(Type.VAR)) {
                        matches.put(uninstancedArg.toFasterString(), s.getArg(i).getRootNode());
                    }
                }
                final List<IExpression> gl = gradient.allocateVar(matches);
                final ILsaMolecule m = new LsaMolecule(gl);
                createdFromSource.add(m);
                node.setConcentration(m);
            }
        }
        return createdFromSource;
    }

    @Override
    public void execute() {
        canRun = false;
        final Map<FasterString, ITreeNode<?>> matches = new HashMap<>();
        matches.put(LsaMolecule.SYN_T, new NumTreeNode(getTau().toDouble()));
        final List<ILsaMolecule> createdFromSource = cleanUpExistingAndRecomputeFromSource(matches); //NOPMD: there is a side effect
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
                    final FasterString varName = uninstancedArg.toFasterString();
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
                    final FasterString varName = uninstancedArg.toFasterString();
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
            filteredGradCache = new ExactHashObjectMap<>(gradCache.size());
            gradCache.forEachEntry(new Cleaner(createdFromSource, filteredGradCache));
        }
        /*
         * Gradients in neighborhood must be discovered
         */
        final List<ILsaMolecule> gradientsFound = new ArrayList<>();
        final GradientSearch gradSearch = new GradientSearch(gradientsFound, matches);
        filteredGradCache.forEachEntry(gradSearch);
        createdFromSource.forEach(genGrad -> gradientsFound.add(genGrad));
        gradientsFound.forEach(grad -> node.setConcentration(grad));
    }

    @Override
    public List<Action<List<ILsaMolecule>>> getActions() {
        return fakeacts;
    }

    @Override
    public List<Condition<List<ILsaMolecule>>> getConditions() {
        return fakeconds;
    }

    @Override
    public Context getInputContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public ILsaNode getNode() {
        return node;
    }

    @Override
    public Context getOutputContext() {
        return Context.LOCAL;
    }

    @Override
    public Reaction<List<ILsaMolecule>> cloneOnNewNode(final Node<List<ILsaMolecule>> n) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void updateInternalStatus(final Time curTime, final boolean executed, final Environment<List<ILsaMolecule>> env) {
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
        final List<? extends ILsaMolecule> sourceCacheTemp = node.getConcentration(source);
        final List<? extends ILsaMolecule> contextCacheTemp = context == null ? EMPTY_LIST : node.getConcentration(context);
        final TIntObjectMap<Position> positionCacheTemp = new ExactHashObjectMap<>(positionCache.size());
        final TIntObjectMap<List<? extends ILsaMolecule>> gradCacheTemp = new ExactHashObjectMap<>(gradCache.size());
        final Position curPos = environment.getPosition(node);
        final boolean positionChanged = !curPos.equals(mypos);
        boolean neighPositionChanged = false;
        for (final Node<List<ILsaMolecule>> n : environment.getNeighborhood(node)) {
            final Position p = environment.getPosition(n);
            final int nid = n.getId();
            positionCacheTemp.put(nid, p);
            gradCacheTemp.put(n.getId(), n.getConcentration(gradient));
            final boolean pConstant = p.equals(positionCache.get(nid));
            if (!pConstant) {
                neighPositionChanged = true;
            }
            /*
             * TODO: may need to cleanup old values for routes and positions
             */
            if (mapenvironment != null && (!pConstant || positionChanged)) {
                routecache.put(nid, mapenvironment.computeRoute(n, curPos).getDistance());
            }
        }
        if (!sourceCacheTemp.equals(sourceCache) || !contextCacheTemp.equals(contextCache) || neighPositionChanged || !gradCacheTemp.equals(gradCache) || positionChanged) {
            sourceCache = sourceCacheTemp;
            contextCache = contextCacheTemp;
            positionCache = positionCacheTemp;
            gradCache = gradCacheTemp;
            mypos = curPos;
            canRun = true;
        }
    }

    @Override
    public double getRate() {
        return canRun ? getTimeDistribution().getRate() : 0;
    }

}
