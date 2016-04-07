/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.model.SAPEREIncarnation;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.Incarnation;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.util.FastMath;
import org.danilopianini.lang.HashUtils;
import org.danilopianini.view.ExportForGUI;

/**
 */
@ExportInspector
public class SAPERENearestNodeSampler extends PositionSampler<List<? extends ILsaMolecule>> {

    private static final long serialVersionUID = -5664559491928782478L;
    @ExportForGUI(nameToExport = "Enable support for node mobility")
    private boolean mobility = true;
    @ExportForGUI(nameToExport = "LSA to track")
    private String lsa = "";
    @ExportForGUI(nameToExport = "Properties to log")
    private String property = "";
    @ExportForGUI(nameToExport = "Property separators")
    private String propertySeparators = " ;,:";

    private String propertyCache;
    private String lsaCache;
    private Molecule mol;
    private final List<String> properties = new LinkedList<>();
    private final Incarnation<List<? extends ILsaMolecule>> sapere = new SAPEREIncarnation();
    private final Map<Position, Node<List<? extends ILsaMolecule>>> pnCache = new ConcurrentHashMap<>();
    private Environment<List<? extends ILsaMolecule>> envCache;

    /**
     * @return lsa
     */
    protected String getLsa() {
        return lsa;
    }

    /**
     * @param l
     *            lsa
     */
    protected void setLsa(final String l) {
        this.lsa = l;
    }

    /**
     * @return property
     */
    protected String getProperty() {
        return property;
    }

    /**
     * @param p
     *            property
     */
    protected void setProperty(final String p) {
        this.property = p;
    }

    /**
     * @return property separators
     */
    protected String getPropertySeparators() {
        return propertySeparators;
    }

    /**
     * @param ps
     *            property separators
     */
    protected void setPropertySeparators(final String ps) {
        this.propertySeparators = ps;
    }

    @Override
    protected double[] getProperties(final Environment<List<? extends ILsaMolecule>> env, final Position pos, final Reaction<List<? extends ILsaMolecule>> r, final Time time, final long step) {
        if (mobility || !HashUtils.pointerEquals(env, envCache)) {
            pnCache.clear();
            envCache = env;
        }
        if (!HashUtils.pointerEquals(propertyCache, property)) {
            propertyCache = property;
            properties.clear();
            final StringTokenizer tk = new StringTokenizer(propertyCache, propertySeparators);
            while (tk.hasMoreElements()) {
                properties.add(tk.nextToken());
            }
        }
        if (!HashUtils.pointerEquals(lsaCache, lsa)) {
            lsaCache = lsa;
            mol = sapere.createMolecule(lsaCache);
        }
        if (env.getNodesNumber() > 0 && mol != null) {
            final double[] res = new double[properties.size()];
            int i = 0;
            Node<List<? extends ILsaMolecule>> node = pnCache.get(pos);
            if (node == null) {
                double range = Arrays.stream(env.getSize()).reduce(1, (x, y) -> FastMath.max(x, y) / FastMath.sqrt(env.getNodesNumber()));
                Collection<Node<List<? extends ILsaMolecule>>> neighs = env.getNodesWithinRange(pos, range);
                while (neighs.isEmpty()) {
                    range *= 2;
                    neighs = env.getNodesWithinRange(pos, range);
                }
                node = neighs.stream().reduce((n1, n2) -> env.getPosition(n1).getDistanceTo(pos) < env.getPosition(n2).getDistanceTo(pos) ? n1 : n2).get();
                pnCache.put(pos, node);
            }
            for (final String prop : properties) {
                /*
                 * Take the nearest node
                 */
                res[i++] = sapere.getProperty(node, mol, prop);
            }
            return res;
        }
        return new double[0];
    }

}
