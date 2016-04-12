package it.unibo.alchemist.model.implementations.concentrations;

import it.unibo.alchemist.model.interfaces.Concentration;

/**
 */
public class DoubleConcentration implements Concentration<Double> {

    private static final long serialVersionUID = 1060443735840512089L;
    private final double content;

    /**
     * Builds a new double concentration.
     * @param i the concentration value
     */
    public DoubleConcentration(final double i) {
        content = i;
    }

    @Override
    public Double getContent() {
        return this.content;
    }

    @Override
    public String toString() {
        return Double.toString(this.content);
    }
}
