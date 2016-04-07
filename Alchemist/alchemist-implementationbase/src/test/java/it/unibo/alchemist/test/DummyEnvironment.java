package it.unibo.alchemist.test;

import org.danilopianini.lang.FlexibleQuadTree;

import it.unibo.alchemist.model.implementations.environments.AbstractEnvironment;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

class DummyEnvironment extends AbstractEnvironment<Object> {
    private static final long serialVersionUID = 3535188332113463170L;
    protected DummyEnvironment() {
        super(new FlexibleQuadTree<>());
    }
    @Override
    public int getDimensions() {
        return 0;
    }
    @Override
    public Neighborhood<Object> getNeighborhood(final Node<Object> center) {
        return null;
    }
    @Override
    public double[] getOffset() {
        return new double[]{};
    }
    @Override
    public double[] getSize() {
        return new double[]{};
    }
    @Override
    public void moveNode(final Node<Object> node, final Position direction) {
    }
    @Override
    public void moveNodeToPosition(final Node<Object> node, final Position position) {
    }

    @Override
    public void setLinkingRule(final LinkingRule<Object> rule) {
    }

    @Override
    public LinkingRule<Object> getLinkingRule() {
        return null;
    }
    @Override
    protected void nodeAdded(final Node<Object> node, final Position p) {
    }
    @Override
    protected boolean nodeShouldBeAdded(final Node<Object> node, final Position p) {
        return false;
    }
    @Override
    protected Position computeActualInsertionPosition(final Node<Object> node, final Position p) {
        return null;
    }
    @Override
    protected void nodeRemoved(final Node<Object> node, final Position pos) {
    }
}
