package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * @param <T>
 *            Concentration type
 */
public abstract class AbstractLocallyConsistentLinkingRule<T, P extends Position<? extends P>> implements LinkingRule<T, P> {

    private static final long serialVersionUID = 1L;

    @Override
    public final boolean isLocallyConsistent() {
        return true;
    }

}
