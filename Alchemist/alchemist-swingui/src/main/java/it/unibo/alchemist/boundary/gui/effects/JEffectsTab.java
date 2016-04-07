/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.tape.JTapeFeatureStack;
import it.unibo.alchemist.boundary.gui.tape.JTapeFeatureStack.Type;
import it.unibo.alchemist.boundary.gui.tape.JTapeGroup;
import it.unibo.alchemist.boundary.gui.tape.JTapeMainFeature;
import it.unibo.alchemist.boundary.gui.tape.JTapeSection;
import it.unibo.alchemist.boundary.gui.tape.JTapeTab;
import it.unibo.alchemist.boundary.interfaces.GraphicalOutputMonitor;
import it.unibo.alchemist.boundary.l10n.R;

import java.awt.Color;
import java.awt.Component;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

import org.danilopianini.io.FileUtilities;
import org.danilopianini.view.GUIUtilities;

/**
 * Graphic component to handle effects.
 * 
 * @param <T>
 *            is the type for the concentration
 */
public class JEffectsTab<T> extends JTapeTab implements ItemListener {

    /**
     * 
     */
    private static final long serialVersionUID = 5687806032498247246L;
    private static final String EXT = ".aes", DESC = "Alchemist Effect Stack";
    private static final String EFFECT_TAB = R.getString("effect_tab");
    private static final String EFFECTS_GROUP = R.getString("effects_group");
    private static final String DRAW_LINKS = R.getString("draw_links");
    private static final String SAVE = R.getString("save");
    private static final String LOAD = R.getString("load");
    private static final String ADD_EFFECT = R.getString("add_effect");
    private static final String REMOVE_EFFECT = R.getString("remove_effect");
    private final GraphicalOutputMonitor<T> main;
    private final List<ActionListener> listeners = new LinkedList<>();
    private final JTapeFeatureStack stackSec;
    private final JButton addEffectButton, remEffectButton, saveButton, loadButton, moveLeftButton, moveRightButton;
    private File currentDirectory = new File(System.getProperty("user.home"));
    private JEffectRepresentation<T> selected;

    /**
     * Initialize the component.
     * 
     * @param main
     *            the target {@link GraphicalOutputMonitor}
     * @param displayPaintLinks
     *            pass true if you want a button to be able to switch link
     *            visualization on or off
     */
    public JEffectsTab(final GraphicalOutputMonitor<T> main, final boolean displayPaintLinks) {
        super(EFFECT_TAB);
        this.main = main;
        stackSec = new JTapeFeatureStack(Type.HORIZONTAL_STACK);
        final JTapeGroup effectsGroup = new JTapeGroup(EFFECTS_GROUP);
        if (displayPaintLinks) {
            final JTapeGroup showGroup = new JTapeGroup(DRAW_LINKS);
            final JTapeSection showLinksSec = new JTapeMainFeature();
            final JToggleButton paintLinksButton;
            paintLinksButton = new JToggleButton(DRAW_LINKS);
            paintLinksButton.addActionListener((e) -> main.setDrawLinks(paintLinksButton.isSelected()));
            showLinksSec.registerFeature(paintLinksButton);
            showGroup.registerSection(showLinksSec);
            registerGroup(showGroup);
        }
        final JTapeSection saveLoadSec = new JTapeFeatureStack(Type.VERTICAL_STACK);
        saveButton = new JButton(SAVE);
        saveButton.addActionListener((e) -> save(makeFileChooser()));
        loadButton = new JButton(LOAD);
        loadButton.addActionListener((e) -> load(makeFileChooser()));
        saveLoadSec.registerFeature(saveButton);
        saveLoadSec.registerFeature(loadButton);
        effectsGroup.registerSection(saveLoadSec);
        final JTapeSection addRemSec = new JTapeFeatureStack(Type.VERTICAL_STACK);
        addEffectButton = new JButton(ADD_EFFECT);
        addEffectButton.addActionListener((e) -> {
            final EffectBuilder eb = new EffectBuilder();
            eb.pack();
            final Point location = addEffectButton.getLocation();
            SwingUtilities.convertPointToScreen(location, addEffectButton);
            eb.setLocation(location);
            eb.setVisible(true);
            new Thread(() -> {
                final Effect effect = EffectFactory.buildEffect(eb.getResult());
                addEffect(effect);
                genEvents();
            }).start();
        });
        remEffectButton = new JButton(REMOVE_EFFECT);
        remEffectButton.addActionListener((event) -> {
            if (selected != null) {
                stackSec.unregisterFeature(selected);
                selected = null;
                genEvents();
            }
        });
        addRemSec.registerFeature(addEffectButton);
        addRemSec.registerFeature(remEffectButton);
        effectsGroup.registerSection(addRemSec);
        final JTapeSection moveSec = new JTapeFeatureStack(Type.VERTICAL_STACK);
        moveLeftButton = new JButton("<");
        moveLeftButton.addActionListener((e) -> moveSelectedLeft());
        moveRightButton = new JButton(">");
        moveRightButton.addActionListener((e) -> moveSelectedRight());
        moveSec.registerFeature(moveLeftButton);
        moveSec.registerFeature(moveRightButton);
        effectsGroup.registerSection(moveSec);
        stackSec.setBorder(new LineBorder(Color.BLACK, 1, false));
        effectsGroup.registerSection(stackSec);
        registerGroup(effectsGroup);
        addActionListener((e) -> {
            if (main != null) {
                main.setEffectStack(getEffects());
                main.repaint();
            }
        });
        final Effect defaultEffect = EffectFactory.buildDefaultEffect();
        addEffect(defaultEffect);
        genEvents();
        main.setEffectStack(getEffects());
    }

    /**
     * See {@link AbstractButton#addActionListener(ActionListener)}.
     * 
     * @param al
     *            the {@link ActionListener} to add
     */
    public void addActionListener(final ActionListener al) {
        listeners.add(al);
    }

    /**
     * Adds a new {@link Effect} to this stack.
     * 
     * @param e
     *            the {@link Effect} to add
     */
    public void addEffect(final Effect e) {
        final JEffectRepresentation<T> er = new JEffectRepresentation<>(e, main);
        registerItemSelectable(er);
        stackSec.registerFeature(er);
    }

    /**
     * Removes every effect.
     */
    public void clearEffects() {
        stackSec.removeAll();
    }

    private void genEvents() {
        revalidate();
        final ActionEvent event = new ActionEvent(this, 0, "");
        for (final ActionListener al : listeners) {
            al.actionPerformed(event);
        }
    }

    /**
     * @return The list of currently active {@link Effect}s.
     */
    public List<Effect> getEffects() {
        final List<Component> l = stackSec.getOrderedComponents();
        final List<Effect> l1 = new ArrayList<>(l.size());
        for (final Component c : l) {
            l1.add(((JEffectRepresentation<?>) c).getEffect());
        }
        return l1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void itemStateChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            selected = (JEffectRepresentation<T>) e.getItem();
            for (final Component c : stackSec.getComponents()) {
                final JEffectRepresentation<T> er = (JEffectRepresentation<T>) c;
                if (!er.equals(selected) && er.isSelected()) {
                    er.setSelected(false);
                }
            }
        }
    }

    /**
     * Decreases the priority of the selected effect.
     */
    protected void moveSelectedLeft() {
        if (selected != null) {
            final List<Component> l = stackSec.getOrderedComponents();
            final int index = l.indexOf(selected);
            if (index > 0) {
                stackSec.setComponentOrder(selected, index - 1);
                genEvents();
            }
        }
    }

    /**
     * Increases the priority of the selected effect.
     */
    protected void moveSelectedRight() {
        if (selected != null) {
            final List<Component> l = stackSec.getOrderedComponents();
            final int index = l.indexOf(selected);
            final int last = l.size() - 1;
            if (index < last) {
                stackSec.setComponentOrder(selected, index + 1);
                genEvents();
            }
        }
    }

    private void registerItemSelectable(final ItemSelectable is) {
        is.addItemListener(this);
    }

    /**
     * Sets a new effect stack.
     * 
     * @param effects
     *            is a {@link List} of effects
     */
    public void setEffects(final List<Effect> effects) {
        clearEffects();
        for (final Effect e : effects) {
            addEffect(e);
        }
        genEvents();
    }

    @Override
    public void setEnabled(final boolean value) {
        super.setEnabled(value);
        addEffectButton.setEnabled(value);
        remEffectButton.setEnabled(value);
        saveButton.setEnabled(value);
        loadButton.setEnabled(value);
        moveLeftButton.setEnabled(value);
        moveRightButton.setEnabled(value);
        for (final Component c : stackSec.getComponents()) {
            c.setEnabled(value);
        }
    }

    private JFileChooser makeFileChooser() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.getName().endsWith(EXT) || f.isDirectory();
            }
            @Override
            public String getDescription() {
                return DESC;
            }
        });
        fc.setCurrentDirectory(currentDirectory);
        return fc;
    }

    private void save(final JFileChooser fc) {
        final int result = fc.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentDirectory = fc.getSelectedFile().getParentFile();
            try {
                final File f = fc.getSelectedFile();
                final File fileToWrite = f.getName().endsWith(EXT) ? f : new File(f.getAbsolutePath() + EXT);
                FileUtilities.objectToFile(getEffects(), fileToWrite, false);
            } catch (final IOException e1) {
                GUIUtilities.errorMessage(e1);
            }
        }
    }

    private void load(final JFileChooser fc) {
        final int result = fc.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentDirectory = fc.getSelectedFile().getParentFile();
            try {
                clearEffects();
                @SuppressWarnings("unchecked")
                final List<Effect> effects = (List<Effect>) FileUtilities.fileToObject(fc.getSelectedFile());
                setEffects(effects);
                revalidate();
            } catch (IOException | ClassNotFoundException e1) {
                GUIUtilities.errorMessage(e1);
            }
        }
    }

}
