package it.unibo.alchemist.model.implementations.conditions;

import java.util.Objects;

import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * A condition that is valid iff a molecule has exactly the desired concentration.
 * 
 * @param <T> concentration type
 */
public class MoleculeHasConcentration<T> extends AbstractCondition<T> {

    private static final long serialVersionUID = 1L;
    private final Molecule mol;
    private final T value;

    /**
     * @param node
     *            the node
     * @param molecule
     *            the target molecule
     * @param value
     *            the desired concentration
     */
    public MoleculeHasConcentration(final Node<T> node, final Molecule molecule, final T value) {
        super(node);
        this.mol = Objects.requireNonNull(molecule);
        this.value = Objects.requireNonNull(value);
        addReadMolecule(this.mol);
    }

    @Override
    public Condition<T> cloneOnNewNode(final Node<T> n) {
        return new MoleculeHasConcentration<>(n, mol, value);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public double getPropensityConditioning() {
        return isValid() ? 1 : 0;
    }

    @Override
    public boolean isValid() {
        return value.equals(getNode().getConcentration(mol));
    }

    @Override
    public String toString() {
        return mol + "=" + value + "?[" + isValid() + "]";
    }

}
