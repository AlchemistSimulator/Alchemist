package it.unibo.alchemist.grid.simulation;

/**
 * {@link Complexity} implementation.
 *
 */
public class ComplexityImpl implements Complexity {

    private final double ram;
    private final double cpu;

    /**
     * 
     * @param ram Simulation's ram usage in GB
     * @param cpu Simulation's cpu usage in %
     */
    public ComplexityImpl(final double ram, final double cpu) {
        this.ram = ram;
        this.cpu = cpu;
    }

    @Override
    public double getRamUsage() {
        return this.ram;
    }

    @Override
    public double getCpuUsage() {
        return this.cpu;
    }

}
