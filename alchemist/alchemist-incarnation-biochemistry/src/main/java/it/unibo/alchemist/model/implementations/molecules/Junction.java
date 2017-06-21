package it.unibo.alchemist.model.implementations.molecules;

import java.util.Collections;
import java.util.Map;

import it.unibo.alchemist.model.interfaces.Molecule;

/**
 * Represents a junction between two cells.
 */
public class Junction extends SimpleMolecule {

    private static final long serialVersionUID = -5538036651435573599L;

    private final Map<Biomolecule, Double> moleculesInCurrentNode;
    private final Map<Biomolecule, Double> moleculesInNeighborNode;
    /**
     * Build a junction.
     * @param name the name of the junction.
     * @param moleculesInCurrentNode 
     *  A map of molecules (with their concentration) which was in the current node. When the junction is removed that molecules will be released in the current node.
     * @param moleculesInNeighborNode
     *  A map of molecules (with their concentration) which was in the current node. When the junction is removed that molecules will be released in the current node.
     */
    public Junction(final String name, final Map<Biomolecule, Double> moleculesInCurrentNode, final Map<Biomolecule, Double> moleculesInNeighborNode) {
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
    public boolean dependsOn(final Molecule mol) {
        return equals(mol);
    }

}
