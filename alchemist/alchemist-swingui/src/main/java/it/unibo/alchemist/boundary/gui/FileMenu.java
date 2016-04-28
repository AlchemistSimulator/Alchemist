/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;

import org.danilopianini.view.GUIUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static it.unibo.alchemist.boundary.l10n.R.getString;

/**
 */
public class FileMenu extends AbstractMenu {

    private static final long serialVersionUID = 5209455686362711386L;
    private static final JMenuItem[] ITEMS = {
            new JMenuItem(getString("quit")),
            new JMenuItem(getString("load_jar_file")),
    };
    private static final Logger L = LoggerFactory.getLogger(FileMenu.class);

    /**
     * Builds the File menu.
     */
    public FileMenu() {
        super(getString("file"), ITEMS);
    }

    @Override
    @SuppressFBWarnings(value = "DM_EXIT", justification = "We actually want to exit in case quit is pressed")
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(ITEMS[0])) {
            System.exit(0);
        } else if (e.getSource().equals(ITEMS[1])) {
            final JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(final File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
                }

                @Override
                public String getDescription() {
                    return getString("jar_file");
                }
            });
            final int response = fc.showOpenDialog(null);
            if (response == JFileChooser.APPROVE_OPTION) {
                final File chosen = fc.getSelectedFile();
                Method method;
                try {
                    /*
                     * This horrible hack won't work if a SecurityManager is
                     * attached.
                     */
                    method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
                    method.setAccessible(true);
                    method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { chosen.toURI().toURL() });
                    GUIUtilities.alertMessage(getString("load_jar_file"), chosen + " " + getString("successfully_included_in_classpath"));
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | MalformedURLException e1) {
                    GUIUtilities.errorMessage(e1);
                    L.error(getString("cannot_load_jar"), e1);
                }
            }
        }
    }

}
