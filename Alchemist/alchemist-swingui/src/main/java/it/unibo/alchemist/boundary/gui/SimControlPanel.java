/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractButton;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

import it.unibo.alchemist.boundary.gui.tape.JTapeFeatureStack;
import it.unibo.alchemist.boundary.gui.tape.JTapeGroup;
import it.unibo.alchemist.boundary.gui.tape.JTapeMainFeature;
import it.unibo.alchemist.boundary.gui.tape.JTapeSection;
import it.unibo.alchemist.boundary.l10n.R;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;

/**
 * This class maintains multiple control panels for controlling a simulation,
 * ensuring that they are coherently updated.
 * 
 */
public final class SimControlPanel extends JTapeGroup {

    private static final long serialVersionUID = 8245609434257107323L;

    private static final Map<Simulation<?>, Set<SimControlPanel>> SIMCONTROLMAP = new MapMaker()
            .weakKeys().makeMap();

    private boolean down;

    private final Map<SimControlCommand, SimControlButton> map = new EnumMap<>(SimControlCommand.class);

    private Simulation<?> simulation;

    private static synchronized void addActionListener(final SimControlPanel cmd, final ActionListener l) {
        for (final SimControlPanel scp : getSiblings(cmd)) {
            for (final SimControlButton b : scp.map.values()) {
                b.addActionListener(l);
            }
        }
    }

    private static synchronized void checkOldAndRemove() {
        final Set<Simulation<?>> toRemove = new HashSet<>();
        for (final Simulation<?> sim : SIMCONTROLMAP.keySet()) {
            if (sim.getStatus().equals(Status.STOPPED) || SIMCONTROLMAP.get(sim).isEmpty()) {
                toRemove.add(sim);
            }
        }
        for (final Simulation<?> sim : toRemove) {
            SIMCONTROLMAP.remove(sim);
        }
    }

    /**
     * @param sim
     *            the simulation, null values allowed.
     * @return a new SimControlPanel
     */
    public static SimControlPanel createControlPanel(final Simulation<?> sim) {
        if (sim == null) {
            return new SimControlPanel();
        }
        return new SimControlPanel(sim);
    }

    private static synchronized Set<SimControlPanel> getSiblings(final SimControlPanel scp) {
        if (scp.simulation != null) {
            final Set<SimControlPanel> result = SIMCONTROLMAP.get(scp.simulation);
            return result == null ? new HashSet<SimControlPanel>() : result;
        }
        return Sets.newHashSet(scp);
    }

    private static synchronized void removeAllActionListeners(final SimControlPanel scp) {
        for (final SimControlButton but : scp.map.values()) {
            while (but.getActionListeners().length > 0) {
                but.removeActionListener(but.getActionListeners()[0]);
            }
        }
    }

    private static synchronized void setButtonEnabled(final SimControlPanel pan, final SimControlCommand cmd, final boolean enabled) {
        for (final SimControlPanel scp : getSiblings(pan)) {
            scp.map.get(cmd).setEnabled(enabled);
        }
    }

    private static synchronized void setSimulation(final SimControlPanel scp, final Simulation<?> sim) {
        if (sim != scp.simulation) {
            if (scp.simulation != null) {
                /*
                 * Remove from the previous set
                 */
                getSiblings(scp).remove(scp);
            }
            checkOldAndRemove();
            scp.simulation = sim;
            Set<SimControlPanel> l = SIMCONTROLMAP.get(sim);
            if (l == null) {
                l = new HashSet<>();
                SIMCONTROLMAP.put(sim, l);
            } else {
                /*
                 * Remove all the listeners
                 */
                removeAllActionListeners(scp);
                /*
                 * Clone one of the existing elements. Ensures consistency.
                 */
                if (!l.isEmpty()) {
                    final SimControlPanel clone = l.iterator().next();
                    for (final Entry<SimControlCommand, SimControlButton> entry : clone.map.entrySet()) {
                        final SimControlCommand cmd = entry.getKey();
                        final SimControlButton but = entry.getValue();
                        final SimControlButton dbut = scp.map.get(cmd);
                        setButtonEnabled(scp, cmd, but.isEnabled());
                        for (final ActionListener al : but.getActionListeners()) {
                            dbut.addActionListener(al);
                        }
                    }
                }
            }
            l.add(scp);
        }
    }

    private static synchronized void shutdown(final SimControlPanel scp) {
        if (scp.simulation != null) {
            final Set<SimControlPanel> scset = SIMCONTROLMAP.get(scp.simulation);
            if (scset != null) {
                scset.remove(scp);
            }
        }
        scp.simulation = null;
        scp.down = true;
        removeAllActionListeners(scp);
    }

    /**
     * Builds a new BaseSimControlPanel.
     */
    private SimControlPanel() {
        super(R.getString("controls"));
        // setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        final JTapeSection mfplay = new JTapeMainFeature();
        final JTapeSection s = new JTapeFeatureStack();
        final JTapeSection mfstop = new JTapeMainFeature();
        for (final SimControlCommand scc : SimControlCommand.values()) {
            final SimControlButton but = scc.createButton();
            map.put(scc, but);
            // add(but);
            if (scc == SimControlCommand.PLAY) {
                mfplay.registerFeature(but);
                // playButt = but;
            } else if (scc == SimControlCommand.STOP) {
                mfstop.registerFeature(but);
            } else {
                s.registerFeature(but);
            }
        }
        registerSection(mfplay);
        registerSection(mfstop);
        registerSection(s);
    }

    private SimControlPanel(final Simulation<?> sim) {
        this();
        setSimulation(sim);
    }

    /**
     * See {@link AbstractButton#addActionListener(ActionListener)}.
     * 
     * @param l
     *            the {@link ActionListener} to add
     */
    public void addActionListener(final ActionListener l) {
        addActionListener(this, l);
    }

    /**
     * @param <T>
     *            concentrations
     * @return the simulation
     */
    @SuppressWarnings("unchecked")
    public <T> Simulation<T> getSimulation() {
        return (Simulation<T>) simulation;
    }

    /**
     * @return the isDown
     */
    public boolean isDown() {
        return down;
    }

    /**
     * @param cmd
     *            the command corresponding to the button
     * @param enabled
     *            true if you want the button to be enabled, false otherwise
     */
    public void setButtonEnabled(final SimControlCommand cmd, final boolean enabled) {
        setButtonEnabled(this, cmd, enabled);
    }

    @Override
    public synchronized void setEnabled(final boolean e) {
        super.setEnabled(e);
        for (final SimControlButton b : map.values()) {
            b.setEnabled(e);
        }
        checkOldAndRemove();
    }

    /**
     * @param sim
     *            the simulation to set
     */
    public void setSimulation(final Simulation<?> sim) {
        setSimulation(this, sim);
    }

    /**
     * To be called when this control panel will be no longer useful.
     */
    public void shutdown() {
        shutdown(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " controlling " + simulation;
    }

}
