package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.interfaces.LinkingRule;

/**
 * @param <T>
 *            Concentration type
 */
public abstract class AbstractLocallyConsistentLinkingRule<T> implements LinkingRule<T> {

    private static final long serialVersionUID = 1L;

    @Override
    public final boolean isLocallyConsistent() {
        return true;
    }

}
