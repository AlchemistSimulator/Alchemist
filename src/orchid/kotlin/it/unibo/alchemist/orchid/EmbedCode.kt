/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.orchid

import com.eden.orchid.api.compilers.TemplateTag
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.options.annotations.StringDefault

private const val tagname = "gistit"

@Description("Embed a GitHub code snippet in your page.", name = tagname)
class EmbedCode : TemplateTag(tagname, Type.Simple, true) {

    @Option
    @Description("The owner of the repository")
    @StringDefault("AlchemistSimulator")
    lateinit var owner: String

    @Option
    @Description("The repository name")
    @StringDefault("Alchemist")
    lateinit var repository: String

    @Option
    @Description("The branch for the file")
    @StringDefault("master")
    lateinit var branch: String

    @Option
    @Description("The file path")
    lateinit var file: String

    @Option
    @Description(
        "Line slice. Format is a number for a single line or start:end." +
            "Negative indexing supported (e.g. 0:-2)"
    )
    @StringDefault("")
    lateinit var slice: String

    override fun parameters() = arrayOf("owner", "repository", "branch", "file", "slice")
}
