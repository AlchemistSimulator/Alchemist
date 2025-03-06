/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter.utils

import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import java.util.function.Supplier

/**
 * A file lock that uses the local filesystem.
 */
class FileLock(name: String, folder: String = "tmp") {
    private val lockFile: File

    init {
        lockFile = File("$folder/$name.lock")
        if (!lockFile.isFile) {
            lockFile.createNewFile()
        }
    }

    /**
     * Executes the given [Supplier] while holding the lock.
     */
    fun <T> doWithLock(f: Supplier<T>): T {
        RandomAccessFile(lockFile, "rw").channel.use { channel ->
            val fileLock: FileLock = channel.lock()
            val result: T = f.get()
            fileLock.release()
            return result
        }
    }
}
