/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.molecules;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Dependency;
import it.unibo.alchemist.model.molecules.SimpleMolecule;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a junction between two cells.
 */
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public final class Junction extends SimpleMolecule {

    private static final long serialVersionUID = -5538036651435573599L;

    private final Map<Biomolecule, Double> moleculesInCurrentNode;
    private final Map<Biomolecule, Double> moleculesInNeighborNode;
    /**
     * Build a junction.
     * @param name the name of the junction.
     * @param moleculesInCurrentNode 
     *  A map of molecules (with their concentration)
     *  which was in the current node.
     *  When the junction is removed the molecules will be released in the current node.
     * @param moleculesInNeighborNode
     *  A map of molecules (with their concentration) which was in the current node.
     *  When the junction is removed the molecules will be released in the current node.
     */
    public Junction(
            final String name,
            final Map<Biomolecule, Double> moleculesInCurrentNode,
            final Map<Biomolecule, Double> moleculesInNeighborNode
    ) {
        super(name);
        this.moleculesInCurrentNode = moleculesInCurrentNode;
        this.moleculesInNeighborNode = moleculesInNeighborNode;
    }

    /**
     * Builds a junction from another junction.
     * @param toClone the junction to clone.
     */
    public Junction(final Junction toClone) {
        this(toClone.getName(), toClone.getMoleculesInCurrentNode(), toClone.getMoleculesInNeighborNode());
    }

    /**
     * @return a map of molecules and concentrations associated with this junction in the current node.
     */
    public Map<Biomolecule, Double> getMoleculesInCurrentNode() {
        return Collections.unmodifiableMap(moleculesInCurrentNode);
    }

    /**
     * @return a map of molecules and concentrations associated with this junction in the neighbor node.
     */
    public Map<Biomolecule, Double> getMoleculesInNeighborNode() {
        return Collections.unmodifiableMap(moleculesInNeighborNode);
    }

    /**
     * Return the reversed junction of the current junction. E.g. junction A-B return junction B-A
     * @return the reversed junction
     */
    public Junction reverse() {
        final String[] split = getName().split("-");
        final String revName = split[1] + "-" + split[0];
        return new Junction(revName, getMoleculesInNeighborNode(), getMoleculesInCurrentNode());
    }

    @Override
    public boolean dependsOn(final Dependency mol) {
        return equals(mol);
    }

}
