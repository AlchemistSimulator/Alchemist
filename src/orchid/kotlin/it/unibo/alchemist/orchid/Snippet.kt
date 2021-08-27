/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.orchid

import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.compilers.TemplateFunction
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.options.annotations.StringDefault
import com.eden.orchid.api.theme.pages.OrchidPage
import java.io.File
import java.lang.IllegalArgumentException

private const val tagname = "snippet"

@Description("Embed code from this project.", name = tagname)
class Snippet : TemplateFunction(tagname, true) {

    @Option
    @Description("The file path")
    lateinit var file: String

    @Option
    @Description("The file path")
    @StringDefault("src/test/resources/website-snippets")
    lateinit var from: String

    @Option
    @Description("The file path")
    @StringDefault(autodetect)
    var language: String? = null

    override fun parameters() = arrayOf("file", "language", "from")
    override fun apply(p0: OrchidContext?, p1: OrchidPage?): String {
        val actualFile = File("${System.getProperty("user.dir")}/$from/$file")
        require(actualFile.exists()) {
            "File $actualFile was required to be embedded but does not exist"
        }
        val actualLanguage = language.takeUnless { it == autodetect } ?: when (actualFile.extension) {
            "java" -> "java"
            "kt" -> "kotlin"
            "py" -> "python"
            "scala" -> "scala"
            in setOf("md", "MD") -> "markdown"
            in setOf("yaml", "yml") -> "yaml"
            else -> throw IllegalArgumentException("Cannot auto-detect the language of file $file")
        }
        val content = actualFile.readText().trim()
        require(content.isNotEmpty()) {
            "File $file cannot be embedded in the webpage because it is blank."
        }
        return "\n```$actualLanguage\n$content\n```\n"
    }

    companion object {
        const val autodetect = "autodetect"
    }
}
