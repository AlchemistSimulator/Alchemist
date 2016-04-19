/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors


 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */


package it.unibo.alchemist.model.implementations.reactions;

import java.util.LinkedHashMap;
import java.util.Map;



import gnu.trove.map.TObjectDoubleMap;
//import it.unibo.alchemist.model.interfaces.IConditionBind;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;


/** 
 * Unused class (probably it will be removed).
 */
public class BiochemicalReaction extends ChemicalReaction<Double> {

    private static final long serialVersionUID = 3849210665619933894L;
    private final Map<Node<Double>, TObjectDoubleMap<Molecule>> validNeighNodes = new LinkedHashMap<>();
    //private double totalPropensity;

    /**
     * @param n
     *            node
     * @param td
     *            time distribution
     */
    public BiochemicalReaction(final Node<Double> n, final TimeDistribution<Double> td) {
        super(n, td);
    }

    @Override 
    protected void updateInternalStatus(final Time curTime, final boolean executed, final Environment<Double> env) {
//        validNeighNodes.clear();
//        for (final Node<Double> n: env.getNeighborhood(super.getNode())) {
//            validNeighNodes.put(n, new TObjectDoubleHashMap<Molecule>());
//        }
//        for (final Condition<Double> cond : getConditions())  {
//            if (cond instanceof IConditionBind) {
//                final IConditionBind cbind = (IConditionBind) cond;
//                cbind.setValidNeighborood(validNeighNodes);
//            }
//        }
//        super.updateInternalStatus(curTime, executed, env);
    }

    @Override 
    public void execute() {
//        final double r = randomNumb.nextDouble() * totalMoleculeNumb;
//        super.g;
//
//        double p = 0;
//        for (final TObjectDoubleIterator<Node<Double>> it = validNeighNodes.iterator(); it.hasNext();) {
//            it.advance();
//            p += it.value();
//            if (p >= r) {
//                executeActionOnNeigh(it.key());
//                break;
//            }
//        }
//        act.bindNeigh(n);
//        super.execute();
    }

}
