/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors


 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.danilopianini.lang.HashUtils;
import org.danilopianini.view.ExportForGUI;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.Incarnation;

/**
 *
 */
@ExportInspector
public class ConcentrationLogger extends NodeInspector<Double> {

    private static final long serialVersionUID = -7930729203484494987L;

    @ExportForGUI(nameToExport = "Molecules to track")
    private final String molToTrack = ""; // NOPMD, this is just a stub class
    @ExportForGUI(nameToExport = "Molecule separators")
    private final String molSeparators = " ;,:"; // NOPMD, this is just a stub class

    private String molCache;
    private final List<Molecule> mols = new LinkedList<>();

    private final transient Incarnation<Double> bio = new BiochemistryIncarnation();


    @Override
    protected double[] getProperties(final Environment<Double> env, final Node<Double> node, final Reaction<Double> r, final Time time, final long step) {
        if (!HashUtils.pointerEquals(molCache, molToTrack)) {
            molCache = molToTrack;
            mols.clear();
            final StringTokenizer tk = new StringTokenizer(molCache, molSeparators);
            while (tk.hasMoreElements()) {
                mols.add(bio.createMolecule(tk.nextToken()));
            }
        }

        final double[] res = new double[mols.size()];
        int i = 0;
        for (final Molecule mol : mols) {
            res[i++] = bio.getProperty(node, mol, null);
        }
        return res;
    }
}