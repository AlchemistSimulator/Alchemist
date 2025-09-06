/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import it.unibo.alchemist.Alchemist
import java.io.File
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests for improved error messages in Alchemist.
 */
class TestAlchemistErrorMessages {

    @Test
    fun `should provide clear error when directory is provided instead of file`() {
        // Enable test mode to prevent System.exit
        Alchemist.enableTestMode()

        // Create a temporary directory
        val tempDir = File.createTempFile("test_dir", null)
        tempDir.delete()
        tempDir.mkdir()
        tempDir.deleteOnExit()

        try {
            // This should throw an exception with an improved error message
            val exception = assertThrows<Exception> {
                Alchemist.main(arrayOf("run", tempDir.absolutePath))
            }

            // The error message should indicate that a directory was provided instead of a file
            val errorMessage = exception.message ?: ""
            assertTrue(
                errorMessage.contains("directory") &&
                    (errorMessage.contains("file") || errorMessage.contains("expected")),
                "Error message should mention that a directory was provided instead of a file. Got: $errorMessage",
            )
        } finally {
            tempDir.delete()
        }
    }

    @Test
    fun `should provide clear error when non-existent file is provided`() {
        // Enable test mode to prevent System.exit
        Alchemist.enableTestMode()

        val nonExistentFile = "/path/to/nonexistent/file.yml"

        // This should throw an exception
        val exception = assertThrows<Exception> {
            Alchemist.main(arrayOf("run", nonExistentFile))
        }

        // The error message should mention that the file was not found
        val errorMessage = exception.message ?: ""
        assertTrue(
            errorMessage.contains("not found") || errorMessage.contains("No classpath resource"),
            "Error message should mention that the file was not found. Got: $errorMessage",
        )
    }
}
