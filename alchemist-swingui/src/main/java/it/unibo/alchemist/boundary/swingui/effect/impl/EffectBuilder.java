/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.effect.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.swingui.effect.api.Effect;
import it.unibo.alchemist.boundary.swingui.impl.AlchemistSwingUI;
import it.unibo.alchemist.boundary.swingui.impl.LocalizedResourceBundle;
import it.unibo.alchemist.util.ClassPathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 */
@Deprecated
@SuppressFBWarnings(
    value = { "SE_BAD_FIELD", "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR" },
    justification = "This class is not meant to get serialized, and it is final"
)
public final class EffectBuilder extends JFrame implements ActionListener {

    private static final long serialVersionUID = -5030318714404946998L;
    private static final List<Class<? extends Effect>> EFFECTS = ClassPathScanner.subTypesOf(Effect.class, "it.unibo.alchemist");
    private static final Logger L = LoggerFactory.getLogger(EffectBuilder.class);
    private static final String ALCHEMIST_EFFECT_BUILDER = LocalizedResourceBundle.getString("alchemist_effect_builder");
    private static final String EFFECT = LocalizedResourceBundle.getString("effect");
    private final CountDownLatch barrier = new CountDownLatch(1); // NOPMD: class not meant to be serializable
    private final JComboBox<Class<? extends Effect>> effectBox;

    /**
     * Default constructor.
     */
    public EffectBuilder() {
        super(ALCHEMIST_EFFECT_BUILDER);
        setUndecorated(true);
        final Container pane = getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        final JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        final Icon effectIcon = AlchemistSwingUI.loadScaledImage("/actions/tools-wizard.png");
        p1.add(new JLabel(EFFECT, effectIcon, SwingConstants.LEADING));
        pane.add(p1);
        pane.add(Box.createVerticalGlue());
        effectBox = new JComboBox<>();
        for (final Class<? extends Effect> c : EFFECTS) {
            if (!Modifier.isAbstract(c.getModifiers())) {
                effectBox.addItem(c);
            }
        }
        pane.add(effectBox);
        final JPanel p4 = new JPanel();
        final Icon done = AlchemistSwingUI.loadScaledImage("/categories/applications-graphics.png");
        final JButton button = new JButton(LocalizedResourceBundle.getString("done"));
        button.setIcon(done);
        p4.add(button);
        pane.add(p4);
        button.addActionListener(this);

        pane.add(Box.createVerticalGlue());
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        barrier.countDown();
        setVisible(false);
    }

    /**
     * @return a future with the built effect
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Effect> getResult() {
        try {
            barrier.await();
        } catch (final InterruptedException e) {
            L.error("Bug in " + getClass(), e);
        }
        return (Class<? extends Effect>) effectBox.getSelectedItem();
    }

}
