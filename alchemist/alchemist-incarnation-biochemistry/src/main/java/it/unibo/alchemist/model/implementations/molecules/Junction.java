package it.unibo.alchemist.model.implementations.molecules;

import java.util.Collections;
import java.util.Map;

import it.unibo.alchemist.model.interfaces.ICellNode;
import it.unibo.alchemist.model.interfaces.Molecule;
import java.util.Optional;

import org.danilopianini.lang.util.FasterString;

/**
 * Represents a junction between two cells.
 */
public class Junction implements Molecule {

    private static final long serialVersionUID = -5538036651435573599L;

    private final FasterString name;
    private final Map<Biomolecule, Double> moleculesInCurrentNode;
    private final Map<Biomolecule, Double> moleculesInNeighborNode;
    private ICellNode neighborNode;

    /**
     * Build a junction.
     * @param name the name of the junction.
     * @param moleculesInCurrentNode 
     *  A map of molecules (with their concentration) which was in the current node. When the junction is removed that molecules will be released in the current node.
     * @param moleculesInNeighborNode
     *  A map of molecules (with their concentration) which was in the current node. When the junction is removed that molecules will be released in the current node.
     */
    public Junction(final String name, final Map<Biomolecule, Double> moleculesInCurrentNode, final Map<Biomolecule, Double> moleculesInNeighborNode) {
        this.name = new FasterString(name);
        this.moleculesInCurrentNode = moleculesInCurrentNode;
        this.moleculesInNeighborNode = moleculesInNeighborNode;
    }

    /**
     * Builds a junction from another junction. The neighbor node of the created junction is NOT set.
     * @param toClone the junction to clone.
     */
    public Junction(final Junction toClone) {
        name = toClone.name;
        moleculesInCurrentNode = toClone.getMoleculesInCurrentNode();
        moleculesInNeighborNode = toClone.getMoleculesInNeighborNode();
    }

    /**
     * @return the name of the junction.
     */
    public String getName() {
        return name.toString();
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
     * 
     * @return the neighbor node if it's present. Return an Optional.empty() instead.
     */
    public Optional<ICellNode> getNeighborNode() {
        return Optional.ofNullable(neighborNode);
    }

    /**
     * Set the neighbor node of the junction.
     * @param neighbor the neighbor node.
     */
    public void setNeighborNode(final ICellNode neighbor) {
        neighborNode = neighbor;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Junction) {
            return ((Junction) obj).name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + neighborNode.hashCode();
    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public long getId() {
        return name.hash64();
    }

    @Override
    public boolean dependsOn(final Molecule mol) {
        return false;
    }

}
