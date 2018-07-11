package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.interfaces.Position;

@Deprecated
public class EuclideanDistance<T, P extends Position<? extends P>> extends ConnectWithinDistance<T, P> {

    public EuclideanDistance(double radius) {
        super(radius);
    }

}
