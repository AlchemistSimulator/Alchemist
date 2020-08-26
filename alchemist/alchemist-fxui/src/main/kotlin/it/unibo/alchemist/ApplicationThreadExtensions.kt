/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import javafx.application.Platform
import javafx.application.Platform.runLater
import java.util.concurrent.FutureTask

/**
 * Runs the given task on the FX thread and waits for the task to finish, returning any value the task returns.
 * Throws an exception if the task takes more than a given amount of milliseconds.
 *
 * @param task the task to execute.
 */
fun <T : Any> syncRunLater(task: () -> T): T =
    FutureTask(task).run {
        runLater {
            run()
        }
        get()
    }

/**
 * Checks if the current thread is the FX Application thread and calls [syncRunLater] if so, otherwise runs the task.
 *
 * @param task the task to execute.
 */
fun <T : Any> syncRunOnFXThread(task: () -> T): T =
    if (!Platform.isFxApplicationThread()) {
        syncRunLater { task() }
    } else {
        task()
    }

/**
 * Checks if the current thread is the FX Application thread and calls [runLater] if so, otherwise runs the task.
 *
 * @param task the task to execute.
 */
fun runOnFXThread(task: () -> Unit): Unit =
    if (!Platform.isFxApplicationThread()) {
        runLater { task() }
    } else {
        task()
    }
