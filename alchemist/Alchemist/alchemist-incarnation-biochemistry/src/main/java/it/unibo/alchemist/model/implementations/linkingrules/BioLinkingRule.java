/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors

 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

/**
 *
 */
public class BioLinkingRule extends EuclideanDistance<Double> {

    /*
     * TODO Parametri: distanza dai nodi ambientali, distanza dai nodi cellule, distanza fra loro di
     * nodi ambientali (cell topology range, cell inspection range, environment grain)
     */
    private static final long serialVersionUID = -9097330142719644684L;

    /**
     * 
     * @param envNodesDistance 
     * @param cellNodesDistance 
     * @param grain 
     */
    public BioLinkingRule(final double envNodesDistance, final double cellNodesDistance, final double grain) {
//        costruttore dela superclasse:
//        public EuclideanDistance(final double radius) {
//            range = radius;
//        }
        super(cellNodesDistance); // TODO il radius corrisponde a cellNodesDistance, envNodesDistance, o a grain? 
        // TODO Auto-generated constructor stub
    }

}
