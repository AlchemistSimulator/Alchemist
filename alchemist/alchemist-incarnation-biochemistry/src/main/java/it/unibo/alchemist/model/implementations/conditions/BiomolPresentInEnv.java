/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
/**
 *
 */
public class BiomolPresentInEnv extends GenericMoleculePresent<Double> {

    private static final long serialVersionUID = 6527297022655890961L;

    private final Environment<Double> environment;

    /**
     * Initialize condition for extra-cellular environment, implemented as a set
     * of {@link EnvironmentNode}.
     * 
     * @param biomol
     *            the {@link Biomolecule} which the condition is about.
     * @param conc
     *            the requested concentration.
     * @param node
     *            the node where this condition is located;
     * @param env
     *            the {@link Environment} where the node is located.
     */
    public BiomolPresentInEnv(final Biomolecule biomol, final Double conc, 
            final Node<Double> node, final Environment<Double> env) {
        super(biomol, node, conc);
        environment = env;
    }

    @Override
    public double getPropensityConditioning() {
        if (getEnviromentNodesSurrounding().isEmpty()) {
            return 0;
        }
        final double totalQuantity = getEnviromentNodesSurrounding().stream()
                .mapToDouble(n -> n.getConcentration(getBiomolecule()))
                .sum();
        if (totalQuantity < getConcentrationReqired()) {
            return 0;
        }
        return CombinatoricsUtils.binomialCoefficientDouble(
                (int) FastMath.round(totalQuantity), 
                (int) FastMath.round(getConcentrationReqired())
                );
    }

    private List<Node<Double>> getEnviromentNodesSurrounding() {
        final List<Node<Double>> list = new ArrayList<>();
        environment
        .getNeighborhood(getNode())
        .getNeighbors()
        .stream()
        .filter(n -> n instanceof EnvironmentNode)
        .forEach(n -> list.add(n));
        return list;
    }

    @Override 
    public BiomolPresentInEnv cloneOnNewNode(final Node<Double> n) {
        return new BiomolPresentInEnv(getBiomolecule(), getConcentrationReqired(), n, environment);
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public boolean isValid() {
        if (getEnviromentNodesSurrounding().isEmpty()) {
            return false;
        } else {
            return getEnviromentNodesSurrounding().stream()
                    .mapToDouble(n -> n.getConcentration(getBiomolecule()))
                    .sum() >= getConcentrationReqired();
        }
    }

    private Biomolecule getBiomolecule() {
        return (Biomolecule) getMolecule();
    }

    private double getConcentrationReqired() {
        return getQuantity();
    }

}