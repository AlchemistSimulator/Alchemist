/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import javafx.application.Platform;

/**
 * 
 *
 */
public class Watcher implements Runnable {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);
    private static final long TIMEOUT = 10;
    private final LeftLayoutController ctrlLeft;
    private final CenterLayoutController ctrlCenter;

    private WatchService watcherServ;
    private String folderPath;
    private boolean isAlive = true;
    private final Map<WatchKey, Path> keys = new HashMap<>();

    /**
     * 
     * @param ctrlLeft The controller of LeftLayout.
     * @param ctrlCenter The controller of CenterLayout.
     */
    public Watcher(final LeftLayoutController ctrlLeft, final CenterLayoutController ctrlCenter) {
        try {
            this.watcherServ = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            L.error("This system does not support watching file system objects for changes and events.", e);
        }
        this.ctrlLeft = ctrlLeft;
        this.ctrlCenter = ctrlCenter;
    }

    /**
     * 
     * @param path a folder path of project.
     */
    public void registerPath(final String path) {
        this.folderPath = path;
        final Path dir = Paths.get(path);
        recursiveRegistration(dir);
    }

    /**
     * 
     * @return True if the watcher is alive, otherwise false.
     */
    public boolean isWatcherAlive() {
        return this.isAlive;
    }

    /**
     * 
     * @return The project folder path.
     */
    public String getFolderPath() {
        return this.folderPath;
    }

    /**
     * Set value of isAlive for terminate the watcher.
     */
    public void terminate() {
        this.isAlive = false;
    }

    @Override
    public void run() {
        while (this.isAlive) {
            WatchKey key = null;
            try {
                key = this.watcherServ.poll(TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                L.error("Error watcher, because it was interrupted. This is most likely a bug.", e);
            }
            if (key != null) {
                for (final WatchEvent<?> event : key.pollEvents()) {
                    final WatchEvent.Kind<?> kind = event.kind();
                    if (event.context() instanceof Path) {
                        final Path fileName = (Path) event.context();
                        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
                            if (fileName.toString().equals(".alchemist_project_descriptor.json")) {
                                refreshGrid();
                            } else if (this.ctrlCenter.getSimulationFilePath().endsWith(fileName.toString())) {
                                refreshVariables();
                            } else {
                                refreshTreeView(this.folderPath);
                            }
                        } else if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                            recursiveRegistration(resolvePath(key, fileName));
                            if (this.folderPath.equals(FilenameUtils.getFullPathNoEndSeparator(resolvePath(key, fileName).toFile().getAbsolutePath()))) {
                                refreshTreeView(this.folderPath);
                            }
                        } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                            WatchKey keyToDelete = null;
                            final Path path = resolvePath(key, fileName);
                            for (final WatchKey w : keys.keySet()) {
                                if (keys.get(w).equals(path)) {
                                    keyToDelete = w;
                                }
                            }
                            if (keyToDelete != null) {
                                refreshFolder(path);
                                keys.remove(keyToDelete);
                                keyToDelete.cancel();
                            } else {
                                refreshFile(path);
                            }
                            if (this.folderPath.equals(FilenameUtils.getFullPathNoEndSeparator(path.toFile().getAbsolutePath()))) {
                                refreshTreeView(this.folderPath);
                            }
                        } else {
                            throw new IllegalStateException("Unexpected event of kind " + kind);
                        }
                    }
                }
                key.reset();
            }
        }
        if (!isAlive) {
            try {
                this.watcherServ.close();
            } catch (IOException e) {
                L.error("I/O error while closing of watcher.", e);
            }
        }
    }

    private void recursiveRegistration(final Path root) {
        /*
         * Work around idi0tic Windows file manager behavior by trying multiple times in case of failure.
         */
        for (int attempts = 0; attempts < 3 && Files.exists(root); attempts++) {
            try {
                Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                        try {
                            final WatchKey key = dir.register(watcherServ, 
                                    StandardWatchEventKinds.ENTRY_CREATE, 
                                    StandardWatchEventKinds.ENTRY_DELETE, 
                                    StandardWatchEventKinds.ENTRY_MODIFY);
                            keys.put(key, dir);
                        } catch (IOException e) {
                            L.error("Error register the folder path to watcher. This is most likely a bug.", e);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                break;
            } catch (IOException e) {
                try {
                    L.error("There was an I/O error. This is most likely due to you using the Windows file manager.", e);
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    L.error("The watcher got interrupted. Please report this error, it is most likely a bug.", e);
                }
            }
        }
    }

    private Path resolvePath(final WatchKey key, final Path name) {
        return keys.get(key).resolve(name);
    }

    private void refreshTreeView(final String path) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ctrlLeft.setTreeView(new File(path));
            }
        });
    }

    private void refreshGrid() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ctrlCenter.setField();
            }
        });
    }

    private void refreshFolder(final Path path) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ctrlCenter.setFolderAfterDelete(path);
            }
        });
    }

    private void refreshFile(final Path path) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ctrlCenter.setFileAfterDelete(path);
            }
        });
    }

    private void refreshVariables() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ctrlCenter.setVariablesList();
            }
        });
    }
}
