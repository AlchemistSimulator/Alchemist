package it.unibo.alchemist.model.implementations.actions;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 *
 */
public class ChemiotacticPolarization extends AbstractAction<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Environment<Double> env;
    private final Biomolecule biomol;
    private final boolean ascend;

    /**
     * Initialize a polarization activity regulated by environmental concentration of a molecule.
     * @param environment 
     * @param node 
     * @param biomol 
     * @param ascendGrad if that parameter is true, the polarization versor of the cell will be directed in direction of the greates concentration of biomolecule in neighborhood; if it's false, the versor will be directed in the exactly the opposite direction.
     */
    public ChemiotacticPolarization(final Environment<Double> environment, final Node<Double> node, final String biomol, final String ascendGrad) {
        super(node);
        if (node instanceof CellNode) {
            this.env = environment;
            this.biomol = new Biomolecule(biomol);
            if (ascendGrad.equals("up")) {
                this.ascend = true;
            } else if (ascendGrad.equals("down")) {
                this.ascend = false;
            } else {
                throw new IllegalArgumentException("Possible imput string are only up or down");
            }
        } else {
            throw  new UnsupportedOperationException("Polarization can happen only in cells.");
        }
    }

    @Override
    public ChemiotacticPolarization cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new ChemiotacticPolarization(env, n, biomol.toString(), ascend ? "up" : "down");
    }

    @Override
    public void execute() {
        // declaring a variable for the node where this action is set, to have faster access
        final CellNode thisNode = getNode();
        final List<Node<Double>> l = env.getNeighborhood(thisNode).getNeighbors().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode && n.contains(biomol))
                .collect(Collectors.toList());
        if (l.isEmpty()) {
            thisNode.addPolarization(new Continuous2DEuclidean(0, 0));
        } else {
            final boolean isNodeOnMaxConc = env.getPosition(l.stream()
                    .max((n1, n2) -> Double.compare(n1.getConcentration(biomol), n2.getConcentration(biomol)))
                    .get()).equals(env.getPosition(thisNode));
            if (isNodeOnMaxConc) {
                thisNode.addPolarization(new Continuous2DEuclidean(0, 0));
            } else {
                Position newPolVer = weightedAverageVectors(l, thisNode);
                final double newPolVerModule = FastMath.sqrt(FastMath.pow(
                        newPolVer.getCoordinate(0), 2) + FastMath.pow(newPolVer.getCoordinate(1), 2)
                        );
                if (newPolVerModule == 0) {
                    thisNode.addPolarization(newPolVer);
                } else {
                    newPolVer = new Continuous2DEuclidean(newPolVer.getCoordinate(0) / newPolVerModule, newPolVer.getCoordinate(1) / newPolVerModule);
                    if (ascend) {
                        thisNode.addPolarization(newPolVer);
                    } else {
                        thisNode.addPolarization(new Continuous2DEuclidean(
                                -newPolVer.getCoordinate(0), 
                                -newPolVer.getCoordinate(1))
                                );
                    }
                }
            }
        }
    }

    private Position weightedAverageVectors(final List<Node<Double>> list, final CellNode thisNode) {
        Position res = new Continuous2DEuclidean(0, 0);
        for (final Node<Double> n : list) {
            Position vecTemp = new Continuous2DEuclidean(
                    env.getPosition(n).getCoordinate(0) - env.getPosition(thisNode).getCoordinate(0),
                    env.getPosition(n).getCoordinate(1) - env.getPosition(thisNode).getCoordinate(1));
            final double vecTempModule = FastMath.sqrt(FastMath.pow(vecTemp.getCoordinate(0), 2) + FastMath.pow(vecTemp.getCoordinate(1), 2));
            vecTemp = new Continuous2DEuclidean(
                    n.getConcentration(biomol) * (vecTemp.getCoordinate(0) / vecTempModule), 
                    n.getConcentration(biomol) * (vecTemp.getCoordinate(1) / vecTempModule));
            res = new Continuous2DEuclidean(
                    res.getCoordinate(0) + vecTemp.getCoordinate(0),
                    res.getCoordinate(1) + vecTemp.getCoordinate(1));
        }
        return res;
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public CellNode getNode() {
        return (CellNode) super.getNode();
    }

}
