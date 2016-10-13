package it.unibo.alchemist.model.implementations.actions;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 *
 */
public class CellTensionPolarization extends AbstractAction<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final EnvironmentSupportingDeformableCells env;

    /**
     * 
     * @param node 
     * @param env 
     */
    public CellTensionPolarization(final Environment<Double> env, final Node<Double> node) {
        super(node);
        if (node instanceof CircularDeformableCell) {
            if (env instanceof EnvironmentSupportingDeformableCells) {
                this.env = (EnvironmentSupportingDeformableCells) env;
            } else {
                throw new IllegalArgumentException("This Condition can only be supported in an EnironmentSupportingDeformableCells");
            }
        } else {
            throw new IllegalArgumentException("This Condition can only be setted in a CircularDeformableCell");
        } 
    }

    @Override
    public CellTensionPolarization cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new CellTensionPolarization(env, n);
    }

    @Override
    public void execute() {
        // get node position as array
        final double[] nodePos = env.getPosition(getNode()).getCartesianCoordinates();
        // initializing resulting versor
        double[] resVersor = new double[nodePos.length];
        // declaring a variable for the node where this action is set, to have faster access
        final CircularDeformableCell thisNode = getNode();
        // transforming each node around in a vector (Position) 
        final List<Position> pushForces = env.getNodesWithinRange(
                thisNode,
                env.getMaxDiameterAmongDeformableCells()).stream()
                .parallel()
                .filter(n -> { // only cells overlapping this cell are selected
                    if (n instanceof CellWithCircularArea) {
                        // computing for each cell the max distance among which cant't be overlapping
                        double maxDist;
                        if (n instanceof CircularDeformableCell) {
                            // for deformable cell is maxRad + maxRad
                             maxDist = (thisNode.getMaxRadius() + ((CircularDeformableCell) n).getMaxRadius());
                        } else {
                            // for simple cells is maxRad + rad
                             maxDist = (thisNode.getMaxRadius() + ((CellWithCircularArea) n).getRadius());
                        }
                        // check
                        return env.getDistanceBetweenNodes(thisNode, n) < maxDist;
                    } else {
                        // only CellWithCircularArea are selected.
                        return false;
                    }
                })
                .map(n -> {
                    // position of node n as array
                    final double[] nPos =  env.getPosition(n).getCartesianCoordinates();
                    // max radius of n
                    final double localNodeMaxRadius;
                    // min radius of n
                    final double localNodeMinRadius;
                    // max radius of this node (thisNode)
                    final double nodeMaxRadius = thisNode.getMaxRadius();
                    // min radius of this node (thisNode)
                    final double nodeMinRadius = thisNode.getRadius();
                    // intensity of tension between n and this node (thisNode), measured as value between 0 and 1
                    final double intensity;
                    if (n instanceof CircularDeformableCell) {
                        final CircularDeformableCell localNode = (CircularDeformableCell) n;
                        localNodeMaxRadius = localNode.getMaxRadius();
                        localNodeMinRadius = localNode.getRadius();
                    } else {
                        localNodeMaxRadius = ((CellWithCircularArea) n).getRadius();
                        localNodeMinRadius = localNodeMaxRadius;
                    }
                    // if both cells has no difference between maxRad and minRad intensity must be 1
                    if (localNodeMaxRadius == localNodeMinRadius && nodeMaxRadius == nodeMinRadius) {
                        intensity = 1;
                    } else {
                        intensity = ((localNodeMaxRadius + nodeMaxRadius) - env.getDistanceBetweenNodes(n, thisNode)) / ((localNodeMaxRadius + nodeMaxRadius) - (localNodeMinRadius + nodeMinRadius));
                    }
                    if (intensity != 0) {
                        double[] propensityVect = new double[]{nodePos[0] - nPos[0], nodePos[1] - nPos[1]};
                        final double module = FastMath.sqrt(FastMath.pow(propensityVect[0], 2) + FastMath.pow(propensityVect[1], 2));
                        if (module == 0) {
                            return new Continuous2DEuclidean(0, 0);
                        }
                        propensityVect = new double[]{intensity * (propensityVect[0] / module), intensity * (propensityVect[1] / module)};
                        return new Continuous2DEuclidean(propensityVect[0], propensityVect[1]);
                    } else {
                        return new Continuous2DEuclidean(0, 0);
                    } 
                })
                .collect(Collectors.toList());
        if (pushForces.isEmpty()) {
            thisNode.addPolarization(new Continuous2DEuclidean(0, 0));
        } else {
            for (final Position p : pushForces) {
                for (int i = 0; i < p.getDimensions(); i++) {
                    resVersor[i] = resVersor[i] + p.getCoordinate(i);
                }
            }
            final double module = FastMath.sqrt(FastMath.pow(resVersor[0], 2) + FastMath.pow(resVersor[1], 2));
            if (module == 0) {
                thisNode.addPolarization(new Continuous2DEuclidean(0, 0));
            } else {
                thisNode.addPolarization(new Continuous2DEuclidean(resVersor[0] / module, resVersor[1] / module));
            }
        }
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public CircularDeformableCell getNode() {
        return (CircularDeformableCell) super.getNode();
    }

}
