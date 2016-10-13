package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * 
 *
 */
public class CellMove extends AbstractMoveNode<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final boolean inPer;
    private final double delta;

    /**
     * Initialize an Action that move the cell of a given space delta, which can be expressed in percent of the cell's diameter or in absolute.
     * If the cell has diameter 0, the only way to express delta is absolute.
     * There's no way to decide the direction of the cell by this {@link Action}. This is inferred by the polarization vector contained in the cell.
     * 
     * @param environment the {@link Environment}
     * @param node the {@link Node} in which the {@link Action} is contained. This can be only a CellNode.
     * @param inPercent a boolean parameter which set the way of expressing delta: if is true the cell movement will be (delta * cellDiameter), otherwise will be simply delta. If cellDiameter is zero, this {@link Action} will in both cases behave like inPercent == false.
     * @param delta the distance at which the cell will be moved.
     */
    public CellMove(final Environment<Double> environment, final Node<Double> node, final boolean inPercent, final double delta) {
        super(environment, node);
        this.inPer = inPercent;
        if (node instanceof CellNode) {
            if (inPercent) {
                if (node instanceof CellWithCircularArea && ((CellWithCircularArea) node).getRadius() != 0) {
                    this.delta = ((CellWithCircularArea) node).getDiameter() * delta;
                } else {
                    throw new IllegalArgumentException("Can't set distance in percent of the cell's diameter if cell has not a diameter");
                }
            } else {
                this.delta = delta;
            }
        } else {
            throw  new UnsupportedOperationException("CellMove can be setted only in cells.");
        }
    }

    @Override
    public CellMove cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new CellMove(getEnvironment(), n, inPer, delta);
    }

    @Override
    public Position getNextPosition() {
        return new Continuous2DEuclidean(
                delta * getNode().getPolarizationVersor().getCoordinate(0),
                delta * getNode().getPolarizationVersor().getCoordinate(1)
                );
    }

    @Override
    public void execute() {
        super.execute();
        getNode().setPolarization(new Continuous2DEuclidean(0, 0));
    }

    @Override
    public CellNode getNode() {
        return ((CellNode) super.getNode());
    }

}
