package it.unibo.alchemist.grid.simulation;

public class ComplexityImpl implements Complexity {

    private final double ram;
    private final double cpu;

    public ComplexityImpl(double ram, double cpu) {
        this.ram = ram;
        this.cpu = cpu;
    }

    @Override
    public double getRamUsage() {
        // TODO Auto-generated method stub
        return this.ram;
    }

    @Override
    public double getCpuUsage() {
        // TODO Auto-generated method stub
        return this.cpu;
    }

}
