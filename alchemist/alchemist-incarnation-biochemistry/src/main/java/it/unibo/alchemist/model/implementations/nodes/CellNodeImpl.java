/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
/*
a * Copyright (C) 2010-2016, Danilo Pianini and contributors

 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.nodes;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Position;

/**
 *
 */
public class CellNodeImpl<P extends Position<P>> extends DoubleNode implements CellNode<P>, CellWithCircularArea<P> {

    private static final long serialVersionUID = 837704874534888283L;

    private final Map<Junction, Map<CellNode<?>, Integer>> junctions = new LinkedHashMap<>();
    private final Environment<Double, P> environment;
    private double diameter;
    private P polarizationVersor;

    /**
     * create a new cell node.
     * 
     * @param env
     *            the environment
     * @param diameter
     *            the diameter
     */
    public CellNodeImpl(final Environment<Double, P> env, final double diameter) {
        super(env);
        environment = env;
        this.polarizationVersor = env.makePosition(0, 0);
        this.diameter = diameter;
    }

    /**
     * @param env
     *            the environment
     */
    public CellNodeImpl(final Environment<Double, P> env) {
        this(env, 0);
    }

    @Override
    protected Double createT() {
        return 0d;
    }

    @Override
    public void setConcentration(final Molecule mol, final Double c) {
        if (c < 0) {
            throw new IllegalArgumentException("No negative concentrations allowed (" + mol + " -> " + c + ")");
        }
        if (c > 0) {
            super.setConcentration(mol, c);
        } else {
            removeConcentration(mol);
        }
    }

    @Override
    public void setPolarization(final P v) {
        this.polarizationVersor = v;
    }

    @Override
    public P getPolarizationVersor() {
        return polarizationVersor;
    }

    @Override
    public void addPolarization(final P v) {
        final double[] tempCor = this.polarizationVersor.add(v).getCartesianCoordinates();
        final double module = FastMath.sqrt(FastMath.pow(tempCor[0], 2) + FastMath.pow(tempCor[1], 2));
        this.polarizationVersor = module == 0 
                ? environment.makePosition(0, 0) 
                        : environment.makePosition(tempCor[0] / module, tempCor[1] / module);
    }

    @Override
    public boolean contains(final Molecule m) {
        if (m instanceof Junction) {
            return containsJunction((Junction) m);
        } else {
            return super.contains(m);
        }
    }

    @Override
    public Map<Junction, Map<CellNode<?>, Integer>> getJunctions() {
        //return Collections.unmodifiableMap(junctions);
        final Map<Junction, Map<CellNode<?>, Integer>> ret = new LinkedHashMap<>();
        junctions.entrySet().forEach(e -> ret.put(e.getKey(), new LinkedHashMap<>(e.getValue())));
        return ret;
    }

    @Override
    public void addJunction(final Junction j, final CellNode<?> neighbor) {
        if (junctions.containsKey(j)) {
            final Map<CellNode<?>, Integer> inner = junctions.get(j);
            if (inner.containsKey(neighbor)) {
                inner.put(neighbor, inner.get(neighbor) + 1);
            } else {
                inner.put(neighbor, 1);
            }
            junctions.put(j, inner);
        } else {
            final Map<CellNode<?>, Integer> tmp = new LinkedHashMap<>(1);
            tmp.put(neighbor, 1);
            junctions.put(j, tmp);
        }
    }

    @Override
    public boolean containsJunction(final Junction j) {
        return junctions.containsKey(j);
    }

    @Override
    public void removeJunction(final Junction j, final CellNode<?> neighbor) {
        if (junctions.containsKey(j)) {
            final Map<CellNode<?>, Integer> inner = junctions.get(j);
            if (inner.containsKey(neighbor)) {
                if (inner.get(neighbor) == 1) { // only one junction j with neighbor
                    inner.remove(neighbor);
                } else {
                    inner.put(neighbor, inner.get(neighbor) - 1);
                }
                if (inner.isEmpty()) {
                    junctions.remove(j);
                } else {
                    junctions.put(j, inner);
                }
                for (final Map.Entry<Biomolecule, Double> e : j.getMoleculesInCurrentNode().entrySet()) {
                    setConcentration(e.getKey(), getConcentration(e.getKey()) +  e.getValue());
                }
            }
        }
    }

    @Override
    public Set<CellNode<?>> getNeighborsLinkWithJunction(final Junction j) {
        if (junctions.get(j) == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(junctions.get(j).keySet());
    }

    @Override
    public Set<CellNode<?>> getAllNodesLinkWithJunction() {
        final Set<CellNode<?>> r = new HashSet<>();
        for (final Map.Entry<Junction, Map<CellNode<?>, Integer>> e : junctions.entrySet()) {
            r.addAll(e.getValue().keySet());
        }
        return r;
    }

    @Override
    public int getJunctionNumber() {
        return junctions.values().stream().mapToInt(m -> m.values().stream().mapToInt(v -> v.intValue()).sum()).sum();
    }

    @Override
    public double getDiameter() {
        return diameter;
    }

    @Override
    public double getRadius() {
        return getDiameter() / 2;
    }

    @Override
    public String toString() {
        return "Instance of CellNodeImpl with diameter = " + diameter;
    }
}
