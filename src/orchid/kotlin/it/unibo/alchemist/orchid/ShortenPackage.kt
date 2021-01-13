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
import com.eden.orchid.api.theme.pages.OrchidPage

class ShortenPackage : TemplateFunction("shortenPackage", false) {

    @Option
    @Description("The input content, a package string")
    var input: String = ""

    @Option
    @Description("The maximum length of the package")
    var length: Int = maxLength

    override fun parameters() = arrayOf("input", "length")

    override fun apply(context: OrchidContext?, page: OrchidPage?): String {
        if (input.length <= length) {
            return input
        }
        var result = input
        do {
            val original = result
            result = result.shortenPackage()
        } while (result.length > length && original != result)
        return result
    }

    companion object {
        private const val maxLength = 35
        // Match the first package longer than one char
        private val regex = Regex("""^(?:\w+\.)*?(?:\w(\w+)\.)(?:\w+\.)*?\w+$""")

        private fun String.shortenPackage(): String = regex.matchEntire(this)?.groups?.get(1)?.range?.let {
            removeRange(it)
        } ?: this
    }
}
