package it.unibo.alchemist.model.implementations.molecules;

/**
 * This class has the sole purpose of making {@link SimpleMolecule} backwards compatible.
 */
@Deprecated
public final class Molecule extends SimpleMolecule {

    private static final long serialVersionUID = -1038166168462926535L;

    /**
     * @param name the {@link Molecule} name
     */
    public Molecule(final String name) {
        super(name);
    }

}
